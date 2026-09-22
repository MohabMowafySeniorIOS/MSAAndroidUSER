package com.msa.android.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.msa.android.domain.model.AssetType
import com.msa.android.domain.model.PortfolioItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists portfolio holdings as a JSON array under a single DataStore key.
 *
 * Why DataStore + plain JSON (not Room): the portfolio is realistically a
 * handful of items, all loaded together at every read. A serialized blob
 * keeps things simple and the entire collection comfortably fits in a single
 * preference value.
 *
 * We use `org.json` (already on the classpath via the manifest dependency)
 * rather than introducing Moshi for one flat shape.
 */
@Singleton
class PortfolioPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val DS_NAME = "portfolio"
        private val KEY_ITEMS = stringPreferencesKey("items_json")
    }

    private val Context.portfolioStore: DataStore<Preferences> by preferencesDataStore(DS_NAME)
    private val dataStore = context.portfolioStore

    val itemsFlow: Flow<List<PortfolioItem>> = dataStore.data.map { prefs ->
        val json = prefs[KEY_ITEMS] ?: return@map emptyList()
        runCatching { decode(json) }.getOrElse { emptyList() }
    }

    suspend fun addItem(item: PortfolioItem)    = mutate { it + item }
    suspend fun updateItem(item: PortfolioItem) = mutate { list ->
        list.map { if (it.id == item.id) item else it }
    }
    suspend fun removeItem(id: String)          = mutate { it.filterNot { it.id == id } }
    suspend fun clearAll() {
        dataStore.edit { it.remove(KEY_ITEMS) }
    }

    private suspend fun mutate(transform: (List<PortfolioItem>) -> List<PortfolioItem>) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_ITEMS]
                ?.let { runCatching { decode(it) }.getOrNull() }
                .orEmpty()
            prefs[KEY_ITEMS] = encode(transform(current))
        }
    }

    // ── JSON codec ──────────────────────────────────────────────────────

    private fun encode(items: List<PortfolioItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
                .put("id", item.id)
                .put("type", item.type.name)
                .put("quantity", item.quantity)
                .put("purchaseTotalPrice", item.purchaseTotalPrice)
                .put("purchaseDate", item.purchaseDate)
            item.notes?.let { obj.put("notes", it) }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decode(json: String): List<PortfolioItem> {
        val arr = JSONArray(json)
        val out = ArrayList<PortfolioItem>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            // valueOf can throw on unknown enums (older app reading newer data);
            // skip silently so the rest of the list still loads.
            val typeName = o.optString("type", "")
            val type = runCatching { AssetType.valueOf(typeName) }.getOrNull() ?: continue
            out += PortfolioItem(
                id                 = o.optString("id"),
                type               = type,
                quantity           = o.optDouble("quantity", 0.0),
                purchaseTotalPrice = o.optDouble("purchaseTotalPrice", 0.0),
                purchaseDate       = o.optLong("purchaseDate", 0L),
                notes              = if (o.has("notes")) o.optString("notes") else null
            )
        }
        return out
    }
}

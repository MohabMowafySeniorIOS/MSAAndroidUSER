package com.msa.android.data.repository

import com.msa.android.data.source.local.PortfolioPreferences
import com.msa.android.domain.model.PortfolioItem
import com.msa.android.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure delegation today, but the interface stays so we can swap storage
 * (Room, Firestore, etc.) later without changing every consumer.
 */
@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    private val prefs: PortfolioPreferences
) : PortfolioRepository {
    override fun observeItems(): Flow<List<PortfolioItem>>       = prefs.itemsFlow
    override suspend fun addItem(item: PortfolioItem)            = prefs.addItem(item)
    override suspend fun updateItem(item: PortfolioItem)         = prefs.updateItem(item)
    override suspend fun removeItem(id: String)                  = prefs.removeItem(id)
}

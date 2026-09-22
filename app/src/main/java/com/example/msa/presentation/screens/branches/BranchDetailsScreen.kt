package com.msa.android.presentation.screens.branches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.msa.android.R
import com.msa.android.data.source.local.LanguagePreferences
import com.msa.android.domain.model.Branch
import com.msa.android.domain.repository.BranchRepository
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BranchDetailsState(
    val loading: Boolean = true,
    val branch: Branch? = null,
    val arabic: Boolean = true
)

@HiltViewModel
class BranchDetailsViewModel @Inject constructor(
    private val repo: BranchRepository,
    private val language: LanguagePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(BranchDetailsState())
    val state: StateFlow<BranchDetailsState> = _state.asStateFlow()

    fun load(id: Long) {
        // محمّل خلاص — مانعيدش الطلب مع كل إعادة رسم
        if (_state.value.branch?.id == id) return

        viewModelScope.launch {
            val arabic = language.languageFlow.first() == "ar"

            _state.value = BranchDetailsState(loading = true, arabic = arabic)

            val branch = repo.branch(id).getOrNull()

            _state.value = BranchDetailsState(loading = false, branch = branch, arabic = arabic)
        }
    }
}

/**
 * تفاصيل الفرع: صورة، خريطة، مواعيد، وأزرار التواصل.
 *
 * الخريطة جوّه التطبيق محتاجة مفتاح `MAPS_API_KEY` في
 * `local.properties`. من غيره الخريطة بتظهر فاضية — وعشان كده زرار
 * «الاتجاهات» موجود تحتها دايماً وبيفتح تطبيق الخرايط الخارجي، فالشاشة
 * بتفضل مفيدة حتى من غير مفتاح.
 */
@Composable
fun BranchDetailsScreen(
    branchId: Long,
    onBack: () -> Unit,
    vm: BranchDetailsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(branchId) { vm.load(branchId) }

    MSABackground {
        Column(Modifier.fillMaxSize()) {

            MSATopBar(
                title = stringResource(R.string.branch_details_title),
                showBack = true,
                onBack = onBack
            )

            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = GoldenBorder)
                }

                state.branch == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        text = stringResource(R.string.branches_load_failed),
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                else -> {
                    val branch = state.branch!!
                    val arabic = state.arabic

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        branch.imageUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }

                        // ── الاسم والعنوان ────────────────────────────
                        Card {
                            Text(
                                text = branch.name(arabic),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            branch.address(arabic)?.let {
                                Spacer(Modifier.height(6.dp))
                                IconLine(Icons.Filled.LocationOn, it)
                            }

                            branch.workingHours(arabic)?.let {
                                Spacer(Modifier.height(6.dp))
                                IconLine(Icons.Filled.AccessTime, it)
                            }

                            branch.distanceKm?.let { km ->
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = stringResource(
                                        R.string.branch_distance_from_you,
                                        if (km < 1.0)
                                            String.format(
                                                ctx.resources.configuration.locales[0],
                                                "%.0f m", km * 1000
                                            )
                                        else
                                            String.format(
                                                ctx.resources.configuration.locales[0],
                                                "%.1f km", km
                                            )
                                    ),
                                    color = GoldenBorder,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // ── الخريطة ───────────────────────────────────
                        if (branch.hasLocation) {
                            BranchMap(branch)

                            GoldButton(
                                text = stringResource(R.string.branch_directions),
                                onClick = { BranchActions.openDirections(ctx, branch) }
                            )
                        }

                        // ── التواصل ───────────────────────────────────
                        if (branch.phone != null || branch.whatsapp != null) {
                            Card {
                                Text(
                                    text = stringResource(R.string.branch_contact),
                                    color = GoldenBorder,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(10.dp))

                                branch.phone?.let { phone ->
                                    ContactButton(
                                        icon = Icons.Filled.Call,
                                        label = stringResource(R.string.branch_call),
                                        value = phone,
                                        onClick = { BranchActions.call(ctx, phone) }
                                    )
                                }

                                branch.whatsapp?.let { number ->
                                    Spacer(Modifier.height(8.dp))
                                    ContactButton(
                                        icon = Icons.Filled.Chat,
                                        label = stringResource(R.string.branch_whatsapp),
                                        value = number,
                                        onClick = { BranchActions.whatsapp(ctx, number) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * خريطة صغيرة بعلامة على الفرع.
 *
 * كل إيماءات الخريطة مقفولة (`scrollGesturesEnabled = false` وإخواتها)
 * عن قصد: الخريطة جوّه شاشة بتتمرّر رأسياً، ولو سبناها تستقبل السحب
 * كان المستخدم اللي بيحاول يمرّر الشاشة هيحرّك الخريطة بالغلط. اللي
 * عايز يتحرك على الخريطة بيدوس «الاتجاهات» ويفتح تطبيق الخرايط.
 */
@Composable
private fun BranchMap(branch: Branch) {
    val position = remember(branch.id) {
        LatLng(branch.latitude ?: 0.0, branch.longitude ?: 0.0)
    }

    val cameraState = rememberCameraPositionState {
        // 15f ≈ مستوى الحي — الشارع والمعالم حواليه باينة
        this.position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, GoldenBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = false,
                rotationGesturesEnabled = false,
                tiltGesturesEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = position),
                title = branch.name(arabic = true)
            )
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
private fun IconLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = Color(0xFFCCCCCC), fontSize = 13.sp)
    }
}

@Composable
private fun ContactButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GoldenBorder.copy(alpha = 0.12f))
            .border(1.dp, GoldenBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldenBorder,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.weight(1f))

        // الرقم دايماً LTR حتى في الواجهة العربية.
        //
        // من غير كده رقم زي "+20 100 123 4567" بيتعرض بترتيب مقلوب
        // في التخطيط العربي والمستخدم بينسخه غلط. LocalLayoutDirection
        // بتقلب الاتجاه لعنصر واحد بس من غير ما تلمس باقي الصف.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = value,
                color = Color(0xFFCCCCCC),
                fontSize = 13.sp
            )
        }
    }
}

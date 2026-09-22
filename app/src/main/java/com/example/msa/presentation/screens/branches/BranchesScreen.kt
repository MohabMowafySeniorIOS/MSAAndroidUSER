package com.msa.android.presentation.screens.branches

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.msa.android.R
import com.msa.android.domain.model.Branch
import com.msa.android.presentation.common.components.GoldButton
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder

/**
 * فروعنا.
 *
 * الشاشة بتطلب إذن الموقع لما تتفتح — هنا، مش عند فتح التطبيق: الطلب
 * في سياقه («عايز أشوف أقرب فرع») نسبة قبوله أعلى بكتير.
 *
 * ولو المستخدم رفض، الشاشة **مبتفضاش**: بتعرض كل الفروع بترتيب اللوحة
 * مع سطر بيوضّح إن الترتيب مش بالأقرب وزرار لتفعيل الموقع.
 */
@Composable
fun BranchesScreen(
    onBack: () -> Unit,
    onOpenBranch: (Long) -> Unit,
    vm: BranchesViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // بنقبل التقريبي أو الدقيق — التقريبي كفاية لترتيب الفروع،
        // والمستخدم من أندرويد 12 بيقدر يختار التقريبي من النافذة
        vm.onPermissionResult(result.values.any { it })
    }

    fun requestLocation() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    // أول ما الشاشة تتفتح: لو الإذن مش متاخد بنطلبه مرة واحدة.
    // `state.locationDenied` بيتحدّث من الـ ViewModel بعد أول تحميل،
    // فالشرط ده مش بيتنفّذ غير لما نتأكد إن الإذن فعلاً ناقص.
    androidx.compose.runtime.LaunchedEffect(state.locationDenied, state.loading) {
        if (!state.loading && state.locationDenied) {
            requestLocation()
        }
    }

    MSABackground {
        Column(Modifier.fillMaxSize()) {

            MSATopBar(
                title = stringResource(R.string.branches_title),
                showBack = true,
                onBack = onBack
            )

            when {
                state.loading && state.branches.isEmpty() -> Box(
                    Modifier.fillMaxSize(), Alignment.Center
                ) { CircularProgressIndicator(color = GoldenBorder) }

                state.failed -> Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.branches_load_failed),
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    GoldButton(
                        text = stringResource(R.string.retry),
                        big = false,
                        onClick = { vm.load(forceRefresh = true) }
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // شريط توضيحي للترتيب — المستخدم لازم يعرف إذا كانت
                    // القايمة مرتّبة بالأقرب له ولا لأ
                    item(key = "sort_note") {
                        SortNote(
                            sortedByDistance = state.sortedByDistance,
                            locationDenied = state.locationDenied,
                            locationOff = state.locationOff,
                            onEnableLocation = ::requestLocation
                        )
                    }

                    if (state.branches.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(R.string.branches_empty),
                                color = Color(0xFFCCCCCC),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
                            )
                        }
                    }

                    items(state.branches, key = { it.id }) { branch ->
                        BranchCard(
                            branch = branch,
                            arabic = state.arabic,
                            onClick = { onOpenBranch(branch.id) },
                            onDirections = { BranchActions.openDirections(ctx, branch) },
                            onCall = { branch.phone?.let { BranchActions.call(ctx, it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortNote(
    sortedByDistance: Boolean,
    locationDenied: Boolean,
    locationOff: Boolean,
    onEnableLocation: () -> Unit
) {
    // الترتيب بالأقرب شغال — سطر تأكيد بسيط وخلاص
    if (sortedByDistance) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = GoldenBorder,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.branches_sorted_by_distance),
                color = Color(0xFFCCCCCC),
                fontSize = 12.sp
            )
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(
                // رسالتين مختلفتين: "الإذن مرفوض" غير "خدمة الموقع
                // مقفولة" — الحل مختلف في كل حالة والمستخدم لازم يعرف
                if (locationOff) R.string.branches_location_off
                else R.string.branches_location_hint
            ),
            color = Color.White,
            fontSize = 13.sp
        )

        if (locationDenied) {
            GoldButton(
                text = stringResource(R.string.branches_enable_location),
                filled = false,
                big = false,
                onClick = onEnableLocation
            )
        }
    }
}

@Composable
private fun BranchCard(
    branch: Branch,
    arabic: Boolean,
    onClick: () -> Unit,
    onDirections: () -> Unit,
    onCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x66000000))
            .border(1.dp, GoldenBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            branch.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = branch.name(arabic),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                branch.city(arabic)?.let {
                    Text(text = it, color = Color(0xFFCCCCCC), fontSize = 12.sp)
                }
            }

            // المسافة بتظهر بس لما الترتيب بالأقرب شغال — من غير موقع
            // المستخدم الرقم ده مش موجود أصلاً في الرد
            branch.distanceKm?.let { km ->
                Text(
                    text = formatDistance(km),
                    color = GoldenBorder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        branch.address(arabic)?.let {
            Text(text = it, color = Color(0xFFCCCCCC), fontSize = 13.sp)
        }

        branch.workingHours(arabic)?.let {
            Text(text = it, color = Color(0xFF9E9E9E), fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (branch.hasLocation) {
                QuickAction(Icons.Filled.Directions, R.string.branch_directions, onDirections)
            }
            if (branch.phone != null) {
                QuickAction(Icons.Filled.Call, R.string.branch_call, onCall)
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    labelRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(GoldenBorder.copy(alpha = 0.15f))
            .border(1.dp, GoldenBorder.copy(alpha = 0.5f), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldenBorder,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(labelRes),
            color = GoldenBorder,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * المسافة بصيغة مقروءة.
 *
 * أقل من كيلومتر بيتعرض بالمتر: "٣٠٠ م" أوضح بكتير من "٠٫٣ كم"
 * للمستخدم اللي الفرع على بعد شارع منه.
 */
@Composable
private fun formatDistance(km: Double): String {
    val ctx = LocalContext.current
    val locale = ctx.resources.configuration.locales[0]

    return if (km < 1.0) {
        ctx.getString(R.string.branch_distance_m, String.format(locale, "%.0f", km * 1000))
    } else {
        ctx.getString(R.string.branch_distance_km, String.format(locale, "%.1f", km))
    }
}

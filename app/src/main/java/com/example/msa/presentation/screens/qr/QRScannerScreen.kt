package com.msa.android.presentation.screens.qr

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.msa.android.R
import com.msa.android.domain.model.BullionVerification
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.MSATopBar
import com.msa.android.presentation.theme.GoldenBorder
import java.util.concurrent.Executors

/**
 * QR scanner screen — 1:1 port of the iOS `ScanQRView`:
 *
 *     VStack {
 *         QRScannerView { code in vm.verify(code: code) }
 *             .frame(height: 500)
 *         Text(vm.result)
 *             .font(.title3)
 *             .padding()
 *     }
 *
 *   • Camera preview occupies the upper region.
 *   • A single `Text` widget directly underneath shows `vm.result`
 *     ("Valid QR" / "Invalid QR" / error.localizedDescription).
 *   • No dialog, no manual-entry sheet, no diagnostic block — just the camera
 *     and the result string, matching iOS exactly.
 */
@Composable
fun QRScannerScreen(
    onBack: () -> Unit,
    vm: ScanQRViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    MSABackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            MSATopBar(
                title = stringResource(R.string.qr_scanner_title),
                showBack = true,
                onBack = onBack
            )

            when {
                !hasCameraPermission -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        PermissionDeniedCard(onRetry = {
                            permLauncher.launch(Manifest.permission.CAMERA)
                        })
                    }
                }
                else -> {
                    val result = vm.result

                    /*
                     * الكاميرا بتقف مكانها لما نتيجة توصل.
                     *
                     * سيبنا المعاينة شغّالة ورا النتيجة كان معناه إن
                     * الكاميرا تفضل تلقط نفس الاستيكر وتبعت طلبات من
                     * ورا ظهر المستخدم وهو بيقرا الرد.
                     */
                    if (result == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                        ) {
                            CameraScanner(
                                onCodeDetected = vm::verify,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (vm.loading) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color(0xAA000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.qr_checking),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.qr_hint),
                            color = Color(0xFFBBBBBB),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        )
                    } else {
                        // رسالة طويلة من السيرفر + ٤ صفوف تفاصيل بيعدّوا
                        // ارتفاع شاشة صغيرة — من غير التمرير الزرار
                        // بيتقصّ ومفيش طريقة توصله
                        androidx.compose.foundation.rememberScrollState().let { scroll ->
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scroll)
                            ) {
                                ScanResultCard(
                                    result = result,
                                    onScanAgain = vm::clearResult,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Camera preview ───────────────────────────────────────────────────────

@Composable
private fun CameraScanner(
    onCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    /*
     * مراجع بنمسكها عشان نقدر نفكّها وقت الخروج.
     *
     * المعاينة بتخرج من التركيب أول ما نتيجة توصل (الكارت بياخد
     * مكانها)، بس دورة حياة الشاشة نفسها لسه RESUMED — يعني CameraX
     * بيفضل مربوط بيها والكاميرا شغّالة من غير أي معاينة ظاهرة،
     * والفريمات بتتبعت لمنفّذ متقفل.
     */
    val providerRef = remember { java.util.concurrent.atomic.AtomicReference<ProcessCameraProvider?>() }
    val analyzerRef = remember { java.util.concurrent.atomic.AtomicReference<QRCodeAnalyzer?>() }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { providerRef.get()?.unbindAll() }
            runCatching { analyzerRef.get()?.close() }
            executor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(1280, 720),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        val analyzer = QRCodeAnalyzer { code ->
                            // Bounce back to the main thread because the VM's
                            // state delegate updates Compose state.
                            previewView.post { onCodeDetected(code) }
                        }
                        analyzerRef.set(analyzer)
                        it.setAnalyzer(executor, analyzer)
                    }

                providerRef.set(cameraProvider)

                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

// ─── Permission denied state ──────────────────────────────────────────────

@Composable
private fun PermissionDeniedCard(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.qr_permission_needed),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.qr_permission_explanation),
            color = Color(0xFFBBBBBB),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldenBorder,
                contentColor = Color.Black
            )
        ) {
            Text(
                stringResource(R.string.grant_permission),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * كارت نتيجة المسح.
 *
 * ## اللون قبل الكلام
 *
 * المستخدم واقف في محل ماسك سبيكة — أول حاجة محتاج يعرفها هي
 * «ماشي ولا لأ»، والألوان بتوصّلها قبل ما يقرا أي كلمة. النص بيشرح
 * بعد كده.
 *
 * النصوص كلها جاية من السيرفر عشان أندرويد و iOS والويب يقولوا نفس
 * الكلام بالحرف — مش ترجمات متوازية بتفرق مع الوقت.
 */
@Composable
private fun ScanResultCard(
    result: BullionVerification,
    onScanAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (accent, glyph) = when (result.status) {
        BullionVerification.Status.VALID ->
            Color(0xFF4CAF50) to "✓"
        BullionVerification.Status.VOID ->
            Color(0xFFE8B138) to "!"
        BullionVerification.Status.UNKNOWN ->
            Color(0xFFE53935) to "✕"
        // فشل الاتصال — برتقالي، مش أحمر: مفيش حكم على السبيكة أصلاً
        BullionVerification.Status.ERROR ->
            Color(0xFFE67E22) to "!"
    }

    val isError =
        result.status == BullionVerification.Status.ERROR

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x14FFFFFF))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(2.dp, accent, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, color = accent, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isError) stringResource(R.string.qr_network_error_title) else result.title,
            color = accent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (isError) stringResource(R.string.qr_network_error_message) else result.message,
            color = Color(0xFFCCCCCC),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        // تفاصيل السبيكة للسليم بس
        result.details?.let { details ->
            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x40000000))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(stringResource(R.string.qr_detail_code), details.code)

                details.karat?.let { karat ->
                    // العيار مع اسم المعدن — «٢٤ دهب» مش «٢٤» لوحدها،
                    // زي ما صفحة التحقّق على الويب بتعرضها بالظبط
                    val metal = when (details.metal) {
                        "silver" -> stringResource(R.string.silver)
                        else     -> stringResource(R.string.gold)
                    }
                    DetailRow(stringResource(R.string.qr_detail_karat), "$karat $metal")
                }
                details.weightGrams?.let {
                    DetailRow(
                        stringResource(R.string.qr_detail_weight),
                        "${it.toString().trimEnd('0').trimEnd('.')} ${stringResource(R.string.qr_detail_grams)}"
                    )
                }
                details.serial?.let {
                    DetailRow(stringResource(R.string.qr_detail_serial), it)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onScanAgain,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldenBorder,
                contentColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.qr_scan_again), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF9E9E9E), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

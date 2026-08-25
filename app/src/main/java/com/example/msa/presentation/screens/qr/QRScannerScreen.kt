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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
                    // ── Camera preview — iOS .frame(height: 500) ──────────
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
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Result text — iOS Text(vm.result).font(.title3) ───
                    // Green for "Valid QR", red for "Invalid QR", amber for any
                    // network/error message. Matches iOS visual convention.
                    val resultColor = when {
                        vm.result.contains("Valid",   ignoreCase = true) -> Color(0xFF2ECC71)
                        vm.result.contains("Invalid", ignoreCase = true) -> Color(0xFFE74C3C)
                        vm.result.isNotEmpty()                            -> Color(0xFFE67E22)
                        else -> Color.Transparent
                    }
                    Text(
                        text = vm.result,
                        color = resultColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    // Tiny "scan another" hint — Android extra (iOS auto-resets
                    // because tap-to-verify means the user can just tap again).
                    if (vm.result.isNotEmpty()) {
                        Button(
                            onClick = vm::clearResult,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldenBorder,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                stringResource(R.string.qr_scan_again),
                                fontWeight = FontWeight.Bold
                            )
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

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
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
                        it.setAnalyzer(executor, QRCodeAnalyzer { code ->
                            // Bounce back to the main thread because the VM's
                            // state delegate updates Compose state.
                            previewView.post { onCodeDetected(code) }
                        })
                    }

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

package com.msa.android.presentation.screens.qr

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX analyzer that feeds frames into Google ML Kit's barcode scanner.
 * On every successful QR/barcode read it invokes [onCodeDetected] with the
 * payload string.
 *
 * Configured to detect QR codes only — narrows the model's work and reduces
 * false positives from packaging barcodes etc.
 */
class QRCodeAnalyzer(
    private val onCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer, AutoCloseable {

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { value ->
                    onCodeDetected(value)
                }
            }
            .addOnCompleteListener {
                // Always close the proxy — failing to do this stalls the
                // camera pipeline after a few frames.
                imageProxy.close()
            }
    }

    /**
     * تسريح قارئ ML Kit.
     *
     * الكلاس ده بيفتح عميل ML Kit في المنشئ، والعميل ده ماسك موارد
     * أصلية (native) مش بتتحرّر مع جامع القمامة. كل دورة «امسح →
     * نتيجة → امسح تاني» كانت بتعمل قارئ جديد وتسيب القديم مفتوح،
     * فالذاكرة الأصلية بتزيد مع كل مسحة.
     */
    override fun close() {
        runCatching { scanner.close() }
    }
}

package com.msa.android.presentation.screens.qr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.android.domain.repository.QRRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 1:1 port of the iOS `ScanQRViewModel`:
 *
 *     final class ScanQRViewModel: ObservableObject {
 *         @Published var result = ""
 *         func verify(code: String) {
 *             Task {
 *                 do {
 *                     guard let qr = try await repository.verifyQR(id: code) else {
 *                         result = "Invalid QR"; return
 *                     }
 *                     try await repository.markUsed(id: code)
 *                     result = "Valid QR"
 *                 } catch {
 *                     result = error.localizedDescription
 *                 }
 *             }
 *         }
 *     }
 *
 * Same exact branches and same exact strings. The only Android-specific bit is
 * the 2-second cool-down after a verify — the camera continuously detects QRs
 * frame after frame, so without it we'd flood Firestore with duplicate
 * verifyQR/markUsed calls. iOS uses tap-to-verify and never has this problem.
 */
@HiltViewModel
class ScanQRViewModel @Inject constructor(
    private val repository: QRRepository
) : ViewModel() {

    /** Result string — exactly mirrors iOS `@Published var result`. */
    var result by mutableStateOf("")
        private set

    /** Simple latch so we don't re-verify the same frame 30 times a second. */
    private var verifying = false

    fun verify(code: String) {
        if (verifying || code.isBlank()) return
        verifying = true

        viewModelScope.launch {
            try {
                val qr = repository.verifyQR(code)
                if (qr == null) {
                    // guard let qr = ... else { result = "Invalid QR"; return }
                    result = "Invalid QR"
                } else {
                    // iOS commented out the `qr.isUsed` check, so we mirror that.
                    repository.markUsed(code)
                    result = "Valid QR"
                }
            } catch (e: Exception) {
                // catch { result = error.localizedDescription }
                result = e.localizedMessage ?: e.javaClass.simpleName
            } finally {
                // Cool-down so the next frame doesn't immediately re-trigger.
                delay(2_000)
                verifying = false
            }
        }
    }

    /** Clear the displayed result (e.g. when the user wants to scan a new QR). */
    fun clearResult() {
        result = ""
    }
}

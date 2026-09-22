package com.msa.android.presentation.screens.home.banner

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

/**
 * فتح البانر بملء الشاشة — النسخة الأندرويد من `openImageViewer` /
 * `openVideoPlayer` في HomeVC+Banner.swift.
 *
 *  • الصورة: زووم من 1x لـ 6x بالإصبعين، ودبل تاب للتكبير/الرجوع.
 *  • الفيديو: بالصوت وبأزرار التحكم الكاملة (تشغيل/إيقاف/شريط تقدّم).
 *
 * الفرق عن الـ iOS إن هنا مفيش AVPlayerViewController جاهزة، فبنستخدم
 * PlayerView بالـ controller بتاعها وهي بتدي نفس الأزرار تقريباً.
 */
@Composable
fun BannerFullScreenViewer(
    item: Banner,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            when (item.mediaType) {
                BannerMediaType.IMAGE -> ZoomableImage(url = item.mediaUrl)
                BannerMediaType.VIDEO -> FullScreenVideo(url = item.mediaUrl)
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "إغلاق",
                    tint = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// الصورة بالزووم
// ─────────────────────────────────────────────────────────────

@Composable
private fun ZoomableImage(url: String) {

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        // نفس حدود الـ iOS: من 1x لـ 6x.
        scale = (scale * zoomChange).coerceIn(1f, 6f)

        // لما نرجع لـ 1x بنرجّع الصورة للنص بدل ما تفضل مزحلقة برة الشاشة.
        offset = if (scale <= 1f) Offset.Zero else offset + panChange
    }

    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 3f
                        }
                    }
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    )
}

// ─────────────────────────────────────────────────────────────
// الفيديو بملء الشاشة
// ─────────────────────────────────────────────────────────────

@OptIn(UnstableApi::class)
@Composable
private fun FullScreenVideo(url: String) {

    val context = LocalContext.current

    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))

            // على عكس البانر، هنا المستخدم طلب الفيديو بنفسه —
            // فالصوت شغال، وبناخد الفوكس الصوتي عشان أي صوت تاني يهدى.
            volume = 1f
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true
            )

            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        update = { view -> view.player = player },
        modifier = Modifier.fillMaxSize()
    )
}

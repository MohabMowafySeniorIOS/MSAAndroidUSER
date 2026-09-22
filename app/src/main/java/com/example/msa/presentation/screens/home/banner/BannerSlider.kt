package com.msa.android.presentation.screens.home.banner

import kotlin.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

/**
 * سلايدر بانرات الرئيسية — النسخة الأندرويد من BannerSliderView.swift.
 *
 * بيعمل بالظبط اللي الـ iOS بيعمله:
 *  • بيقلب أوتوماتيك: الصورة ٥ ثواني، والفيديو لحد ما يخلص (بحد أقصى ٢٠ ثانية).
 *  • الفيديو بيشتغل مكتوم و بيلوب، والغلاف بيفضل ظاهر تحته لحد أول فريم.
 *  • بيقف لما التطبيق يروح للخلفية أو المستخدم يسيب الشاشة.
 *  • بيقف مؤقتاً وهو بيتقلب بإيد المستخدم عشان ما نخطفهوش من تحت إيده.
 *  • الضغط بيفتح الصورة بالزووم أو الفيديو بملء الشاشة بالصوت.
 *  • لو مفيش بانرات، الكومبوزابل مبيرسمش حاجة والشاشة بتترص عادي.
 */

// ─────────────────────────────────────────────────────────────
// شريط اختياري جوه المحتوى (مش مستخدم دلوقتي — الـ popup هو الأساسي)
//
// سيبته موجود لو حبيت في أي وقت تعرض البانر كشريط عادي في الرئيسية
// بدل الـ popup أو جنبه. نادِها من HomeScreen كده:
//     HomeBannerSection(modifier = Modifier.padding(horizontal = 16.dp))
// ─────────────────────────────────────────────────────────────

@Composable
fun HomeBannerSection(
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    onOpenLink: (String) -> Unit = {},
    vm: BannerViewModel = hiltViewModel()
) {
    val items by vm.banners.collectAsStateWithLifecycle()

    // لو اللينك المفروض ياخد الأولوية على فتح الميديا، خلّي ده true.
    // (نفس `linkTakesPriority` في HomeVC+Banner.swift)
    val linkTakesPriority = false

    var fullScreenItem by remember { mutableStateOf<Banner?>(null) }

    // مفيش بانرات = مفيش مساحة محجوزة أصلاً ولا حتى المسافات اللي حواليه،
    // زي `banneView.isHidden = true` في الـ iOS.
    if (items.isEmpty()) return

    BannerSlider(
        items = items,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
            .height(height),
        // وقّف السلايدر وإحنا فاتحين الفل سكرين عشان ما يفضلش
        // بيقلّب ورا الشاشة ويستهلك داتا على الفاضي.
        paused = fullScreenItem != null,
        onTap = { item ->
            val link = item.linkUrl
            if (linkTakesPriority && !link.isNullOrBlank()) {
                onOpenLink(link)
            } else {
                fullScreenItem = item
            }
        }
    )

    fullScreenItem?.let { item ->
        BannerFullScreenViewer(
            item = item,
            onDismiss = { fullScreenItem = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// السلايدر نفسه
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerSlider(
    items: List<Banner>,
    modifier: Modifier = Modifier,
    paused: Boolean = false,
    imageDurationMs: Long = 5_000,
    maxVideoDurationMs: Long = 20_000,
    cornerRadius: Dp = 16.dp,
    onTap: (Banner) -> Unit = {},
    onPageChanged: (Banner) -> Unit = {}
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    // بنبلّغ اللي فوق بالبانر الظاهر دلوقتي، عشان زرار الـ CTA في الـ popup
    // يعرف يفتح لينك البانر الصح.
    LaunchedEffect(pagerState.currentPage, items) {
        items.getOrNull(pagerState.currentPage)?.let(onPageChanged)
    }

    // بنعرف إحنا ظاهرين ولا لأ من الـ lifecycle بدل ما نستنى الشاشة تتقفل.
    var isResumed by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        isResumed = true
        onPauseOrDispose { isResumed = false }
    }

    val active = isResumed && !paused

    // مدة كل فيديو أول ما نعرفها من المشغل — عشان القلبة تحصل بعد ما
    // الفيديو يخلص مش في نصه. المفتاح هو الـ URL مش الـ index عشان
    // ما يتلغبطش لو اللستة اتغيرت.
    val videoDurations = remember { mutableStateMapOf<String, Long>() }

    val currentItem = items.getOrNull(pagerState.currentPage)

    val currentDurationMs: Long = when {
        currentItem == null -> imageDurationMs
        currentItem.mediaType == BannerMediaType.VIDEO ->
            (videoDurations[currentItem.mediaUrl] ?: maxVideoDurationMs)
                .coerceAtMost(maxVideoDurationMs)
        else -> imageDurationMs
    }

    // التقليب الأوتوماتيك.
    // بنعيد جدولته لما: الصفحة تتغير، المدة تتعرف، الشاشة تظهر/تختفي،
    // أو المستخدم يمسك السلايدر بإيده.
    LaunchedEffect(
        pagerState.currentPage,
        pagerState.isScrollInProgress,
        currentDurationMs,
        active,
        items.size
    ) {
        if (items.size <= 1) return@LaunchedEffect
        if (!active) return@LaunchedEffect
        if (pagerState.isScrollInProgress) return@LaunchedEffect

        delay(currentDurationMs)

        val next = (pagerState.currentPage + 1) % items.size
        pagerState.animateScrollToPage(next)
    }

    // بنثبّت الاتجاه على LTR زي `semanticContentAttribute = .forceLeftToRight`
    // في الـ iOS، عشان الترتيب اللي جاي من الداشبورد يفضل هو المعتمد
    // ومايتقلبش في الواجهة العربية.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

        Box(modifier = modifier) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                val item = items[page]

                BannerPage(
                    item = item,
                    isActive = active && page == pagerState.currentPage,
                    cornerRadius = cornerRadius,
                    onDurationKnown = { durationMs ->
                        videoDurations[item.mediaUrl] = durationMs
                    },
                    onClick = { onTap(item) }
                )
            }

            if (items.size > 1) {
                BannerDots(
                    count = items.size,
                    current = pagerState.currentPage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// صفحة واحدة (صورة أو فيديو)
// ─────────────────────────────────────────────────────────────

@Composable
private fun BannerPage(
    item: Banner,
    isActive: Boolean,
    cornerRadius: Dp,
    onDurationKnown: (Long) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVideo = item.mediaType == BannerMediaType.VIDEO

    var videoReady by remember(item.mediaUrl) { mutableStateOf(false) }
    var durationMs by remember(item.mediaUrl) { mutableStateOf<Long?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerRadius))
            .background(BannerPlaceholder)
            .clickable(onClick = onClick)
    ) {

        // الغلاف. في حالة الفيديو بيفضل ظاهر تحته لحد ما أول فريم يترسم،
        // عشان ما يبانش مستطيل أسود لحظة التحميل.
        AsyncImage(
            model = item.thumbUrl ?: item.mediaUrl,
            contentDescription = item.name.ifEmpty { null },
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isVideo) {
            BannerVideo(
                mediaUrl = item.mediaUrl,
                isActive = isActive,
                onFirstFrame = { videoReady = true },
                onDurationKnown = {
                    durationMs = it
                    onDurationKnown(it)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // تدرّج أسود تحت عشان العنوان يبان فوق أي صورة.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.55f)
                    )
                )
        )

        if (item.name.isNotEmpty()) {
            Text(
                text = item.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
            )
        }

        if (isVideo) {

            // الشارة بتبان فوراً — حتى قبل ما الفيديو يحمّل — عشان المستخدم
            // يعرف من أول لحظة إن ده فيديو وإن الضغط هيفتحه بالصوت.
            VideoBadge(
                durationMs = durationMs,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            )

            // أيقونة كبيرة في النص — بتظهر بس والفيديو لسه بيحمّل.
            if (!videoReady) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// الفيديو المكتوم جوه السلايدر
// ─────────────────────────────────────────────────────────────

@OptIn(UnstableApi::class)
@Composable
private fun BannerVideo(
    mediaUrl: String,
    isActive: Boolean,
    onFirstFrame: () -> Unit,
    onDurationKnown: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var rendered by remember(mediaUrl) { mutableStateOf(false) }

    val player = remember(mediaUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            repeatMode = Player.REPEAT_MODE_ALL

            // مكتوم دايماً: البانر بيشتغل لوحده من غير ما المستخدم يطلبه،
            // فصوت فجأة حاجة مزعجة. الصوت بيشتغل في الفل سكرين بس.
            volume = 0f

            // handleAudioFocus = false ⇒ مش بناخد الفوكس الصوتي، يعني
            // الأغنية اللي المستخدم سامعها في تطبيق تاني مبتوقفش.
            // (نفس فكرة AVAudioSession .ambient في الـ iOS)
            setAudioAttributes(AudioAttributes.DEFAULT, false)

            playWhenReady = false
            prepare()
        }
    }

    DisposableEffect(player) {

        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val d = player.duration
                    if (d != C.TIME_UNSET && d > 0) onDurationKnown(d)
                }
            }

            override fun onRenderedFirstFrame() {
                rendered = true
                onFirstFrame()
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(isActive) {
        if (isActive) player.play() else player.pause()
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                this.player = player
            }
        },
        update = { view -> view.player = player },
        // شفاف لحد أول فريم عشان الغلاف اللي تحته يفضل باين.
        modifier = modifier.alpha(if (rendered) 1f else 0f)
    )
}

// ─────────────────────────────────────────────────────────────
// عناصر مساعدة
// ─────────────────────────────────────────────────────────────

@Composable
private fun VideoBadge(
    durationMs: Long?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )

        if (durationMs != null) {
            Text(
                text = formatDuration(durationMs),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BannerDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == current) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) BannerGold
                        else Color.White.copy(alpha = 0.45f)
                    )
            )
        }
    }
}

internal fun formatDuration(ms: Long): String {
    val total = (ms / 1000L).toInt()
    return "%d:%02d".format(total / 60, total % 60)
}

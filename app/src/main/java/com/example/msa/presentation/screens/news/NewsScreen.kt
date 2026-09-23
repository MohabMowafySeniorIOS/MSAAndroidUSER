package com.msa.android.presentation.screens.news

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.msa.android.R
import com.msa.android.domain.model.NewsItem
import com.msa.android.presentation.common.components.MSABackground
import com.msa.android.presentation.common.components.PullToRefreshContainer
import com.msa.android.presentation.common.components.SegmentedToggle
import com.msa.android.presentation.theme.GoldenBorder
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * News list screen.
 *   • NORMAL tab → tap → open the URL in browser.
 *   • MSA tab    → tap → open the image fullscreen with zoom.
 * Cards skip any missing title/description.
 */
@Composable
fun NewsScreen(
    onOpenImage: (String) -> Unit = {},
    vm: NewsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    MSABackground {
        PullToRefreshContainer(
            refreshing = state.isLoading,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            com.msa.android.presentation.common.components.MSATopBar(
                title = stringResource(R.string.news),
                showBack = false,
                showShare = true,
                onShare = { com.msa.android.presentation.common.shareApp(context) }
            )

            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                SegmentedToggle(
                    selected = state.tab,
                    options = listOf(
                        NewsTab.NORMAL to stringResource(R.string.news_tab_normal),
                        NewsTab.MSA    to stringResource(R.string.news_tab_msa)
                    ),
                    onSelect = vm::setTab
                )
            }

            when {
                state.isLoading && state.articles.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = GoldenBorder)
                    }
                }
                state.error != null && state.articles.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text(stringResource(R.string.error_loading), color = Color(0xFFE0E0E0))
                    }
                }
                state.articles.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text(stringResource(R.string.no_news), color = Color(0xFFE0E0E0))
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 8.dp,    bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.articles, key = { it.id }) { article ->
                            NewsCard(
                                article = article,
                                onClick = {
                                    if (state.tab == NewsTab.MSA) {
                                        val img = article.urlToImage
                                        if (!img.isNullOrBlank()) onOpenImage(img)
                                    } else {
                                        val url = article.url
                                        if (!url.isNullOrBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun NewsCard(article: NewsItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xCC1C1C1E))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        if (!article.urlToImage.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.urlToImage)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2C))
            ) {
                when (painter.state) {
                    is coil.compose.AsyncImagePainter.State.Loading -> Box(
                        Modifier.fillMaxSize(), Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = GoldenBorder,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    is coil.compose.AsyncImagePainter.State.Error -> Box(
                        Modifier.fillMaxSize(), Alignment.Center
                    ) { Text("📰", fontSize = 40.sp) }
                    else -> SubcomposeAsyncImageContent()
                }
            }
        }

        if (!article.title.isNullOrBlank()) {
            if (!article.urlToImage.isNullOrBlank()) Spacer(Modifier.height(10.dp))
            Text(
                text = article.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!article.description.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = article.description,
                color = Color(0xFFB0B0B0),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        val formatted = formatPublishedAt(article.publishedAt)
        if (formatted.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(text = formatted, color = GoldenBorder, fontSize = 12.sp)
        }
    }
}

private fun formatPublishedAt(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault())
            .format(java.util.Date.from(odt.toInstant()))
    } catch (e: Exception) {
        try {
            val legacy = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = legacy.parse(iso) ?: return ""
            SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault()).format(date)
        } catch (_: Exception) { "" }
    }
}

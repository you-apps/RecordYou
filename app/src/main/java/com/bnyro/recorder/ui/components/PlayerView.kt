package com.bnyro.recorder.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.models.PlayerModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerView(
    showVideoModeInitially: Boolean
) {
    val playerModel: PlayerModel = viewModel(factory = PlayerModel.Factory)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val pagerState = rememberPagerState(
            initialPage = if (showVideoModeInitially) 1 else 0,
            initialPageOffsetFraction = 0f
        ) {
            2
        }
        val scope = rememberCoroutineScope()
        TabRow(selectedTabIndex = pagerState.currentPage, Modifier.fillMaxWidth()) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            ) {
                Text(
                    stringResource(R.string.audio),
                    Modifier.padding(10.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.video),
                    Modifier.padding(10.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            when (index) {
                0 -> RecordingItemList(
                    items = playerModel.audioRecordingItems,
                    isVideoList = false
                )

                1 -> RecordingItemList(
                    items = playerModel.screenRecordingItems,
                    isVideoList = true
                )
            }
        }
    }
}

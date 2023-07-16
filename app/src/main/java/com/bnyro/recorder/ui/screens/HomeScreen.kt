package com.bnyro.recorder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.RecorderState
import com.bnyro.recorder.enums.RecorderType
import com.bnyro.recorder.ui.Destination
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.models.RecorderModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    initialRecorder: RecorderType,
    onNavigate: (Destination) -> Unit,
    recorderModel: RecorderModel = viewModel()
) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
            ClickableIcon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings)
            ) {
                onNavigate(Destination.Settings)
            }
            ClickableIcon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = stringResource(R.string.recordings)
            ) {
                onNavigate(Destination.RecordingPlayer)
            }
        })
    }, bottomBar = {
        Column {
            AnimatedVisibility(recorderModel.recorderState == RecorderState.IDLE) {
                NavigationBar {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(
                                    id = R.string.record_sound
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.record_sound)) },
                        selected = (pagerState.currentPage == 0),
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = stringResource(
                                    id = R.string.record_screen
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.record_screen)) },
                        selected = (pagerState.currentPage == 1),
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                }
            }
        }
    }) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { index ->
                RecorderView(initialRecorder = initialRecorder, recordScreenMode = (index == 1))
            }
        }
    }
}

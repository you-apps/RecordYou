package com.bnyro.recorder.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.components.PlayerView
import com.bnyro.recorder.ui.dialogs.ConfirmationDialog
import com.bnyro.recorder.ui.models.PlayerModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    showVideoModeInitially: Boolean
) {
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    var selectedSortOrder by remember {
        mutableStateOf(SortOrder.ALPHABETIC)
    }
    val playerModel: PlayerModel = viewModel(factory = PlayerModel.Factory)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.recordings)) },
                actions = {
                    Box {
                        var showDropDown by remember {
                            mutableStateOf(false)
                        }
                        ClickableIcon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = stringResource(R.string.sort)
                        ) {
                            showDropDown = true
                        }

                        val sortOptions = listOf(
                            SortOrder.MODIFIED to R.string.modified,
                            SortOrder.MODIFIED_REV to R.string.modified_rev,
                            SortOrder.ALPHABETIC to R.string.alphabetic,
                            SortOrder.ALPHABETIC_REV to R.string.alphabetic_rev,
                            SortOrder.SIZE to R.string.size,
                            SortOrder.SIZE_REV to R.string.size_rev
                        )
                        DropdownMenu(showDropDown, { showDropDown = false }) {
                            sortOptions.forEach { sortOrder ->
                                DropdownMenuItem(
                                    text = {
                                        Text(stringResource(sortOrder.second))
                                    },
                                    onClick = {
                                        selectedSortOrder = sortOrder.first
                                        playerModel.sortItems(sortOrder.first)
                                        showDropDown = false
                                    }
                                )
                            }
                        }
                    }
                    if (playerModel.selectedFiles.isNotEmpty()) {
                        val selectedAll =
                            playerModel.selectedFiles.size == playerModel.audioRecordingItems.size + playerModel.screenRecordingItems.size
                        Checkbox(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            checked = selectedAll,
                            onCheckedChange = {
                                if (selectedAll) {
                                    playerModel.selectedFiles = listOf()
                                } else {
                                    playerModel.selectedFiles =
                                        playerModel.screenRecordingItems + playerModel.audioRecordingItems
                                }
                            }
                        )
                    }
                    ClickableIcon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_all)
                    ) {
                        showDeleteDialog = true
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            PlayerView(
                showVideoModeInitially
            )
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = if (playerModel.selectedFiles.isEmpty()) R.string.delete_all else R.string.delete,
            onDismissRequest = { showDeleteDialog = false }
        ) {
            playerModel.deleteFiles()
        }
    }
}

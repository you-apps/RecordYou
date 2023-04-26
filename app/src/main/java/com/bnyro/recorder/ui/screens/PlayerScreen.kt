package com.bnyro.recorder.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.recorder.R
import com.bnyro.recorder.enums.SortOrder
import com.bnyro.recorder.ui.common.ClickableIcon
import com.bnyro.recorder.ui.common.FullscreenDialog
import com.bnyro.recorder.ui.components.PlayerView
import com.bnyro.recorder.ui.models.PlayerModel

@Composable
fun PlayerScreen(
    showVideoModeInitially: Boolean,
    onDismissRequest: () -> Unit
) {
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    var selectedSortOrder by remember {
        mutableStateOf(SortOrder.ALPHABETIC)
    }
    val selectedFiles = remember {
        mutableStateOf(listOf<DocumentFile>())
    }
    val playerModel: PlayerModel = viewModel()

    FullscreenDialog(
        title = stringResource(R.string.recordings),
        onDismissRequest = onDismissRequest,
        useLargeAppBar = true,
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
                    SortOrder.ALPHABETIC,
                    SortOrder.ALPHABETIC_REV,
                    SortOrder.SIZE,
                    SortOrder.SIZE_REV
                )
                val sortOptionNames = listOf(
                    R.string.alphabetic,
                    R.string.alphabetic_rev,
                    R.string.size,
                    R.string.size_rev
                )
                DropdownMenu(showDropDown, { showDropDown = false }) {
                    sortOptions.forEachIndexed { index, sortOrder ->
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(sortOptionNames[index]))
                            },
                            onClick = {
                                selectedSortOrder = sortOrder
                                showDropDown = false
                            }
                        )
                    }
                }
            }
            if (selectedFiles.value.isNotEmpty()) {
                val selectedAll = selectedFiles.value.size == playerModel.files.size
                Checkbox(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    checked = selectedAll,
                    onCheckedChange = {
                        if (selectedAll) {
                            selectedFiles.value = listOf()
                        } else {
                            selectedFiles.value = playerModel.files
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
        }
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp
            )
        ) {
            PlayerView(showVideoModeInitially, showDeleteDialog, selectedSortOrder, selectedFiles) {
                showDeleteDialog = false
            }
        }
    }
}

package com.bnyro.recorder.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bnyro.recorder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenDialog(
    title: String = "",
    onDismissRequest: () -> Unit,
    useLargeAppBar: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState()
        )

        Scaffold(
            modifier = if (useLargeAppBar) {
                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            } else {
                Modifier
            },
            topBar = {
                if (useLargeAppBar) {
                    LargeTopAppBar(
                        title = {
                            Text(title)
                        },
                        navigationIcon = {
                            ClickableIcon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            ) {
                                onDismissRequest.invoke()
                            }
                        },
                        actions = actions,
                        scrollBehavior = scrollBehavior
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(title)
                        },
                        navigationIcon = {
                            ClickableIcon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            ) {
                                onDismissRequest.invoke()
                            }
                        },
                        actions = actions
                    )
                }
            }
        ) { pV ->
            Box(
                modifier = Modifier.padding(pV)
            ) {
                content.invoke()
            }
        }
    }
}

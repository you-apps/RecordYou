package com.bnyro.recorder.ui.dialogs

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.common.DialogButton

@Composable
fun ConfirmationDialog(
    @StringRes title: Int,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(title))
        },
        text = {
            Text(stringResource(R.string.irreversible))
        },
        confirmButton = {
            DialogButton(stringResource(R.string.okay)) {
                onConfirm.invoke()
                onDismissRequest.invoke()
            }
        },
        dismissButton = {
            DialogButton(stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        }
    )
}

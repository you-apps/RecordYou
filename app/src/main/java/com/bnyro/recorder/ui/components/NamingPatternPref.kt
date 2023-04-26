package com.bnyro.recorder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.recorder.R
import com.bnyro.recorder.ui.common.DialogButton
import com.bnyro.recorder.util.Preferences
import com.bnyro.recorder.util.StorageHelper

@Composable
fun NamingPatternPref() {
    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                showDialog = true
            }
            .padding(horizontal = 2.dp, vertical = 5.dp)
    ) {
        Text(
            text = stringResource(R.string.naming_pattern),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.naming_pattern_desc),
            fontSize = 12.sp
        )
    }

    if (showDialog) {
        var value by remember {
            mutableStateOf(
                Preferences.getString(
                    Preferences.namingPatternKey,
                    StorageHelper.DEFAULT_NAMING_PATTERN
                )
            )
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { stringResource(R.string.naming_pattern) },
            text = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(R.string.naming_pattern)) }
                )
            },
            confirmButton = {
                DialogButton(stringResource(R.string.okay)) {
                    Preferences.edit { putString(Preferences.namingPatternKey, value) }
                    showDialog = false
                }
            },
            dismissButton = {
                DialogButton(stringResource(R.string.cancel)) {
                    showDialog = false
                }
            }
        )
    }
}

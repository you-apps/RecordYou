package com.bnyro.recorder.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.recorder.R

@Composable
fun SelectionDialog(
    onDismissRequest: () -> Unit,
    title: String,
    entries: List<String>,
    onSelect: (index: Int) -> Unit
) {
    AlertDialog(
        title = { Text(title) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(stringResource(R.string.cancel)) {
                onDismissRequest.invoke()
            }
        },
        text = {
            LazyColumn {
                itemsIndexed(entries) { index, entry ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onSelect.invoke(index)
                                onDismissRequest.invoke()
                            }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        text = entry
                    )
                }
            }
        }
    )
}

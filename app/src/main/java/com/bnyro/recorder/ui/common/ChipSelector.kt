package com.bnyro.recorder.ui.common

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipSelector(
    title: String? = null,
    entries: List<String>,
    values: List<Int>,
    selections: List<Int>,
    onSelectionChanged: (Int, Boolean) -> Unit
) {
    val view = LocalView.current
    title?.let {
        Text(
            text = it,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.semantics { heading() }
        )
    }
    Spacer(modifier = Modifier.height(5.dp))
    LazyRow(
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(entries) { index, entry ->
            ElevatedFilterChip(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clearAndSetSemantics { // We need to suppress calculated contentDescription that contains word selected or not selected based off of the selected state
                        contentDescription = entry
                        selected = selections.contains(values[index])
                    },
                label = {
                    Text(entry)
                },
                selected = selections.contains(values[index]),
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onSelectionChanged(index, !selections.contains(values[index]))
                },
                leadingIcon = {
                    if (selections.contains(values[index])) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.scale(0.6f)
                        )
                    }
                }
            )
        }
    }
}

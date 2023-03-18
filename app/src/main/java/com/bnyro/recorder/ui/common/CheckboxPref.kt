package com.bnyro.recorder.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bnyro.recorder.util.Preferences

@Composable
fun CheckboxPref(
    prefKey: String,
    title: String,
    summary: String? = null,
    defaultValue: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    var checked by remember {
        mutableStateOf(
            Preferences.prefs.getBoolean(prefKey, defaultValue)
        )
    }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 10.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                checked = !checked
                Preferences.edit { putBoolean(prefKey, checked) }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(title)
                if (summary != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = summary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = {
                checked = it
                Preferences.edit { putBoolean(prefKey, it) }
                onCheckedChange.invoke(it)
            }
        )
    }
}

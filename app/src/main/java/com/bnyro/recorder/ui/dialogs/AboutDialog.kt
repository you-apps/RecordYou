package com.bnyro.recorder.ui.dialogs

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bnyro.recorder.BuildConfig
import com.bnyro.recorder.R
import com.bnyro.recorder.obj.AboutItem
import com.bnyro.recorder.ui.common.DialogButton
import com.bnyro.recorder.util.IntentHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val actions = listOf(
        AboutItem(R.string.source_code, Icons.Default.Code, "https://github.com/Bnyro/RecordYou"),
        AboutItem(R.string.author, Icons.Default.Person, "https://github.com/Bnyro"),
        AboutItem(
            R.string.translation,
            Icons.Default.Translate,
            "https://hosted.weblate.org/projects/you-apps/record-you"
        )
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.about)) },
        confirmButton = {
            DialogButton(stringResource(R.string.okay)) {
                onDismissRequest.invoke()
            }
        },
        text = {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                    Text(stringResource(R.string.app_name))
                    Badge(
                        modifier = Modifier.padding(end = 15.dp, start = 8.dp)
                    ) {
                        Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    }
                }
                actions.forEach {
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            IntentHelper.openHref(context, it.url ?: return@OutlinedButton)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(it.icon, null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stringResource(it.title))
                        }
                    }
                }
            }
        }
    )
}

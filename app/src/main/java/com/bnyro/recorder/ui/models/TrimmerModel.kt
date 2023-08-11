package com.bnyro.recorder.ui.models

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bnyro.recorder.App
import com.bnyro.recorder.util.MediaTrimmer
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.coroutines.launch

class TrimmerModel(context: Context) : ViewModel() {

    val player = ExoPlayer.Builder(context).build()

    var startTimeStamp by mutableLongStateOf(0L)
    var endTimeStamp by mutableStateOf<Long?>(null)

    fun startTrimmer(context: Context, inputFile: DocumentFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModelScope.launch {
                val trimmer = MediaTrimmer()
                Toast.makeText(context, "Starting Trimmer", Toast.LENGTH_LONG).show()
                val result = trimmer.trimMedia(context, inputFile, startTimeStamp, endTimeStamp!!)
                if (result) {
                    Toast.makeText(context, "Trim Success", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Trim Failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application =
                    (this[APPLICATION_KEY] as App)
                TrimmerModel(application)
            }
        }
    }
}

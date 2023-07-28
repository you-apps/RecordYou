package com.bnyro.recorder

import android.content.Context
import com.bnyro.recorder.util.FileRepository
import com.bnyro.recorder.util.FileRepositoryImpl

class AppContainer(context: Context) {
    val fileRepository: FileRepository by lazy {
        FileRepositoryImpl(context)
    }
}

package com.bnyro.recorder.util

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class CustomViewModelStoreOwner:ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()

}
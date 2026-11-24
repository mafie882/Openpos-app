package com.openpos.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.openpos.app.OpenPosApp

/** Tiny generic factory so view models can take the repository in their constructor. */
class SimpleViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}

@Composable
fun openPosApp(): OpenPosApp {
    val context = LocalContext.current.applicationContext
    return context as OpenPosApp
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(noinline creator: (OpenPosApp) -> VM): VM {
    val app = openPosApp()
    return viewModel(factory = SimpleViewModelFactory { creator(app) })
}

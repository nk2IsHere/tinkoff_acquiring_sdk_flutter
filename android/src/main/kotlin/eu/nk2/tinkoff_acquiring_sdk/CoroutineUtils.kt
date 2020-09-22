package eu.nk2.tinkoff_acquiring_sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

fun CoroutineScope.doOnMain(block: () -> Unit) {
    this.launch(Dispatchers.Main) {
        block.invoke()
    }
}

fun CoroutineScope.doOnBackground(block: () -> Unit) {
    this.launch(IO) {
        block.invoke()
    }
}
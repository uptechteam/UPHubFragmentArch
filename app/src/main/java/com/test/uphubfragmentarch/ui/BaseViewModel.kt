package com.test.uphubfragmentarch.ui

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext


abstract class BaseViewModel : ViewModel(), CoroutineScope, Observable {
    private val callbacks = PropertyChangeRegistry()

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyChange(this, fieldId)
    }
}
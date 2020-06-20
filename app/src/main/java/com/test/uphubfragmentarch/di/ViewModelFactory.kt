package com.test.uphubfragmentarch.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.MapKey
import javax.inject.Provider
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val viewModels: Map<Class<out ViewModel>, Provider<out ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return viewModels[modelClass]?.get() as T
    }
}

@Suppress("UNCHECKED_CAST")
class SingleViewModelFactory(private val viewModel: Provider<out ViewModel>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return viewModel.get() as T
    }
}

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

typealias ViewModelMap = MutableMap<Class<out ViewModel>, Provider<ViewModel>>
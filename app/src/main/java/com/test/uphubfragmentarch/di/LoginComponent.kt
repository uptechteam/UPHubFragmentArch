package com.test.uphubfragmentarch.di

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.test.uphubfragmentarch.domain.DefaultLoginInteractor
import com.test.uphubfragmentarch.domain.LoginInteractor
import com.test.uphubfragmentarch.ui.login.LoginViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.*
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

@LoginScope
@Subcomponent(modules = [LoginComponent.LoginModule::class])
abstract class LoginComponent {

    @Login
    abstract fun vmFactory(): ViewModelProvider.Factory

    companion object INSTANCE : CoroutineScope {
        @Volatile
        private var instance: LoginComponent? = null
        private var job = SupervisorJob().also { it.cancel() }
        private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e(LoginComponent::class.simpleName, throwable.message, throwable)
        }

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Default + job + exceptionHandler

        operator fun invoke(): LoginComponent {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        job = SupervisorJob()
                        instance = AppComponent.createLoginComponent()
                    }
                }
            }
            return instance!!
        }

        fun kill() {
            instance = null
            if (job.isActive) {
                job.cancel(CancellationException("Component was killed"))
            }
        }
    }

    @Subcomponent.Factory
    interface Factory {
        fun create(): LoginComponent
    }

    @Module
    abstract class LoginModule {

        @Binds
        @LoginScope
        abstract fun bind(loginInteractor: DefaultLoginInteractor): LoginInteractor

        companion object {
            @Provides
            @LoginScope
            @Login
            fun provide(viewModel: Provider<LoginViewModel>): ViewModelProvider.Factory =
                SingleViewModelFactory(viewModel)

            @Provides
            @Login
            fun provideCoroutineScore(): CoroutineScope = INSTANCE
        }
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Login
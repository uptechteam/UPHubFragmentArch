package com.test.uphubfragmentarch.di

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.test.uphubfragmentarch.data.db.Database
import com.test.uphubfragmentarch.data.repository.DefaultReposRepository
import com.test.uphubfragmentarch.data.repository.DefaultUserRepository
import com.test.uphubfragmentarch.data.repository.ReposRepository
import com.test.uphubfragmentarch.data.repository.UsersRepository
import com.test.uphubfragmentarch.ui.main.MainViewModel
import com.test.uphubfragmentarch.ui.main.navigation.DefaultMainCoordinator
import com.test.uphubfragmentarch.ui.main.navigation.MainCoordinator
import com.test.uphubfragmentarch.ui.main.profile.ProfileViewModel
import com.test.uphubfragmentarch.ui.main.profile.followers.FollowersViewModel
import com.test.uphubfragmentarch.ui.main.profile.repository.ProfileRepositoryViewModel
import com.test.uphubfragmentarch.ui.main.profile.subscriptions.SubscriptionViewModel
import com.test.uphubfragmentarch.ui.main.repository.RepositoryViewModel
import com.test.uphubfragmentarch.ui.main.user.UserCoordinator
import com.test.uphubfragmentarch.ui.main.user.UserViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoMap
import kotlinx.coroutines.*
import javax.inject.Qualifier
import javax.inject.Scope
import kotlin.coroutines.CoroutineContext

@Subcomponent(modules = [MainComponent.MainModule::class])
@MainScope
abstract class MainComponent {
    @Main
    abstract fun vmFactory() : ViewModelProvider.Factory
    abstract val mainCoordinator: MainCoordinator

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    companion object INSTANCE : CoroutineScope {

        private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e(MainComponent::class.simpleName, throwable.message, throwable)
        }

        @Volatile
        private var instance: MainComponent? = null
        private var job = SupervisorJob().also { it.cancel() }

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Default + job + exceptionHandler

        operator fun invoke(): MainComponent {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        job = SupervisorJob()
                        instance = AppComponent.createMainComponent()
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

    @Module(includes = [ViewModelModule::class])
    abstract class MainModule {

        @Binds
        @MainScope
        abstract fun bindMainCoordinator(defaultMainCoordinator: DefaultMainCoordinator): MainCoordinator

        @Binds
        @MainScope
        abstract fun bindUserCoordinator(mainCoordinator: MainCoordinator): UserCoordinator

        @Binds
        @MainScope
        abstract fun bindUserRepo(userRepository: DefaultUserRepository): UsersRepository

        @Binds
        @MainScope
        abstract fun bindRepoRepo(reposRepository: DefaultReposRepository): ReposRepository

        companion object {

            @Provides
            @Main
            fun provideCoroutineScope(): CoroutineScope = INSTANCE

            @Provides
            @MainScope
            fun provideRepositoryDao(db: Database) = db.repositoryDao()

            @Provides
            @MainScope
            fun provideUserDao(db: Database) = db.userDao()
        }
    }

    @Module
    abstract class ViewModelModule {
        @Binds
        @IntoMap
        @ViewModelKey(ProfileViewModel::class)
        abstract fun bindProfile(mainViewModel: ProfileViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(SubscriptionViewModel::class)
        abstract fun bindSubs(mainViewModel: SubscriptionViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(ProfileRepositoryViewModel::class)
        abstract fun bindUserRepos(mainViewModel: ProfileRepositoryViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(FollowersViewModel::class)
        abstract fun bindUserFollowers(mainViewModel: FollowersViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(MainViewModel::class)
        abstract fun bindMain(mainViewModel: MainViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(UserViewModel::class)
        abstract fun bindUser(hostViewModel: UserViewModel): ViewModel

        @Binds
        @IntoMap
        @ViewModelKey(RepositoryViewModel::class)
        abstract fun bindRepository(mainViewModel: RepositoryViewModel): ViewModel

        companion object {
            @Provides
            @MainScope
            @Main
            fun provide(viewModels: ViewModelMap): ViewModelProvider.Factory =
                ViewModelFactory(viewModels)
        }
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Main

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MainScope
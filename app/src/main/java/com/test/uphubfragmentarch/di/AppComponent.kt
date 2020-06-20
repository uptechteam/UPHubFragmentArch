package com.test.uphubfragmentarch.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.test.uphubfragmentarch.data.AndroidResources
import com.test.uphubfragmentarch.data.Resources
import com.test.uphubfragmentarch.data.UserPreferences
import com.test.uphubfragmentarch.data.db.Database
import com.test.uphubfragmentarch.data.remote.AuthInterceptor
import com.test.uphubfragmentarch.data.remote.CallAdapterFactory
import com.test.uphubfragmentarch.data.remote.GithubService
import com.test.uphubfragmentarch.ui.host.HostActivity
import com.test.uphubfragmentarch.ui.host.HostViewModel
import dagger.*
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Singleton
@Component(
    modules = [
        AppModule::class
    ]
)
abstract class AppComponent : Application.ActivityLifecycleCallbacks {

    abstract val vmFactory: ViewModelProvider.Factory
    abstract val loginComponentFactory: LoginComponent.Factory
    abstract val mainComponentFactory: MainComponent.Factory

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        when (activity) {
            is HostActivity -> {
                val vm = ViewModelProvider(activity, vmFactory)[HostViewModel::class.java]
                val injector = HostInjector(vmFactory, activity)
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(injector, true)
                activity.initActivity(vm)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    companion object INSTANCE : CoroutineScope {
        private val TAG = AppComponent::class.java.name

        private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, throwable.message, throwable)
        }
        private var job: Job = SupervisorJob().also { it.cancel() }

        @Volatile
        private var instance: AppComponent? = null

        override val coroutineContext: CoroutineContext
            get() {
                if (job.isCancelled) {
                    Log.e(TAG, "", ScopeCancelledException(AppComponent::class.java.simpleName))
                }
                return Dispatchers.Default + job + exceptionHandler
            }

        operator fun invoke(context: Context): AppComponent {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        job = SupervisorJob()
                        instance = DaggerAppComponent.factory().create(context)
                    }
                }
            }
            return instance!!
        }

        fun kill() {
            instance = null
            LoginComponent.kill()
            MainComponent.kill()
            if (job.isActive) {
                job.cancel(CancellationException("Component was killed"))
            }
        }

        fun createMainComponent(): MainComponent {
            return instance!!.mainComponentFactory.create()
        }

        fun createLoginComponent(): LoginComponent {
            return instance!!.loginComponentFactory.create()
        }
    }

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}

class ScopeCancelledException(name: String) :
    Throwable("Scope $name has been cancelled or not yet created")

@Module(includes = [DataModule::class, DomainModule::class])
abstract class AppModule {

    companion object {

        @Provides
        @Singleton
        fun provideVmFactory(viewModel: Provider<HostViewModel>): ViewModelProvider.Factory =
            SingleViewModelFactory(viewModel)

        @Provides
        fun provideCoroutineScope(): CoroutineScope = AppComponent.INSTANCE
    }

}

@Module
abstract class DataModule {

    @Binds
    @Singleton
    @IntoMap
    @StringKey("auth")
    abstract fun bind(authInterceptor: AuthInterceptor): Interceptor

    @Binds
    @Singleton
    abstract fun bindAndroidResources(androidResources: AndroidResources): Resources


    companion object {

        @Provides
        @Singleton
        fun provideDatabase(context: Context): Database =
            Room.databaseBuilder(
                context,
                Database::class.java, "UPHUB"
            )
                .fallbackToDestructiveMigration()
                .build()


        @Provides
        @Singleton
        fun provide(context: Context) = UserPreferences(context)

        @Provides
        @Singleton
        @IntoMap
        @StringKey("log")
        fun provideLoggingInterceptor(): Interceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        @Provides
        @Singleton
        fun provideOkHttpClient(
            interceptors: MutableMap<String, Provider<Interceptor>>
        ): OkHttpClient =
            with(OkHttpClient.Builder()) {
                addNetworkInterceptor(StethoInterceptor())
                interceptors.forEach { (_, v) ->
                    addInterceptor(v.get())
                }
                build()
            }

        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()

        @Provides
        @Singleton
        fun provideGithubService(
            okhttpClient: OkHttpClient,
            gson: Gson,
            callAdapterFactory: CallAdapterFactory
        ) = Retrofit.Builder()
            .baseUrl(GithubService.ENDPOINT)
            .client(okhttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(callAdapterFactory)
            .build()
            .create(GithubService::class.java)

    }
}

@Module
abstract class DomainModule {


}


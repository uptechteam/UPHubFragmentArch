package com.test.uphubfragmentarch.data.remote

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallAdapterFactory @Inject constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {

        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                "Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>"
            )
        }
        val responseType = getParameterUpperBound(0, returnType)

        val rawObservableType = getRawType(responseType)

        return if (rawObservableType == Response::class.java) {
            if (responseType !is ParameterizedType) {
                throw IllegalStateException(
                    "Response must be parameterized as Response<Foo> or Response<out Foo>"
                )
            }
            ResponseCallAdapter<Any>(
                getParameterUpperBound(
                    0,
                    responseType
                )
            )
        } else {
            DefaultCallAdapter<Any>(responseType)
        }
    }

    private class ResponseCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Deferred<Response<T>>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Deferred<Response<T>> {
            val deferred = CompletableDeferred<Response<T>>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(
                    call: Call<T>,
                    t: Throwable
                ) {
                    deferred.completeExceptionally(t)
                }

                override fun onResponse(
                    call: Call<T>,
                    response: Response<T>
                ) {
                    deferred.complete(response)
                }
            })

            return deferred
        }
    }

    private class DefaultCallAdapter<R>(
        private val responseType: Type
    ) : CallAdapter<R, Deferred<R>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<R>): Deferred<R> {

            val deferred = CompletableDeferred<R>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<R> {
                override fun onFailure(
                    call: Call<R>,
                    t: Throwable
                ) {
                    deferred.completeExceptionally(t)
                }

                override fun onResponse(
                    call: Call<R>,
                    response: Response<R>
                ) {
                    ApiException.get(response)?.let {
                        deferred.completeExceptionally(it)
                    } ?: kotlin.run {
                        deferred.complete(response.body()!!)
                    }
                }
            })

            return deferred
        }
    }
}


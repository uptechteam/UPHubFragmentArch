package com.test.uphubfragmentarch.domain

sealed class Result<R, E> {
    data class Success<R, E>(val result: R) : Result<R, E>()

    data class Error<R, E>(val error: E) : Result<R, E>()
}
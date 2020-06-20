package com.test.uphubfragmentarch.data.remote

import retrofit2.HttpException
import retrofit2.Response

open class ApiException(private val response: Response<*>) : HttpException(response) {
    override fun toString(): String {
        return "${this.javaClass.name} : \ncode: ${response.code()} \nmessage: ${response.message()}"
    }

    companion object {
        fun get(response: Response<*>): ApiException? {
            if (response.isSuccessful) return null
            return when {
                response.code() == 400 -> BadRequestException(response)
                response.code() == 401 -> UnauthorizedException(response)
                response.code() == 403 -> ForbiddenException(response)
                response.code() == 404 -> NotFoundException(response)
                response.code() == 409 -> ConflictException(response)
                response.code() == 429 -> TooManyRequests(response)
                response.code() / 500 == 1 -> InternalServerException(response)
                else -> UnknownApiException(response)
            }
        }
    }
}

open class BadRequestException(response: Response<*>) : ApiException(response)
open class NotFoundException(response: Response<*>) : ApiException(response)
open class UnauthorizedException(response: Response<*>) : ApiException(response)
open class ConflictException(response: Response<*>) : ApiException(response)
open class InternalServerException(response: Response<*>) : ApiException(response)
open class UnknownApiException(response: Response<*>) : ApiException(response)
open class ForbiddenException(response: Response<*>) : ApiException(response)
open class TooManyRequests(response: Response<*>) : ApiException(response)


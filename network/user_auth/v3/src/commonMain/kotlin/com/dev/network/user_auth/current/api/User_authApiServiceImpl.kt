package com.dev.network.user_auth.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.call.safeCall
import com.dev.network.user_auth.current.dto.AuthBody
import com.dev.network.user_auth.current.dto.AuthUserDto
import com.dev.network.user_auth.current.dto.ChangePasswordDto
import com.dev.network.user_auth.current.dto.GuestAuthDto
import com.dev.network.user_auth.current.dto.RefreshSessionDto
import com.dev.network.user_auth.current.dto.RegisterAuthUserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlin.Unit

class User_authApiServiceImpl(
  private val client: HttpClient,
) : User_authApiService {
  override suspend fun changePassword(body: ChangePasswordDto): NetworkResult<Unit> = safeCall {
    client.post("/auth/change-password") {
      setBody(body)
    }
    .let { Unit }
  }

  override suspend fun authAsGuest(body: GuestAuthDto): NetworkResult<AuthBody> = safeCall {
    client.post("/auth/guest") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<AuthBody>>().data
  }

  override suspend fun loginUser(body: AuthUserDto): NetworkResult<AuthBody> = safeCall {
    client.post("/auth/login") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<AuthBody>>().data
  }

  override suspend fun refreshSession(body: RefreshSessionDto): NetworkResult<AuthBody> = safeCall {
    client.post("/auth/refresh") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<AuthBody>>().data
  }

  override suspend fun createUserAuth(body: RegisterAuthUserDto): NetworkResult<RegisterAuthUserDto?>
      = safeCall {
    client.post("/auth/register") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<RegisterAuthUserDto?>>().data
  }
}

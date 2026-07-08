package com.dev.network.user.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.memebattle.core.network.call.safeCall
import com.dev.network.user.current.dto.SearchUserDto
import com.dev.network.user.current.dto.UpdateMeDto
import com.dev.network.user.current.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlin.String
import kotlin.collections.List

class UserApiServiceImpl(
  private val client: HttpClient,
) : UserApiService {
  override suspend fun getUsers(): NetworkResult<List<UserDto>> = safeCall {
    client.get("/user") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<List<UserDto>>>().data
  }

  override suspend fun getUserList(body: SearchUserDto): NetworkResult<List<UserDto>> = safeCall {
    client.post("/user/list") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<List<UserDto>>>().data
  }

  override suspend fun getMe(): NetworkResult<UserDto> = safeCall {
    client.get("/user/me") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<UserDto>>().data
  }

  override suspend fun updateMe(body: UpdateMeDto): NetworkResult<UserDto> = safeCall {
    client.patch("/user/me") {
      setBody(body)
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<UserDto>>().data
  }

  override suspend fun updateMyAvatar(): NetworkResult<UserDto> = safeCall {
    client.put("/user/me/avatar") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<UserDto>>().data
  }

  override suspend fun getUserById(id: String): NetworkResult<UserDto> = safeCall {
    client.get("/user/$id") {
    }
    .body<com.dev.memebattle.core.network.call.BaseResponse<UserDto>>().data
  }
}

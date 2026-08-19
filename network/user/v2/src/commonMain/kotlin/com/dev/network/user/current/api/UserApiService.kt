package com.dev.network.user.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.user.current.dto.UpdateMeDto
import com.dev.network.user.current.dto.UserDto
import kotlin.String

interface UserApiService {
  suspend fun getMe(): NetworkResult<UserDto>

  suspend fun updateMe(body: UpdateMeDto): NetworkResult<UserDto>

  suspend fun getUserById(id: String): NetworkResult<UserDto>
}

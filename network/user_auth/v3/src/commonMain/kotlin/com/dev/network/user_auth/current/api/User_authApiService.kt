package com.dev.network.user_auth.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.user_auth.current.dto.AuthBody
import com.dev.network.user_auth.current.dto.AuthUserDto
import com.dev.network.user_auth.current.dto.ChangePasswordDto
import com.dev.network.user_auth.current.dto.GuestAuthDto
import com.dev.network.user_auth.current.dto.RefreshSessionDto
import com.dev.network.user_auth.current.dto.RegisterAuthUserDto
import kotlin.Unit

interface User_authApiService {
  suspend fun changePassword(body: ChangePasswordDto): NetworkResult<Unit>

  suspend fun authAsGuest(body: GuestAuthDto): NetworkResult<AuthBody>

  suspend fun loginUser(body: AuthUserDto): NetworkResult<AuthBody>

  suspend fun refreshSession(body: RefreshSessionDto): NetworkResult<AuthBody>

  suspend fun createUserAuth(body: RegisterAuthUserDto): NetworkResult<RegisterAuthUserDto?>
}

package com.dev.network.user_auth.current.api

import com.dev.memebattle.core.network.call.NetworkResult
import com.dev.network.user_auth.current.dto.AuthBody
import com.dev.network.user_auth.current.dto.RefreshSessionDto

interface User_authApiService {
  suspend fun authAsGuest(): NetworkResult<AuthBody>

  suspend fun refreshSession(body: RefreshSessionDto): NetworkResult<AuthBody>
}

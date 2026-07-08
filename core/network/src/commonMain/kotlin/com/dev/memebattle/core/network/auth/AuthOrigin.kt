package com.dev.memebattle.core.network.auth

enum class AuthOrigin {
    /** Session not created - app just started */
    NONE,

    /** Guest session - token received via guest auth */
    GUEST,

    /** Authorized session - token received after login/registration */
    USER
}

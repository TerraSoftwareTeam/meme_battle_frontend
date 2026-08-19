package com.dev.memebattle.feature.home.impl.domain

/**
 * Describes the current user's authentication state in the home feature.
 */
sealed interface UserIdentity {

    /** App just launched — token origin is NONE, we don't know the state yet. */
    data object Unknown : UserIdentity

    /**
     * User is authenticated as a guest via /auth/guest.
     * [name] is optional — the user may or may not have provided a username.
     */
    data class Guest(val name: String? = null) : UserIdentity

    /**
     * User is fully authenticated via login/register.
     * [id] and [username] come from /user/me.
     */
    data class Authorized(val id: String, val username: String) : UserIdentity
}

val UserIdentity.isGuest: Boolean get() = this is UserIdentity.Guest
val UserIdentity.isAuthorized: Boolean get() = this is UserIdentity.Authorized
val UserIdentity.isUnknown: Boolean get() = this is UserIdentity.Unknown

val UserIdentity.displayName: String?
    get() = when (this) {
        is UserIdentity.Unknown -> null
        is UserIdentity.Guest -> name
        is UserIdentity.Authorized -> username
    }

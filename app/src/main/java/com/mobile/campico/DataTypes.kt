package com.mobile.campico

import kotlinx.serialization.Serializable

@Serializable
data class UserCredential (val email: String)

@Serializable
data class UserToken (val token: String)


@Serializable
data class UploadBackupRequest (val key: String, val content: String, val token: String, val email:String)



@Serializable
data class SimpleResponse (val message: String, val code: Int)
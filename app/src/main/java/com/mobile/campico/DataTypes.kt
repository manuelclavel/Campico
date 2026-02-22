package com.mobile.campico

import kotlinx.serialization.Serializable

@Serializable
data class UserCredential (val email: String)

@Serializable
data class UserToken (val token: String)


@Serializable
data class UploadBackupRequest (val key: String, val content: String, val token: String, val email:String)

@Serializable
data class UploadMediaVisitRequest (val s3key: String, val visitUid: Int, val content: String, val token: String, val email:String)

@Serializable
data class GetMediaObjectByKeyRequest(val bucket: String = "campico", val key: String, val email: String, val token: String)
@Serializable
data class GetTreesRequest (val token: String, val email: String, val query: String = "getTrees")
@Serializable
data class GetVisitsRequest (val token: String, val email:String, val query: String = "getVisits")


@Serializable
data class AddVisitRequest (val token: String, val email:String, val query: String = "addVisit", val date:String)

@Serializable
data class DeleteVisitRequest (val token: String, val email: String, val query: String = "deleteVisitByUid", val uid: Int)
@Serializable
data class GetVisitByUidRequest(val token: String, val email:String, val query: String = "getVisitByUid", val uid:Int)

@Serializable
data class GetMediaVisitsByVisitUidRequest(val token: String, val email: String, val query: String = "getMediaVisitsByVisitUid", val visitUid: Int)
@Serializable
data class SimpleResponse (val message: String, val code: Int)

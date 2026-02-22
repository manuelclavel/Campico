package com.mobile.campico

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Url



interface NetworkService {

    @GET
    suspend fun getBase64Image(@Url url: String): ResponseBody // Get raw string

    @PUT
    suspend fun generateToken(
        @Url url: String = "https://egsbwqh7kildllpkijk6nt4soq0wlgpe.lambda-url.ap-southeast-1.on.aws/",
        @Body email: UserCredential
    ): SimpleResponse

    @PUT
    suspend fun uploadBackupDB(
        @Url url: String = "https://f5sexosqfl4f2x3ksym23cqe640lzsun.lambda-url.ap-southeast-1.on.aws/",
        @Body backup: UploadBackupRequest
    ): SimpleResponse



    @PUT
    suspend fun uploadMediaVisitObject(
        // CampicoLambdaUploadObjectOrchestrator
        @Url url: String = "https://5skipjg6twldyyrlbfoy6n26jy0htjlh.lambda-url.ap-southeast-1.on.aws/",
        @Body upload: UploadMediaVisitRequest
    ): SimpleResponse


    @PUT
    suspend fun getTrees(
        @Url url:String = "https://3ajixssromdj2oqx4kpnbdvmxu0bndyu.lambda-url.ap-southeast-1.on.aws/",
        @Body payload: GetTreesRequest
    ): SimpleResponse

    @PUT
    suspend fun getVisits(
        @Url url:String = "https://3ajixssromdj2oqx4kpnbdvmxu0bndyu.lambda-url.ap-southeast-1.on.aws/",
        @Body payload: GetVisitsRequest
    ): SimpleResponse

    @PUT
    suspend fun addVisit(
        @Url url:String = "https://3ajixssromdj2oqx4kpnbdvmxu0bndyu.lambda-url.ap-southeast-1.on.aws/",
        @Body payload: AddVisitRequest
    ): SimpleResponse

    @PUT
    suspend fun deleteVisit(
        @Url url:String = "https://3ajixssromdj2oqx4kpnbdvmxu0bndyu.lambda-url.ap-southeast-1.on.aws/",
        @Body payload: DeleteVisitRequest
    ): SimpleResponse
    @PUT
    suspend fun getVisitByUid(
        @Url url:String = "https://3ajixssromdj2oqx4kpnbdvmxu0bndyu.lambda-url.ap-southeast-1.on.aws/",
        @Body payload: GetVisitByUidRequest
    ): SimpleResponse

    @PUT
    suspend fun getMediaVisitsByVisitUid(
        @Url url:String = "https://3ajixssromdj2oqx4kpnbdvmxu0bndyu.lambda-url.ap-southeast-1.on.aws/",
        @Body payload: GetMediaVisitsByVisitUidRequest
    ): SimpleResponse

    @PUT
    suspend fun getMediaObjectByKey(
        @Url url:String = "https://wwsynfkf6lzk6ezzjqvs27wdj40vbelj.lambda-url.ap-southeast-1.on.aws/",
        @Body payload: GetMediaObjectByKeyRequest
    ): SimpleResponse
}


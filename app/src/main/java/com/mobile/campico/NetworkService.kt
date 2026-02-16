package com.mobile.campico

import okhttp3.OkHttpClient
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url



interface NetworkService {
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
    suspend fun uploadObject(
        // CampicoLambdaUploadObjectOrchestrator
        @Url url: String = "https://5skipjg6twldyyrlbfoy6n26jy0htjlh.lambda-url.ap-southeast-1.on.aws/",
        @Body upload: UploadObjectRequest
    ): SimpleResponse

}


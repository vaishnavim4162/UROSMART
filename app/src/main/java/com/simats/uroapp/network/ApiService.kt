package com.simats.uroapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/signup.php")
    fun signup(@Body request: SignupRequest): Call<SignupResponse>

    @POST("api/auth/reset-password-email.php")
    fun resetPassword(@Body request: ChangePasswordRequest): Call<BasicResponse>

    @POST("api/auth/change-password.php")
    fun changePassword(@Body request: ChangePasswordRequest): Call<BasicResponse>

    @POST("api/auth/delete-account.php")
    fun deleteAccount(@retrofit2.http.Header("Authorization") token: String): Call<BasicResponse>

    @retrofit2.http.FormUrlEncoded
    @POST("fl.php")
    fun uploadGradients(
        @retrofit2.http.Field("action") action: String = "upload_gradients",
        @retrofit2.http.Field("client_id") clientId: String,
        @retrofit2.http.Field("gradients") gradients: String
    ): Call<BasicResponse>

    @retrofit2.http.FormUrlEncoded
    @POST("fl.php")
    fun getGlobalModelInfo(
        @retrofit2.http.Field("action") action: String = "get_global_model"
    ): Call<FLModelInfoResponse>
}

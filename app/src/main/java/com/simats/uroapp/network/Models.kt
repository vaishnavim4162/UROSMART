package com.simats.uroapp.network

data class User(
    val id: Int,
    val phone_number: String,
    val email: String,
    val created_at: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String?,
    val error: String?,
    val user: User?,
    val access_token: String?
)

data class SignupRequest(
    val phone_number: String,
    val email: String,
    val password: String
)

data class SignupResponse(
    val message: String?,
    val error: String?,
    val user: User?,
    val access_token: String?
)

data class ResetPasswordRequest(
    val email: String
)

data class BasicResponse(
    val message: String?,
    val error: String?
)

data class ChangePasswordRequest(
    val email: String,
    val new_password: String
)

data class FLModelInfoResponse(
    val status: String,
    val version: String,
    val download_url: String?
)

data class FLUploadRequest(
    val action: String = "upload_gradients",
    val client_id: String,
    val gradients: String
)

data class GroundTruthLabels(
    val yeast: Boolean,
    val triplePhosphate: Boolean,
    val calciumOxalate: Boolean,
    val squamousCells: Boolean,
    val uricAcid: Boolean
)

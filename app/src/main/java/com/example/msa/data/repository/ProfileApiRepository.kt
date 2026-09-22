package com.msa.android.data.repository

import com.msa.android.data.source.local.AuthPreferences
import com.msa.android.data.source.network.MsaApi
import com.msa.android.data.source.network.dto.ApiError
import com.msa.android.data.source.network.dto.ChangePasswordRequest
import com.msa.android.data.source.network.dto.UpdateProfileRequest
import com.msa.android.data.source.network.dto.UserDto
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ProfileResult {
    data class Success(val user: UserDto) : ProfileResult
    data class Failure(val message: String) : ProfileResult
}

@Singleton
class ProfileApiRepository @Inject constructor(
    private val api: MsaApi,
    private val prefs: AuthPreferences,
    private val moshi: Moshi
) {

    suspend fun load(): ProfileResult = safely {
        ProfileResult.Success(api.profile().bodyOrThrow().data)
    }

    suspend fun update(name: String?, email: String?): ProfileResult = safely {
        val user = api.updateProfile(UpdateProfileRequest(name = name, email = email))
            .bodyOrThrow().data
        // الاسم المخزّن محلياً لازم يتحدّث كمان، وإلا كارت الحساب
        // في شاشة المزيد يفضل شايل الاسم القديم.
        prefs.updateName(user.name)
        ProfileResult.Success(user)
    }

    /** بترجع رسالة الخطأ، أو null لو نجحت */
    suspend fun changePassword(current: String, new: String, confirm: String): String? = try {
        api.changePassword(ChangePasswordRequest(current, new, confirm)).unitOrThrow()
        null
    } catch (e: ApiException) {
        e.error.firstMessage() ?: "حصل خطأ"
    } catch (e: Exception) {
        NETWORK_ERROR
    }

    private inline fun safely(block: () -> ProfileResult): ProfileResult = try {
        block()
    } catch (e: ApiException) {
        ProfileResult.Failure(e.error.firstMessage() ?: "حصل خطأ")
    } catch (e: Exception) {
        ProfileResult.Failure(NETWORK_ERROR)
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        if (!isSuccessful) throw ApiException(parseError(this))
        return body() ?: throw ApiException(ApiError(message = "رد فاضي من السيرفر"))
    }

    private fun <T> Response<T>.unitOrThrow() {
        if (!isSuccessful) throw ApiException(parseError(this))
    }

    private fun <T> parseError(response: Response<T>): ApiError {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        return raw?.let {
            runCatching { moshi.adapter(ApiError::class.java).fromJson(it) }.getOrNull()
        } ?: ApiError(message = "حصل خطأ غير متوقع")
    }

    private class ApiException(val error: ApiError) : Exception(error.message)

    private companion object {
        const val NETWORK_ERROR = "تأكد من اتصالك بالإنترنت وجرّب تاني"
    }
}

package com.msa.android.data.source.network

import com.msa.android.data.source.network.dto.NewsApiResponse
import retrofit2.http.GET

/**
 * REST endpoints under `https://msagold.com/api/v1/`. iOS uses Alamofire with
 * the same base URL (see iOS `hostName` constant in `APIClient`); we declare
 * one interface here and Retrofit synthesizes the rest.
 */
interface MsaApi {
    /**
     * GET /news — fetches the admin-curated MSA news feed.
     * iOS sources this from `\(hostName)news` in `getManualNews()`.
     */
    @GET("news")
    suspend fun getNews(): NewsApiResponse

    companion object {
        const val BASE_URL = "https://api.msagold.com/api/v1/"
    }
}

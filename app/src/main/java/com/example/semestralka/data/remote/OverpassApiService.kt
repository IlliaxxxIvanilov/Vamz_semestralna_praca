package com.example.semestralka.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {

    @GET("api/interpreter")
    suspend fun fetchPoi(
        @Query("data") query: String
    ): OverpassResponse
}
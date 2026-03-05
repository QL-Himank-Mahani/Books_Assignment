package com.himank.booksassignment.retrofit

import com.himank.booksassignment.BuildConfig
import com.himank.booksassignment.utils.constants.ApiConstants
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET(ApiConstants.HARDCOVER_FICTION)
    suspend fun getBooks(
        @Query("api-key") apiKey: String = BuildConfig.NYT_API_KEY
    ): BooksResponse
}
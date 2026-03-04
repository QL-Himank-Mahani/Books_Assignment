package com.himank.booksassignment.retrofit

import com.himank.booksassignment.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("lists/current/hardcover-fiction.json")
    suspend fun getBooks(
        @Query("api-key") apiKey: String = BuildConfig.NYT_API_KEY
    ): BooksResponse
}
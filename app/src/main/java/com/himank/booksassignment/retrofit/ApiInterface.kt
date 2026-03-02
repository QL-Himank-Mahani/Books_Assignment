package com.himank.booksassignment.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("lists/current/hardcover-fiction.json")
    suspend fun getBooks(
        @Query("api-key") apiKey: String = "yqMoVi326swL7zmTH0CsddG9LHGtqAoZ"
    ): BooksResponse
}
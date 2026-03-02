package com.himank.booksassignment.dataStore

import com.himank.booksassignment.retrofit.ApiInterface
import com.himank.booksassignment.retrofit.BooksResponse

class BookNetworkRepository(private val api: ApiInterface) {
    suspend fun getBooks(): BooksResponse {
        return api.getBooks()
    }
}

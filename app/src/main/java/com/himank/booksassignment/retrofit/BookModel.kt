package com.himank.booksassignment.retrofit

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class BooksResponse(
    val status: String,
    val results: Results
)

data class Results(
    val books: List<Book>
)

@Parcelize
data class Book(
    val title: String,
    val author: String,
    val description: String,
    @SerializedName("book_image")
    val bookImage: String,
    val price: String
) : Parcelable

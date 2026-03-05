package com.himank.booksassignment.adapters

import androidx.recyclerview.widget.DiffUtil
import com.himank.booksassignment.retrofit.Book

class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
        return oldItem == newItem
    }

}
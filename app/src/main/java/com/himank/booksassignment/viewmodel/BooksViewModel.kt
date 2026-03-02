package com.himank.booksassignment.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.himank.booksassignment.dataStore.BookMarkedRepository
import com.himank.booksassignment.dataStore.BookQuantityRepository
import com.himank.booksassignment.retrofit.ApiInterface
import com.himank.booksassignment.retrofit.Book
import kotlinx.coroutines.launch

class BooksViewModel(
    private val api: ApiInterface,
    private val bookmarkRepository: BookMarkedRepository,
    private val quantityRepository: BookQuantityRepository
) : ViewModel() {

    // Home
    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books

    private val _bookmarkedTitles = MutableLiveData<Set<String>>(emptySet())
    val bookmarkedTitles: LiveData<Set<String>> = _bookmarkedTitles

    //bookview
    private val _quantity = MutableLiveData(0)
    val quantity: LiveData<Int> = _quantity

    private val _isBookmarked = MutableLiveData(false)
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private val _buySuccess = MutableLiveData(false)
    val buySuccess: LiveData<Boolean> = _buySuccess

    init {
        observeBookmarks()
        fetchBooks()
    }


    fun fetchBooks() {
        if (_books.value.isNullOrEmpty()) {
            Log.d("API", "API hit")
            viewModelScope.launch {
                try {
                    val response = api.getBooks()
                    _books.value = response.results.books
                } catch (e: Exception) {
                    Log.e("API", "Error fetching books", e)
                }
            }
        }
    }

    fun searchBooks(query: String): List<Book> {
        if (query.isEmpty()) return emptyList()
        return _books.value?.filter { it.title.contains(query, ignoreCase = true) } ?: emptyList()
    }

    fun toggleBookmark(book: Book) {
        viewModelScope.launch { bookmarkRepository.toggleBookmark(book.title) }
    }


    // BookView

    fun loadBookDetail(book: Book) {
        observeQuantity(book)
        observeBookmarkStatus(book)
    }

    fun onPlusClicked(book: Book) {
        viewModelScope.launch { quantityRepository.increment(book.title) }
    }

    fun onMinusClicked(book: Book) {
        viewModelScope.launch { quantityRepository.decrement(book.title) }
    }

    fun onBuyNowClicked(book: Book) {
        val qty = _quantity.value ?: 0
        if (qty > 0) {
            viewModelScope.launch {
                quantityRepository.reset(book.title)
                _buySuccess.postValue(true)
            }
        }
    }

    fun onBuyHandled() {
        _buySuccess.value = false
    }


    private fun observeBookmarks() {
        viewModelScope.launch {
            bookmarkRepository.getAllBookMarkedTitles().collect { _bookmarkedTitles.postValue(it) }
        }
    }

    private fun observeQuantity(book: Book) {
        viewModelScope.launch {
            quantityRepository.getQuantity(book.title).collect { _quantity.postValue(it) }
        }
    }

    private fun observeBookmarkStatus(book: Book) {
        viewModelScope.launch {
            bookmarkRepository.isBookMarked(book.title).collect { _isBookmarked.postValue(it) }
        }
    }
}

class BooksViewModelFactory(
    private val api: ApiInterface,
    private val bookmarkRepository: BookMarkedRepository,
    private val quantityRepository: BookQuantityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BooksViewModel(api, bookmarkRepository, quantityRepository) as T
    }
}

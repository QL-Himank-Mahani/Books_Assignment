package com.himank.booksassignment.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.search.SearchView
import com.himank.booksassignment.R
import com.himank.booksassignment.adapters.BooksHorizontalAdapter
import com.himank.booksassignment.adapters.BooksVerticalAdapter
import com.himank.booksassignment.dataStore.BookMarkedRepository
import com.himank.booksassignment.databinding.FragmentHomeBinding
import com.himank.booksassignment.retrofit.ApiInterface
import com.himank.booksassignment.retrofit.Book
import com.himank.booksassignment.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var fullBooksList: List<Book> = emptyList()
    private lateinit var api: ApiInterface
    private lateinit var bookmarkRepository: BookMarkedRepository
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var bookmarkedTitles: Set<String> = emptySet()

    private lateinit var horizontalLayoutManager: CustomLinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSearch.setOnClickListener {
            binding.ivMenu.visibility = View.GONE
            binding.tvExplore.visibility = View.GONE
            binding.ivSearch.visibility = View.GONE

            binding.searchBar.visibility = View.VISIBLE
            binding.searchBar.performClick()
        }

        binding.searchView.setupWithSearchBar(binding.searchBar)

        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                binding.searchBar.visibility = View.GONE
                binding.ivMenu.visibility = View.VISIBLE
                binding.tvExplore.visibility = View.VISIBLE
                binding.ivSearch.visibility = View.VISIBLE
            }
        }

        binding.searchView.editText.addTextChangedListener { editable ->
            val query = editable.toString()

            if (query.isEmpty()) {
                updateSearchRecyclerView(emptyList())
            } else {
                val filtered = fullBooksList.filter {
                    it.title.contains(query, ignoreCase = true) == true
                }
                updateSearchRecyclerView(filtered)
            }
        }


        api = RetrofitInstance
            .retrofit
            .create(ApiInterface::class.java)

        bookmarkRepository = BookMarkedRepository(requireContext())

        observeBookmarks()
        fetchBooks()

        binding.tvShowAll.setOnClickListener {
            if(binding.tvShowAll.text == "Show All"){
                horizontalLayoutManager.setScrollEnabled(true)
                binding.tvShowAll.text = "Show Less"
            } else {
                horizontalLayoutManager.setScrollEnabled(false)
                binding.tvShowAll.text = "Show All"
            }
            binding.rvYourInterest.smoothScrollToPosition(0)
        }

        binding.ivMenu.setOnClickListener {
            Toast.makeText(view.context, "Menu Bar Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeBookmarks() {
        viewLifecycleOwner.lifecycleScope.launch {
            bookmarkRepository.getAllBookMarkedTitles().collect { titles ->
                bookmarkedTitles = titles
                _binding?.let { binding ->
                    (binding.rvBestSellers.adapter as? BooksVerticalAdapter)?.updateBookmarks(titles)
                    (binding.rvYourInterest.adapter as? BooksHorizontalAdapter)?.updateBookmarks(titles)
                    (binding.rvSearchResults.adapter as? BooksVerticalAdapter)?.updateBookmarks(titles)
                }
            }
        }
    }

    private fun fetchBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = api.getBooks()
                val booksList = response.results.books
                fullBooksList = booksList

                _binding?.let {
                    setupHorizontalRecyclerView(booksList)
                    setupVerticalRecyclerView(booksList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API_ERROR", "Error fetching books", e)
                Toast.makeText(requireContext(), "Please try again later.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleBookmark(book: Book) {
        viewLifecycleOwner.lifecycleScope.launch {
                bookmarkRepository.toggleBookmark(book.title)

        }
    }

    private fun setupVerticalRecyclerView(booksList: List<Book>) {
        binding.rvBestSellers.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvBestSellers.adapter = BooksVerticalAdapter(
            booksList,
            bookmarkedTitles,
            onBookClick = { book -> navigateToBookView(book) },
            onBookmarkClick = { book -> toggleBookmark(book) }
        )
    }

    private fun setupHorizontalRecyclerView(booksList: List<Book>) {
        horizontalLayoutManager = CustomLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        horizontalLayoutManager.setScrollEnabled(false)

        binding.rvYourInterest.layoutManager = horizontalLayoutManager

        binding.rvYourInterest.adapter = BooksHorizontalAdapter(
            booksList,
            bookmarkedTitles,
            onBookClick = { book -> navigateToBookView(book) },
            onBookmarkClick = { book -> toggleBookmark(book) }
        )
    }

    private fun updateSearchRecyclerView(booksList: List<Book>) {
        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSearchResults.adapter = BooksVerticalAdapter(
            booksList,
            bookmarkedTitles,
            onBookClick = { book -> navigateToBookView(book) },
            onBookmarkClick = { book -> toggleBookmark(book) }
        )
    }

    private fun navigateToBookView(book: Book) {
        val bundle = Bundle().apply {
            putParcelable("book", book)
        }
        val bookViewFragment = BookView()
        bookViewFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .add(R.id.main, bookViewFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CustomLinearLayoutManager(context: Context, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    private var isScrollEnabled = false

    fun setScrollEnabled(enabled: Boolean) {
        isScrollEnabled = enabled
    }

    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled && super.canScrollHorizontally()
    }
}
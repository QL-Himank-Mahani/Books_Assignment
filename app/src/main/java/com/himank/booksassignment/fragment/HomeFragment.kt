package com.himank.booksassignment.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.search.SearchView
import com.himank.booksassignment.R
import com.himank.booksassignment.adapters.BooksHorizontalAdapter
import com.himank.booksassignment.adapters.BooksVerticalAdapter
import com.himank.booksassignment.dataStore.BookMarkedRepository
import com.himank.booksassignment.databinding.FragmentHomeBinding
import com.himank.booksassignment.retrofit.Book
import com.himank.booksassignment.retrofit.RetrofitInstance
import com.himank.booksassignment.dataStore.BookQuantityRepository
import com.himank.booksassignment.viewmodel.BooksViewModel
import com.himank.booksassignment.viewmodel.BooksViewModelFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var horizontalLayoutManager: CustomLinearLayoutManager

    private val viewModel: BooksViewModel by viewModels {
        BooksViewModelFactory(
            RetrofitInstance.retrofit.create(com.himank.booksassignment.retrofit.ApiInterface::class.java),
            BookMarkedRepository(requireContext()),
            BookQuantityRepository(requireContext())
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearch()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupSearch() {
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
            val results = viewModel.searchBooks(editable.toString())
            updateSearchRecyclerView(results)
        }
    }

    private fun setupClickListeners() {
        binding.ivSearch.setOnClickListener {
            binding.ivMenu.visibility = View.GONE
            binding.tvExplore.visibility = View.GONE
            binding.ivSearch.visibility = View.GONE
            binding.searchBar.visibility = View.VISIBLE
            binding.searchBar.performClick()
        }

        binding.tvShowAll.setOnClickListener {
            if (binding.tvShowAll.text == "Show All") {
                horizontalLayoutManager.setScrollEnabled(true)
                binding.tvShowAll.text = "Show Less"
            } else {
                horizontalLayoutManager.setScrollEnabled(false)
                binding.tvShowAll.text = "Show All"
            }
            binding.rvYourInterest.smoothScrollToPosition(0)
        }

        binding.ivMenu.setOnClickListener {
            Toast.makeText(requireContext(), "Menu Bar Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.books.observe(viewLifecycleOwner) { books ->
            setupVerticalRecyclerView(books)
            setupHorizontalRecyclerView(books)
        }

        viewModel.bookmarkedTitles.observe(viewLifecycleOwner) { titles ->
            (binding.rvBestSellers.adapter as? BooksVerticalAdapter)?.updateBookmarks(titles)
            (binding.rvYourInterest.adapter as? BooksHorizontalAdapter)?.updateBookmarks(titles)
            (binding.rvSearchResults.adapter as? BooksVerticalAdapter)?.updateBookmarks(titles)
        }
    }

    private fun setupVerticalRecyclerView(books: List<Book>) {
        binding.rvBestSellers.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvBestSellers.adapter = BooksVerticalAdapter(
            books,
            viewModel.bookmarkedTitles.value ?: emptySet(),
            onBookClick = { book -> navigateToBookView(book) },
            onBookmarkClick = { book -> viewModel.toggleBookmark(book) }
        )
    }

    private fun setupHorizontalRecyclerView(books: List<Book>) {
        horizontalLayoutManager = CustomLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        horizontalLayoutManager.setScrollEnabled(false)
        binding.rvYourInterest.layoutManager = horizontalLayoutManager
        binding.rvYourInterest.adapter = BooksHorizontalAdapter(
            books,
            viewModel.bookmarkedTitles.value ?: emptySet(),
            onBookClick = { book -> navigateToBookView(book) },
            onBookmarkClick = { book -> viewModel.toggleBookmark(book) }
        )
    }

    private fun updateSearchRecyclerView(books: List<Book>) {
        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSearchResults.adapter = BooksVerticalAdapter(
            books,
            viewModel.bookmarkedTitles.value ?: emptySet(),
            onBookClick = { book -> navigateToBookView(book) },
            onBookmarkClick = { book -> viewModel.toggleBookmark(book) }
        )
    }

    private fun navigateToBookView(book: Book) {
        val bundle = Bundle().apply { putParcelable("book", book) }
        val bookViewFragment = BookView()
        bookViewFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.main, bookViewFragment)
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
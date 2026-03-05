package com.himank.booksassignment.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.search.SearchView
import com.himank.booksassignment.R
import com.himank.booksassignment.adapters.BooksHorizontalAdapter
import com.himank.booksassignment.adapters.BooksVerticalAdapter
import com.himank.booksassignment.dataStore.BookNetworkRepository
import com.himank.booksassignment.dataStore.BookRepository
import com.himank.booksassignment.databinding.FragmentHomeBinding
import com.himank.booksassignment.retrofit.ApiInterface
import com.himank.booksassignment.retrofit.Book
import com.himank.booksassignment.retrofit.RetrofitInstance
import com.himank.booksassignment.utils.constants.BundleKeys
import com.himank.booksassignment.viewmodel.BooksViewModel
import com.himank.booksassignment.viewmodel.BooksViewModelFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var horizontalLayoutManager: CustomLinearLayoutManager
    private var isShowingAll = false

    private lateinit var horizontalAdapter: BooksHorizontalAdapter
    private lateinit var verticalAdapter: BooksVerticalAdapter
    private lateinit var searchAdapter: BooksVerticalAdapter

    private val viewModel: BooksViewModel by activityViewModels {
        BooksViewModelFactory(
            BookNetworkRepository(RetrofitInstance.retrofit.create(ApiInterface::class.java)),
            BookRepository(requireContext())
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearch()
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        verticalAdapter = BooksVerticalAdapter(
            viewModel.bookmarkedTitles.value ?: emptySet(),
            onBookClick = { navigateToBookView(it) },
            onBookmarkClick = { viewModel.toggleBookmark(it) }
        )

        horizontalAdapter = BooksHorizontalAdapter(
            viewModel.bookmarkedTitles.value ?: emptySet(),
            onBookClick = { navigateToBookView(it) },
            onBookmarkClick = { viewModel.toggleBookmark(it) }
        )

        searchAdapter = BooksVerticalAdapter(
            viewModel.bookmarkedTitles.value ?: emptySet(),
            onBookClick = { navigateToBookView(it) },
            onBookmarkClick = { viewModel.toggleBookmark(it) }
        )

        binding.rvBestSellers.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvBestSellers.adapter = verticalAdapter

        horizontalLayoutManager =
            CustomLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        horizontalLayoutManager.setScrollEnabled(false)

        binding.rvYourInterest.layoutManager = horizontalLayoutManager
        binding.rvYourInterest.adapter = horizontalAdapter

        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSearchResults.adapter = searchAdapter
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
            isShowingAll = !isShowingAll
            horizontalLayoutManager.setScrollEnabled(isShowingAll)
            binding.tvShowAll.text = getString(if (isShowingAll) R.string.show_less else R.string.show_all)
            binding.rvYourInterest.scrollToPosition(0)
        }

        binding.ivMenu.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.menu_bar_clicked), Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.books.observe(viewLifecycleOwner) { books ->
            verticalAdapter.submitList(books)
            horizontalAdapter.submitList(books)
            searchAdapter.submitList(books)
        }

        viewModel.bookmarkedTitles.observe(viewLifecycleOwner) { titles ->
            verticalAdapter.updateBookmarks(titles)
            horizontalAdapter.updateBookmarks(titles)
            searchAdapter.updateBookmarks(titles)
        }
    }


    private fun updateSearchRecyclerView(books: List<Book>) {
       searchAdapter.submitList(books)
    }

    private fun navigateToBookView(book: Book) {
        val bundle = Bundle().apply { putParcelable(BundleKeys.BOOK_ARG_KEY, book) }
        val bookViewFragment = BookViewFragment()
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

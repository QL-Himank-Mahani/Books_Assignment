package com.himank.booksassignment.fragment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.himank.booksassignment.R
import com.himank.booksassignment.constants.BOOK_ARG_KEY
import com.himank.booksassignment.dataStore.BookNetworkRepository
import com.himank.booksassignment.dataStore.BookRepository
import com.himank.booksassignment.databinding.FragmentBookViewBinding
import com.himank.booksassignment.retrofit.ApiInterface
import com.himank.booksassignment.retrofit.Book
import com.himank.booksassignment.retrofit.RetrofitInstance
import com.himank.booksassignment.viewmodel.BooksViewModel
import com.himank.booksassignment.viewmodel.BooksViewModelFactory

class BookView : Fragment() {
    private var _binding: FragmentBookViewBinding? = null
    private val binding get() = _binding!!

    private val bookArg: Book? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(BOOK_ARG_KEY, Book::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable<Book>(BOOK_ARG_KEY)
        }
    }

    private val viewModel: BooksViewModel by activityViewModels {
        BooksViewModelFactory(
            BookNetworkRepository(RetrofitInstance.retrofit.create(ApiInterface::class.java)),
            BookRepository(requireContext())
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val book = bookArg ?: return

        viewModel.loadBookDetail(book)

        binding.tvBookTitle.text = book.title
        binding.tvBookAuthor.text = getString(R.string.author_prefix, book.author)
        binding.tvBookDescription.text = book.description
        binding.tvBookPrice.text = if (book.price == "0.00") getString(R.string.free) else getString(R.string.price_value, book.price)

        loadBookImage(view, book)

        clicks(book)

        setUpObservers()

    }

    private fun setUpObservers() {
        viewModel.quantity.observe(viewLifecycleOwner) { qty ->
            binding.tvQuantity.text = qty.toString()
        }

        viewModel.isBookmarked.observe(viewLifecycleOwner) { bookmarked ->
            binding.ivBookmarked.setImageResource(
                if (bookmarked) R.drawable.bookmark_yes24px else R.drawable.bookmark_24px
            )
        }

        viewModel.buySuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, getString(R.string.purchase_successful), Toast.LENGTH_SHORT).show()
                viewModel.onBuyHandled()
            }
        }
    }

    private fun clicks(book: Book) {
        binding.btnPlus.setOnClickListener { viewModel.onPlusClicked(book) }
        binding.btnMinus.setOnClickListener { viewModel.onMinusClicked(book) }
        binding.ivBookmarked.setOnClickListener { viewModel.toggleBookmark(book) }
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnBuyNow.setOnClickListener {
            val qty = viewModel.quantity.value ?: 0
            if (qty == 0) {
                Toast.makeText(context, getString(R.string.select_at_least_one_book), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.onBuyNowClicked(book)
            }
        }
    }

    private fun loadBookImage(view: View, book: Book) {
        Glide.with(view.context)
            .asBitmap()
            .load(book.book_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.ivBookImage.setImageBitmap(resource)
                    Palette.from(resource).generate { palette ->
                        val defaultColor = ContextCompat.getColor(view.context, R.color.white)
                        val color = palette?.getDominantColor(defaultColor) ?: defaultColor
                        binding.constraintLayout.backgroundTintList = ColorStateList.valueOf(color)

                        val textColor = if (ColorUtils.calculateLuminance(color) < 0.5)
                            ContextCompat.getColor(view.context, R.color.white)
                        else
                            ContextCompat.getColor(view.context, R.color.black)

                        binding.tvBookTitle.setTextColor(textColor)
                        binding.tvBookAuthor.setTextColor(textColor)
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

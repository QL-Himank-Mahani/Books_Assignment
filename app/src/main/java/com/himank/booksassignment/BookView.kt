package com.himank.booksassignment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.himank.booksassignment.databinding.FragmentBookViewBinding
import kotlinx.coroutines.launch

class BookView : Fragment() {

    private var _binding: FragmentBookViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var quantityRepository: BookQuantityRepository
    private lateinit var bookmarkRepository: BookMarkedRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quantityRepository = BookQuantityRepository(requireContext())
        bookmarkRepository = BookMarkedRepository(requireContext())

        val book = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("book", Book::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable<Book>("book")
        }

        binding.mainFrame.setOnClickListener {
            Log.d("BookView", "Main frame clicked, popping back stack")
        }

        val title = book?.title ?: return

        book.let { b ->
            binding.tvBookTitle.text = b.title
            binding.tvBookAuthor.text = "By " + b.author
            binding.tvBookDescription.text = b.description

            if(b.price == "0.00") {
                binding.tvBookPrice.text = "Free"
            } else {
                binding.tvBookPrice.text = "$" + b.price
            }

            fun setRoundedBackground(color: Int) {
                binding.constraintLayout.backgroundTintList = ColorStateList.valueOf(color)
            }


            Glide.with(view.context)
                .asBitmap()
                .load(book.book_image)
                .into(object: CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        binding.ivBookImage.setImageBitmap(resource)
                        Palette.from(resource).generate(){ palette ->
                            val defaultColor = ContextCompat.getColor(
                                view.context,
                                R.color.white
                            )

                            val color = palette?.getDominantColor(defaultColor) ?: defaultColor
                            setRoundedBackground(color)

                            val luminace = ColorUtils.calculateLuminance(color)
                            if (luminace < 0.5){
                                binding.tvBookTitle.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                                binding.tvBookAuthor.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                                binding.tvBookDescription.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                                binding.tvBookPrice.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                            } else {
                                binding.tvBookTitle.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                                binding.tvBookAuthor.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                                binding.tvBookDescription.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                                binding.tvBookPrice.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                })

            viewLifecycleOwner.lifecycleScope.launch {
                quantityRepository.getQuantity(b.title).collect { qty ->
                    binding.tvQuantity.text = qty.toString()
                }
            }

            binding.btnPlus.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    quantityRepository.increment(b.title)
                }
            }

            binding.btnMinus.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    quantityRepository.decrement(b.title)
                }
            }

            binding.imageView3.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bookmarkRepository.isBookMarked(title).collect { isBookmarked ->
                if (isBookmarked) {
                    binding.ivBookmarked.setImageResource(R.drawable.bookmark_yes24px)
                } else {
                    binding.ivBookmarked.setImageResource(R.drawable.bookmark_24px)
                }
            }
        }

        binding.ivBookmarked.setOnClickListener {
            lifecycleScope.launch {
                val title = book.title ?: return@launch
                bookmarkRepository.toggleBookmark(title)
            }
        }

        binding.btnBuyNow.setOnClickListener {
            val quantity = binding.tvQuantity.text.toString().toIntOrNull() ?: 0
            if (quantity == 0) {
                Toast.makeText(context, "Please select at least 1 book to buy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                lifecycleScope.launch {
                    quantityRepository.reset(book.title ?: "")
                }
                Toast.makeText(context, "Purchased $quantity copies of ${book.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

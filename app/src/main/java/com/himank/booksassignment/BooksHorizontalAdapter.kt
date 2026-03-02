package com.himank.booksassignment

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.himank.booksassignment.databinding.BookLayoutBinding

class BooksHorizontalAdapter(
    private val books: List<Book>,
    private var bookmarkedTitles: Set<String>,
    private val onBookClick: (Book) -> Unit,
    private val onBookmarkClick: (Book) -> Unit
) : RecyclerView.Adapter<BooksHorizontalAdapter.BookViewHolder>() {

    class BookViewHolder(val binding: BookLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookViewHolder {
        val binding = BookLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val params = binding.root.layoutParams

        val displayMetrics = parent.context.resources.displayMetrics
        val screenWidthPx = displayMetrics.widthPixels

        val horizontalPadding = (32 * displayMetrics.density).toInt()
        val itemSpacing = (16 * displayMetrics.density).toInt()

        val itemWidth = (screenWidthPx - horizontalPadding - itemSpacing) / 2

        params.width = itemWidth
        binding.root.layoutParams = params

//        params.width = (168 * parent.context.resources.displayMetrics.density).toInt()
//        binding.root.layoutParams = params

        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BookViewHolder,
        position: Int
    ) {
        val book = books[position]
        holder.binding.tvBookName.text = book.title
        holder.binding.tvAuthorName.text = "By " + book.author

        val isBookmarked = bookmarkedTitles.contains(book.title)
        if (isBookmarked) {
            holder.binding.ivBookmark.setImageResource(R.drawable.bookmark_yes24px)
        } else {
            holder.binding.ivBookmark.setImageResource(R.drawable.bookmark_24px)
        }

        holder.binding.ivBookmark.setOnClickListener {
            onBookmarkClick(book)
        }

        fun setRoundedBackground(color: Int) {
            holder.binding.imageOuterBox.backgroundTintList = ColorStateList.valueOf(color)
        }

        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(book.book_image)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    holder.binding.bookImage.setImageBitmap(resource)
                    Palette.from(resource).generate { palette ->
                        val defaultColor = ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.white
                        )
                        val color = palette?.getDominantColor(defaultColor) ?: defaultColor
                        setRoundedBackground(color)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        holder.itemView.setOnClickListener {
            onBookClick(book)
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }

    fun updateBookmarks(newBookmarkedTitles: Set<String>) {
        this.bookmarkedTitles = newBookmarkedTitles
        notifyDataSetChanged()
    }
}

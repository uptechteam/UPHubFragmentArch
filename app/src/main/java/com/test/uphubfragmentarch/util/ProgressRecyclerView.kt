package com.test.uphubfragmentarch.util

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.test.uphubfragmentarch.R

class ProgressRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    companion object {

        private const val DURATION = 400L
    }

    private val progress = ProgressLayout(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        visibility = View.VISIBLE
        setDimColor(ContextCompat.getColor(context, R.color.progressDimFul))
    }

    private val emptyList: View =
        LayoutInflater.from(context).inflate(R.layout.view_empty_list, this, false).apply {
            visibility = View.GONE
        }

    val recycler =
        LayoutInflater.from(context).inflate(R.layout.view_recycler, this, false) as RecyclerView

    val swipeToRefresh = SwipeRefreshLayout(context).also {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    var onSwipe: () -> Unit = {}

    init {
        swipeToRefresh.addView(recycler)
        addView(swipeToRefresh)
        addView(emptyList)
        addView(progress)

        swipeToRefresh.setOnRefreshListener {
            onSwipe()
            swipeToRefresh.isRefreshing = false
        }
        emptyList.findViewById<Button>(R.id.button).setOnClickListener { onSwipe() }
    }

    fun onEmptyList() {
        emptyList.visibility = View.VISIBLE
    }

    fun onNonEmptyList() {
        emptyList.visibility = View.GONE
    }

    fun showProgress() = progress.show()

    fun hideProgress() = progress.hide()
}
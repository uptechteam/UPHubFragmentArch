package com.test.uphubfragmentarch.ui.main.repository

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.uphubfragmentarch.databinding.FragmentRepositoryBinding
import com.test.uphubfragmentarch.ui.BaseFragment
import com.test.uphubfragmentarch.util.observe

class RepositoryFragment : BaseFragment<RepositoryViewModel, FragmentRepositoryBinding>() {

    companion object {
        private const val TRIGGER_ZONE = 15
    }

    private val adapter = RepositoryAdapter()
    private lateinit var dividerItemDecoration: DividerItemDecoration

    override fun provideBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRepositoryBinding =
        FragmentRepositoryBinding.inflate(inflater, container, false)

    override fun onBinding(bind: FragmentRepositoryBinding, bundle: Bundle?) {
        super.onBinding(bind, bundle)
        val layoutManager = LinearLayoutManager(context)
        dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        bind.viewModel = viewModel
        bind.list.apply {
            recycler.adapter = adapter
            recycler.layoutManager = layoutManager
            recycler.addItemDecoration(dividerItemDecoration)
            recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (layoutManager.findLastVisibleItemPosition() + TRIGGER_ZONE > adapter.itemCount)
                        viewModel.onEndOfListReached()
                }
            })

            onSwipe = viewModel::refresh
        }

        observe(viewModel.models) { list ->
            adapter.update(list)
            if (list.isEmpty()) bind.list.onEmptyList() else bind.list.onNonEmptyList()
        }
    }

    override fun onDestroyView() {
        binding.list.recycler.apply {
            layoutManager = null
            adapter = null
            removeItemDecoration(dividerItemDecoration)
        }
        super.onDestroyView()

    }
}
package com.test.uphubfragmentarch.ui.main.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.uphubfragmentarch.databinding.FragmentUserBinding
import com.test.uphubfragmentarch.ui.BaseFragment
import com.test.uphubfragmentarch.util.observe

class UserFragment : BaseFragment<UserViewModel, FragmentUserBinding>() {

    companion object {
        private const val TRIGGER_ZONE = 15
    }

    private val adapter = UserAdapter {
        viewModel.onUserClick(it)
//        (activity as? MainActivity)?.showUser(it)
    }

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var dividerItemDecoration: DividerItemDecoration

    private val scrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (adapter.itemCount - layoutManager.findLastVisibleItemPosition() < TRIGGER_ZONE)
                viewModel.onFetchMore()
        }
    }

    override fun provideBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserBinding =
        FragmentUserBinding.inflate(inflater, container, false)

    override fun onBinding(bind: FragmentUserBinding, bundle: Bundle?) {
        bind.viewModel = viewModel
        layoutManager = LinearLayoutManager(context)
        dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        bind.recycler.apply {
            recycler.adapter = adapter
            recycler.layoutManager = layoutManager
            recycler.addOnScrollListener(scrollListener)
            recycler.addItemDecoration(dividerItemDecoration)
            bind.recycler.onSwipe = viewModel::refresh
        }

        observe(viewModel.models) { users ->
            adapter.update(users)
            if (users.isEmpty()) bind.recycler.onEmptyList() else bind.recycler.onNonEmptyList()
        }
    }

    override fun onDestroyView() {
        binding.recycler.recycler.apply {
            layoutManager = null
            adapter = null
            removeItemDecoration(dividerItemDecoration)
        }
        super.onDestroyView()
    }
}
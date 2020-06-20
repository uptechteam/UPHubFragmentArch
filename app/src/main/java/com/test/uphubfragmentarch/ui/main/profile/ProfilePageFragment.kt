package com.test.uphubfragmentarch.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.uphubfragmentarch.databinding.FragmentProfileSubscriptionBinding
import com.test.uphubfragmentarch.ui.BaseFragment
import com.test.uphubfragmentarch.ui.BaseRecyclerViewAdapter
import com.test.uphubfragmentarch.ui.BaseViewHolder
import com.test.uphubfragmentarch.ui.Item
import com.test.uphubfragmentarch.util.observe

abstract class ProfilePageFragment<Data : Item<Data>> :
    BaseFragment<ProfilePageViewModel<Data>, FragmentProfileSubscriptionBinding>() {

    companion object {
        private const val USER_ID = "user_id"
        private const val TYPE = "type"
        private const val TRIGGER_ZONE = 15

        fun getBundle(
            userId: Int,
            type: Type
        ) = bundleOf(USER_ID to userId, TYPE to type)

        enum class Type {
            Follower, Subscription, Repository
        }
    }

    private lateinit var dividerItemDecoration: DividerItemDecoration

    abstract val adapter: BaseRecyclerViewAdapter<out BaseViewHolder<Data>, Data>

    override fun onBinding(bind: FragmentProfileSubscriptionBinding, bundle: Bundle?) {
        super.onBinding(bind, bundle)
        bind.list.apply {
            recycler.adapter = adapter
            val lm = LinearLayoutManager(context)
            dividerItemDecoration =
                DividerItemDecoration(
                    context,
                    lm.orientation
                )
            recycler.layoutManager = lm
            recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (adapter.itemCount - lm.findLastVisibleItemPosition() < TRIGGER_ZONE)
                        viewModel.onMore()
                }
            })
            recycler.addItemDecoration(dividerItemDecoration)
            onSwipe = viewModel::refresh
        }
        observe(viewModel.models) { adapter.update(it) }
        observe(viewModel.progress) {
            if (it) bind.list.showProgress() else bind.list.hideProgress()
        }
        val id = (arguments ?: bundle)?.getInt(USER_ID) ?: throw IllegalArgumentException()
        viewModel.onFetch(id)
    }

    override fun onDestroyView() {
        binding.list.apply {
            recycler.layoutManager = null
            recycler.adapter = null
            recycler.removeItemDecoration(dividerItemDecoration)
        }
        super.onDestroyView()
    }

    override fun provideBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileSubscriptionBinding {
        return FragmentProfileSubscriptionBinding.inflate(
            inflater,
            container,
            false
        )
    }
}
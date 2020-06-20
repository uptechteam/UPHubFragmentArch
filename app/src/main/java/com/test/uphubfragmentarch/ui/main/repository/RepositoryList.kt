package com.test.uphubfragmentarch.ui.main.repository

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.test.uphubfragmentarch.databinding.ItemProgressBinding
import com.test.uphubfragmentarch.databinding.ItemRepositoryBinding
import com.test.uphubfragmentarch.ui.BaseRecyclerViewAdapter
import com.test.uphubfragmentarch.ui.BaseViewHolder

class RepositoryAdapter : BaseRecyclerViewAdapter<RepositoryUIViewHolder, RepositoryUI>() {

    companion object {

        private const val TYPE_REPOSITORY = 45
        private const val TYPE_PROGRESS = 23
    }

    override fun provideHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): RepositoryUIViewHolder = when (viewType) {
        TYPE_REPOSITORY ->
            RepositoryViewHolder(ItemRepositoryBinding.inflate(layoutInflater, parent, false))
        else ->
            EmptyViewHolder(ItemProgressBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int =
        if (models[position].repository == null) TYPE_PROGRESS else TYPE_REPOSITORY
}

class RepositoryViewHolder(private val binding: ItemRepositoryBinding) :
    RepositoryUIViewHolder(binding.root) {

    override fun bind(model: RepositoryUI, payload: MutableList<Any>?) {
        binding.repos = model.repository
        binding.executePendingBindings()
    }

    override fun unbind() {
    }

}

class EmptyViewHolder(binding: ItemProgressBinding) : RepositoryUIViewHolder(binding.root)

abstract class RepositoryUIViewHolder(view: View) : BaseViewHolder<RepositoryUI>(view) {
    override fun bind(model: RepositoryUI, payload: MutableList<Any>?) {

    }

    override fun unbind() {

    }
}
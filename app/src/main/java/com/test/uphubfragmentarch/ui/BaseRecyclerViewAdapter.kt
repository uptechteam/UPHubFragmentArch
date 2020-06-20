package com.test.uphubfragmentarch.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

abstract class BaseRecyclerViewAdapter<VH : BaseViewHolder<M>, M : Item<M>> :
    RecyclerView.Adapter<VH>() {

    protected open val diffUtilCallback: DefaultDiffUtilCallback<M> =
        DefaultDiffUtilCallback()

    val models = mutableListOf<M>()

    abstract fun provideHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): VH

    fun update(updModels: List<M>) = DiffUtil.calculateDiff(
        diffUtilCallback.apply {
            setItems(models, updModels)
        }
    ).run {
        models.clear()
        models.addAll(updModels)
        dispatchUpdatesTo(this@BaseRecyclerViewAdapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        provideHolder(LayoutInflater.from(parent.context), parent, viewType)

    override fun getItemCount(): Int = models.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(models[position], null)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        holder.bind(models[position], payloads)
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.unbind()
    }
}

abstract class BaseViewHolder<M>(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bind(model: M, payload: MutableList<Any>?)
    abstract fun unbind()
}

abstract class GlideViewHolder<M>(view: View) : BaseViewHolder<M>(view) {
    protected val requestManager: RequestManager = Glide.with(view)

    @CallSuper
    override fun bind(model: M, payload: MutableList<Any>?) {
    }

    @CallSuper
    override fun unbind() {
    }
}

open class DefaultDiffUtilCallback<M : Item<M>> : DiffUtil.Callback() {

    protected var oldList: List<M> = mutableListOf()
    protected var newList: List<M> = mutableListOf()

    fun setItems(
        oldList: List<M>,
        newList: List<M>
    ) {
        this.oldList = oldList
        this.newList = newList
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].eqId(newList[newItemPosition])

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].eqUI(newList[newItemPosition])
}

interface Item<M> {

    fun eqId(item: M): Boolean

    fun eqUI(item: M): Boolean
}

class PagerStateAdapter(
    fm: FragmentManager,
    private val fragments: List<Fragment>,
    private val labels: List<String> = listOf()
) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = fragments.size

    override fun getItem(i: Int): Fragment = fragments[i]

    override fun getPageTitle(position: Int): CharSequence = labels.getOrNull(position) ?: ""
}

package com.test.uphubfragmentarch.ui.main.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.test.uphubfragmentarch.data.db.model.User
import com.test.uphubfragmentarch.databinding.ItemProgressBinding
import com.test.uphubfragmentarch.databinding.ItemUserBinding
import com.test.uphubfragmentarch.ui.BaseRecyclerViewAdapter
import com.test.uphubfragmentarch.ui.BaseViewHolder
import com.test.uphubfragmentarch.ui.Item

class UserAdapter(val callback: (UserUI) -> Unit = {}) :
    BaseRecyclerViewAdapter<UserUIViewHolder, UserUI>() {

    companion object {

        private const val TYPE_REPOSITORY = 45
        private const val TYPE_PROGRESS = 23
    }

    override fun provideHolder(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        when (viewType) {
            TYPE_REPOSITORY ->
                UserHolder(ItemUserBinding.inflate(layoutInflater, parent, false), callback)
            else ->
                EmptyViewHolder(ItemProgressBinding.inflate(layoutInflater, parent, false))
        }

    override fun getItemViewType(position: Int): Int = TYPE_REPOSITORY
}

class EmptyViewHolder(binding: ItemProgressBinding) : UserUIViewHolder(binding.root) {
    override fun bind(model: UserUI, payload: MutableList<Any>?) {

    }

    override fun unbind() {
    }
}

class UserHolder(val binding: ItemUserBinding, val callback: (UserUI) -> Unit) :
    UserUIViewHolder(binding.root) {

    init {
        binding.root.setOnClickListener(View.OnClickListener {
            callback(binding.model ?: return@OnClickListener)
        })
    }

    override fun bind(model: UserUI, payload: MutableList<Any>?) {
        binding.model = model
    }

    override fun unbind() {
    }
}

abstract class UserUIViewHolder(view: View) : BaseViewHolder<UserUI>(view) {

}

data class UserUI(
    val id: Int,
    val login: String,
    val name: String,
    val location: String,
    val bio: String,
    val avatarUrl: String
) : Item<UserUI> {

    override fun eqId(item: UserUI): Boolean = id == item.id

    override fun eqUI(item: UserUI): Boolean =
        eqId(item)
                && item.name == name
                && item.login == login
                && item.bio == bio
                && item.location == location

    companion object {
        fun fromUser(user : User) : UserUI {
            return with(user) {
                UserUI(
                    id, login, name, location, bio, avatarUrl
                )
            }
        }
    }
}
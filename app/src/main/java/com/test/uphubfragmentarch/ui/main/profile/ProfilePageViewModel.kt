package com.test.uphubfragmentarch.ui.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.test.uphubfragmentarch.ui.BaseViewModel
import com.test.uphubfragmentarch.util.set


abstract class ProfilePageViewModel<DataType> : BaseViewModel() {
    protected val fetch = MutableLiveData<Int>()
    fun onFetch(userId: Int) = fetch.set(userId)
    abstract val models: LiveData<List<DataType>>
    abstract val progress: LiveData<Boolean>
    abstract fun onMore()
    abstract fun onItemClicked(item: DataType)
    abstract fun refresh()
}
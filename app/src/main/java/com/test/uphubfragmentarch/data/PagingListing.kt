package com.test.uphubfragmentarch.data

import androidx.lifecycle.LiveData

class PagingListing<T>(
    val data: LiveData<T>,
    val initialProgress: LiveData<Boolean>,
    val errors: LiveData<Any>,
    val onMore: () -> Unit,
    val refresh: () -> Unit
)
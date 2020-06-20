package com.test.uphubfragmentarch.ui.main.repository

import com.test.uphubfragmentarch.data.db.model.Repository
import com.test.uphubfragmentarch.ui.Item

data class RepositoryUI(val repository: Repository? = null) : Item<RepositoryUI> {

    override fun eqId(item: RepositoryUI): Boolean =
            if (repository != null && item.repository != null)
                with(repository) { item.repository.id == id }
            else
                repository == null && item.repository == null

    override fun eqUI(item: RepositoryUI): Boolean =
            if (repository != null && item.repository != null)
                with(repository) {
                    eqId(item)
                            && item.repository.name == name
                            && item.repository.description == description
                            && item.repository.isFork == isFork
                            && item.repository.isPrivate == isPrivate
                }
            else
                eqId(item)
}
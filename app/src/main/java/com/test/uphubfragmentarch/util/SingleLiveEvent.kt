package com.test.uphubfragmentarch.util

import androidx.annotation.MainThread
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

open class SingleLiveEvent<T> : MediatorLiveData<T>() {
    //Used when observeForever is called since ConcurrentHashMap doesn't support null as key
    private val fakeOwner = object : LifecycleOwner {
        override fun getLifecycle(): Lifecycle {
            return object : Lifecycle() {
                override fun addObserver(observer: LifecycleObserver) {}

                override fun removeObserver(observer: LifecycleObserver) {}

                override fun getCurrentState(): State {
                    return State.STARTED
                }
            }
        }

        override fun hashCode(): Int {
            return 0
        }
    }
    private val observers = ConcurrentHashMap<LifecycleOwner?, MutableSet<ObserverWrapper<in T>>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        observeInternal(owner, observer)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        observe(fakeOwner, observer)
    }

    private fun observeInternal(owner: LifecycleOwner, observer: Observer<in T>) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }

        val wrapper = ObserverWrapper(observer)
        val set = observers[owner]
        set?.apply {
            this.find { it.observer == observer }?.let {
                doObserve(owner, it)
                return
            }
            add(wrapper)
        } ?: run {
            val newSet =
                Collections.newSetFromMap(ConcurrentHashMap<ObserverWrapper<in T>, Boolean>())
            newSet.add(wrapper)
            observers[owner] = newSet
        }
        doObserve(owner, wrapper)
    }

    private fun doObserve(owner: LifecycleOwner, observer: Observer<in T>) {
        when (owner) {
            fakeOwner -> super.observeForever(observer)
            else -> super.observe(owner, observer)
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        observers.remove(owner)
        super.removeObservers(owner)
    }

    override fun removeObserver(observer: Observer<in T>) {
        observers.forEach { entry ->
            entry.value.find { it == observer || it.observer == observer }?.let {
                entry.value.remove(it)
                super.removeObserver(it)
                if (entry.value.isEmpty()) {
                    observers.remove(entry.key)
                }
                return@forEach
            }
        }
    }

    override fun postValue(value: T) {
        observers.forEach { it.value.forEach { wrapper -> wrapper.newValue() } }
        super.postValue(value)
    }

    @MainThread
    override fun setValue(t: T?) {
        observers.forEach { it.value.forEach { wrapper -> wrapper.newValue() } }
        super.setValue(t)
    }

    private class ObserverWrapper<T>(
        val observer: Observer<T>
    ) : Observer<T> {

        private val pending = AtomicBoolean(false)

        override fun onChanged(t: T?) {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }

        fun newValue() {
            pending.set(true)
        }
    }
}
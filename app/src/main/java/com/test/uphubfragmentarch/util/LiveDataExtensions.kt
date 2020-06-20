package com.test.uphubfragmentarch.util

import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

fun <A> LiveData<A>.skip(n: Int): LiveData<A> {
    var count = n
    return MediatorLiveData<A>().apply {
        addSource(this@skip) {
            if (count <= 0) {
                this.value = it
            } else {
                count--
            }
        }
    }
}

fun <A, B> withLatest(a: LiveData<A>, b: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB
            if (localLastA != null && localLastB != null)
                this.value = Pair(localLastA, localLastB)
        }

        addSource(a) {
            lastA = it
            update()
        }

        addSource(b) {
            lastB = it
        }
    }
}

fun <A, B, C> combineLatest(
    a: LiveData<A>,
    b: LiveData<B>,
    combineFunction: (A, B) -> C
): LiveData<C> {
    return MediatorLiveData<C>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB
            if (localLastA != null && localLastB != null)
                this.value = combineFunction.invoke(localLastA, localLastB)
        }

        addSource(a) {
            lastA = it
            update()
        }
        addSource(b) {
            lastB = it
            update()
        }
    }
}

fun <A, B, C, D> combineLatest(
    a: LiveData<A>,
    b: LiveData<B>,
    c: LiveData<C>,
    combineFunction: (A, B, C) -> D
): LiveData<D> =
    combineLatest(a, combineLatest(b, c) { b, c ->
        Pair(b, c)
    }) { a, (b, c) ->
        combineFunction(a, b, c)
    }


fun <A, B, C> combineLatestAllowNull(
    a: LiveData<A>,
    b: LiveData<B>,
    combineFunction: (A?, B?) -> C
): LiveData<C> {
    return MediatorLiveData<C>().apply {
        var first = false
        var second = false
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB
            if (first && second)
                this.value = combineFunction.invoke(localLastA, localLastB)
        }

        addSource(a) {
            first = true
            lastA = it
            update()
        }

        addSource(b) {
            second = true
            lastB = it
            update()
        }
    }
}

fun <A, B> LiveData<A>.withLatestFrom(other: LiveData<B>): LiveData<Pair<A, B>> {
    return withLatest(this@withLatestFrom, other)
}


fun <A, B, C> LiveData<A>.combineLatestWith(
    other: LiveData<B>,
    transform: (A, B) -> C
): LiveData<C> {
    return combineLatest(this, other, transform)
}

fun <A, B, C, D> LiveData<A>.combineLatestWith(
    b: LiveData<B>,
    c: LiveData<C>,
    transform: (A, B, C) -> D
): LiveData<D> =
    combineLatest(this, b, c, transform)

fun <A, B> zipLiveData(a: LiveData<A>, b: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
        var lastA: A? = null
        var lastB: B? = null

        fun update() {
            val localLastA = lastA
            val localLastB = lastB
            if (localLastA != null && localLastB != null) {
                this.value = Pair(localLastA, localLastB)
                lastA = null
                lastB = null
            }
        }

        addSource(a) {
            lastA = it
            update()
        }

        addSource(b) {
            lastB = it
            update()
        }
    }
}

fun <A, B> LiveData<A>.zipWith(other: LiveData<B>): LiveData<Pair<A, B>> {
    return zipLiveData(this@zipWith, other)
}

fun <A> mergeLiveData(collection: Collection<LiveData<A>>): LiveData<A> {
    return MediatorLiveData<A>().apply {
        collection.forEach { data ->
            addSource(data) {
                this.value = it
            }
        }
    }
}

fun <A> LiveData<A>.mergeWith(other: LiveData<A>): LiveData<A> {
    return MediatorLiveData<A>().apply {
        addSource(this@mergeWith) {
            value = it
        }
        addSource(other) {
            value = it
        }
    }
}

fun <T, R> mediatorLiveData(
    source: LiveData<T>,
    block: MediatorLiveData<R>.(T) -> Unit
): LiveData<R> =
    MediatorLiveData<R>().apply {
        addSource(source) {
            this.block(it)
        }
    }

fun <T, R> LiveData<T>.scan(initialValue: R, accumulator: (R, T) -> R): LiveData<R> {
    val returnLiveData = MediatorLiveData<R>()
    returnLiveData.value = initialValue
    returnLiveData.addSource(this) {
        val result = accumulator.invoke(returnLiveData.value!!, this.value!!)
        returnLiveData.value = result
    }
    return returnLiveData
}

fun <T> LiveData<T>.doOnNext(doFunc: (T) -> Unit): LiveData<T> =
    this.map {
        doFunc(it)
        it
    }

fun <T, R> LiveData<T>.mapNotNull(mapper: (T) -> R?): LiveData<R> =
    this.map(mapper).filter { it != null }.map { it!! }

fun <T> LiveData<T>.filter(filter: (T) -> Boolean): LiveData<T> =
    MediatorLiveData<T>().apply {
        addSource(this@filter) {
            if (filter.invoke(it)) {
                this.value = it
            }
        }
    }

fun <T> LiveData<T>.toSingleEvent(): LiveData<T> {
    val result = SingleLiveEvent<T>()
    result.addSource(this) {
        result.value = it
    }
    return result
}

fun <T> LiveData<T?>.throttleLast(offsetMillis: Long, coroutineScope: CoroutineScope) =
    MediatorLiveData<T>().also { mld ->
        var tempValue: T? = null
        var currentJob: Job? = null
        val source = this

        mld.addSource(source) {
            if (tempValue == null || tempValue != source.value) {
                //cancel the last unfinished job
                currentJob?.cancel()
                //launch a coroutine on background
                currentJob = coroutineScope.launch(Dispatchers.IO) {
                    //wait the offset time, if another item is emitted it will be cancelled before the delay time ends
                    delay(offsetMillis)
                    //if the job was not cancelled it will emit the last item on the UI Thread
                    withContext(Dispatchers.Main) {
                        tempValue = source.value
                        mld.value = source.value
                    }
                }
            }
        }
    }

fun <T> LiveData<T?>.filterNotNull(): LiveData<T> =
    this.filter { it != null }.map { it!! }

fun <T> LiveData<T?>.filterNotNull(filter: (T) -> Boolean): LiveData<T> =
    this.filter { it != null && filter.invoke(it) }.map { it!! }

fun <T> LiveData<T>.distinctUntilChangedIgnoreNulls(): LiveData<T> =
    MediatorLiveData<T>().apply {
        var item: T? = null

        addSource(this@distinctUntilChangedIgnoreNulls) {
            if (it != null && it != item) {
                value = it
                item = it
            }
        }
    }

fun <T1, T2> LiveData<T1>.switchMap(function: (T1) -> LiveData<T2>): LiveData<T2> =
    Transformations.switchMap(this, function)

fun <T1, T2> LiveData<T1>.map(function: (T1) -> T2): LiveData<T2> {
    return MediatorLiveData<T2>().apply {
        addSource(this@map) {
            value = function.invoke(it)
        }
    }
}
//    Transformations.map(this, function)

fun <T> LiveData<T>.asMutable(): MutableLiveData<T> = MediatorLiveData<T>().also { mediator ->
    mediator.addSource(this) {
        mediator.set(it)
    }
}

fun <T> LiveData<T>.startWith(first: T): LiveData<T> = MediatorLiveData<T>().apply {
    set(first)
    addSource(this@startWith) { set(it) }
}

fun <T> LiveData<T>.toMutable(): MutableLiveData<T> = MediatorLiveData<T>().apply {
    addSource(this@toMutable) { set(it) }
}

fun <T1 : Any> LiveData<T1?>.notNull(): LiveData<T1> = MediatorLiveData<T1>().apply {
    addSource(this@notNull) { data ->
        value = data ?: return@addSource
    }
}

fun <T> MutableLiveData<T>.set(value: T?) {
    if (Looper.myLooper() == Looper.getMainLooper()) setValue(value)
    else postValue(value)
}

fun <T> MutableLiveData<T>.default(value: T): MutableLiveData<T> = this.also { set(value) }

fun MutableLiveData<Unit>.click() = set(Unit)

fun MutableLiveData<String>.click(message: String) = set(message)

inline fun <T> AppCompatActivity.observe(liveData: LiveData<T>, crossinline onChange: (T) -> Unit) {
    liveData.observe(this, Observer { onChange(it) })
}

inline fun <T> Fragment.observe(liveData: LiveData<T>, crossinline onChange: (T) -> Unit) {
    liveData.observe(viewLifecycleOwner, Observer {
        onChange(it ?: return@Observer)
    })
}

inline fun <T> Fragment.observe(flow: Flow<T>, crossinline onChange: (T) -> Unit) {
    flow.asLiveData(viewLifecycleOwner.lifecycleScope.coroutineContext)
        .observe(viewLifecycleOwner, Observer {
            onChange(it ?: return@Observer)
        })
}

inline fun <T> AppCompatActivity.observe(flow: Flow<T>, crossinline onChange: (T) -> Unit) {
    flow.asLiveData(lifecycleScope.coroutineContext).observe(this, Observer {
        onChange(it ?: return@Observer)
    })
}


fun <T> T.toLiveData() = MutableLiveData(this)

fun <T> LiveData<T>.takeFirst() = MediatorLiveData<T>().also {
    it.addSource(this) { newValue ->
        if (it.value == null) it.value = newValue
    }
}

class LiveDataPool<K, T> : MediatorLiveData<Map<K, T>>() {

    private val sources = mutableMapOf<K, MutableLiveData<T>>()

    init {
        updateValue()
    }

    operator fun get(key: K): MutableLiveData<T> =
        if (key in sources) sources[key]!! else {
            MutableLiveData<T>().also { source ->
                sources.put(key, source)
                addSource(source) {
                    updateValue()
                }
            }
        }

    private fun updateValue() {
        value = mutableMapOf<K, T>().apply {
            sources.forEach {
                it.value.value?.let { value -> put(it.key, value) }
            }
        }
    }
}

inline fun MutableLiveData<Boolean>.trackProgress(callback: () -> Unit) {
    set(true)
    callback()
    set(false)
}
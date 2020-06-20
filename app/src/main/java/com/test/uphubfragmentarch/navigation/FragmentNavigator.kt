package com.test.uphubfragmentarch.navigation

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.test.uphubfragmentarch.util.guard
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList


open class FragmentNavigator<T : FragmentNavigator.FragmentRoute>(
    protected val context: Context,
    protected val fragmentManager: FragmentManager,
    @IdRes protected val containerId: Int
) : Navigator<T> {
    protected val backStack = Stack<Transaction>()
    private val stackBundle = Bundle()

    //Used in case when fragment transaction is not in backstack,
    //but when performing popBackStack we need to get enter, exit anims from recent transaction
    protected var lastNoBackStackTransaction: Transaction? = null

    private val TAG = "FragmentNavigator $containerId"

    override fun popBackStack(): Boolean {
        if (!backStack.isEmpty()) {
            val initialStackSize = backStack.size
            val currentActive = getCurrentFragment() ?: return false
            //If there is a transaction which is not in backstack, selecting it's animation to act
            //as a pop one
            val popAnimation =
                backStack.peek().animation.let { lastNoBackStackTransaction?.animation ?: it }

            val transaction = fragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    popAnimation.popEnterAnim,
                    popAnimation.popExitAnim
                )
                .remove(currentActive)

            val pop = backStack.pop()
            lastNoBackStackTransaction?.let {
                //In addition transaction which is last on backstack needs to be removed and
                //not managed by FragmentManager any more
                getFragment(pop.uuid)?.let {
                    transaction.remove(it)
                }
            }

            if (initialStackSize == 1) {
                transaction.commitNow()
                return true
            } else {
                changeDrawingOrder(currentActive, true)
                val target = getFragment(backStack.peek().uuid) ?: kotlin.run {
                    val newFragment = fragmentManager.fragmentFactory.instantiate(
                        context.classLoader,
                        backStack.peek().fragmentName
                    )

                    val top = backStack.peek()

                    top.bundle?.let {
                        newFragment.arguments = it
                    }

                    transaction
                        .replace(containerId, newFragment)
                        .commitNow()

                    fragmentManager.putFragment(stackBundle, top.uuid, newFragment)
                    return true
                }

                transaction
                    .attach(target)
                    .commitNow()

                return true
            }
        }
        return false
    }

    fun getCurrentFragment(): Fragment? {
        return fragmentManager.findFragmentByTag(TAG)
    }

    private fun getFragmentContainer(f: Fragment): ViewGroup? {
        return (f.view?.parent as? ViewGroup)
    }

    private fun changeDrawingOrder(f: Fragment, pop: Boolean) {
        when (val container = getFragmentContainer(f)) {
            is FragmentContainer -> container.setDrawDisappearingViewsLast(!pop)
        }
    }

    protected fun getFragment(key: String): Fragment? {
        return guard { fragmentManager.getFragment(stackBundle, key) }
    }

    override fun navigate(route: T, intermediateRoutes: Collection<T>) {
        val current = getCurrentFragment()?.also {
            changeDrawingOrder(it, false)
        }
        val fm = fragmentManager
        val newFragment =
            fm.fragmentFactory.instantiate(context.classLoader, route.destination.name)

        route.bundle?.let {
            newFragment.arguments = it
        }

        val intermediateFragments =
            intermediateRoutes.map { intRoute ->
                fm.fragmentFactory.instantiate(
                    context.classLoader,
                    intRoute.destination.name
                ).also { frag ->
                    intRoute.bundle?.let {
                        frag.arguments = it
                    }
                }
            }

        val transaction = with(fm.beginTransaction()) {
            setCustomAnimations(
                route.animation.enterAnim,
                route.animation.exitAnim
            )
            current?.let {
                //Fragments which are not in backstack are not saved to FM and should be removed
                //instead of detach because there will be no pop transaction which would require
                //fragment re-attachment
                if (lastNoBackStackTransaction != null || route.replaceTop
                ) {
                    //Pop backstack if current transaction's addToBackStack set to true
                    //and it appeared to be on top of stack
                    if (route.replaceTop && lastNoBackStackTransaction == null) {
                        backStack.pop()
                    }
                    remove(it)
                } else {
                    detach(it)
                }
            }

            intermediateRoutes.forEachIndexed { index, t ->
                replace(containerId, intermediateFragments[index], TAG)
                detach(intermediateFragments[index])
            }

            replace(
                containerId,
                newFragment,
                TAG
            )
        }

        transaction.commitNow()

        intermediateRoutes.forEachIndexed { index, r ->
            val trans = Transaction(
                fragmentName = r.destination.name,
                animation = r.animation,
                bundle = r.bundle
            )
            fm.putFragment(stackBundle, trans.uuid, intermediateFragments[index])
            backStack.add(trans)
        }

        Transaction(
            fragmentName = route.destination.name,
            animation = route.animation,
            bundle = route.bundle
        ).let {
            if (route.addToBackStack) {
                fm.putFragment(stackBundle, it.uuid, newFragment)
                backStack.add(it)
                lastNoBackStackTransaction = null
            } else {
                lastNoBackStackTransaction = it
            }
        }
    }

    override fun navigate(route: T) {
        navigate(route, emptyList())
    }

    companion object {
        const val LAST_NO_BACK_STACK_FRAGMENT = "last_no_back_stack"
        const val FRAGMENTS = "fragment_saved_state"
        const val TRANSACTIONS = "transactions_state"
    }

    @CallSuper
    open fun saveInstanceState(bundle: Bundle) {
        bundle.putBundle(FRAGMENTS, stackBundle)
        val stack = ArrayList<Transaction>().apply {
            addAll(backStack)
        }
        bundle.putParcelableArrayList(TRANSACTIONS, stack)
        lastNoBackStackTransaction?.let {
            bundle.putParcelable(LAST_NO_BACK_STACK_FRAGMENT, it)
        }
    }

    @CallSuper
    open fun restoreInstanceState(bundle: Bundle) {
        backStack.clear()
        val fragments = bundle.getBundle(FRAGMENTS)
        lastNoBackStackTransaction = bundle.getParcelable(LAST_NO_BACK_STACK_FRAGMENT)
        val transactions = bundle.getParcelableArrayList<Transaction>(TRANSACTIONS)
        if (fragments != null && transactions != null) {
            backStack.addAll(transactions)
            stackBundle.clear()
            stackBundle.putAll(fragments)
        }
    }

    open class FragmentRoute(
        private val fragmentClass: Class<out Fragment>,
        open val animation: Animation,
        open val addToBackStack: Boolean = false,
        open val replaceTop: Boolean = false,
        open val bundle: Bundle? = null
    ) : Navigator.Route {
        override val destination: FragmentDestination
            get() = FragmentDestination(fragmentClass)
    }

    data class FragmentDestination(val fragmentClass: Class<out Fragment>) :
        Navigator.Destination(fragmentClass.name)

    @Parcelize
    data class Transaction(
        val uuid: String = UUID.randomUUID().toString(),
        val fragmentName: String, //Unique
        val animation: Animation,
        val bundle: Bundle? = null
    ) : Parcelable

    @Parcelize
    data class Animation(
        val enterAnim: Int = 0,
        val exitAnim: Int = 0,
        val popEnterAnim: Int = 0,
        val popExitAnim: Int = 0
    ) : Parcelable
}
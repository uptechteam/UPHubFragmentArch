package com.test.uphubfragmentarch.navigation

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import java.util.*

class FragmentContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mDisappearingFragmentChildren: ArrayList<View>? = null
    private var mTransitioningFragmentViews: ArrayList<View>? = null

    // Used to indicate whether the FragmentContainerView should override the default ViewGroup
    // drawing order.
    private var mDrawDisappearingViewsFirst = true

    /**
     * {@inheritDoc}
     *
     *
     * The sys ui flags must be set to enable extending the layout into the window insets.
     */
    @RequiresApi(20)
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            // Give child views fresh insets.
            child.dispatchApplyWindowInsets(WindowInsets(insets))
        }
        return insets
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (mDrawDisappearingViewsFirst && mDisappearingFragmentChildren != null) {
            for (i in mDisappearingFragmentChildren!!.indices) {
                super.drawChild(canvas, mDisappearingFragmentChildren!![i], drawingTime)
            }
        }
        super.dispatchDraw(canvas)
    }

    override fun drawChild(
        canvas: Canvas,
        child: View,
        drawingTime: Long
    ): Boolean {
        if (mDrawDisappearingViewsFirst && mDisappearingFragmentChildren != null && mDisappearingFragmentChildren!!.size > 0
        ) {
            // If the child is disappearing, we have already drawn it so skip.
            if (mDisappearingFragmentChildren!!.contains(child)) {
                return false
            }
        }
        return super.drawChild(canvas, child, drawingTime)
    }

    override fun startViewTransition(view: View) {
        if (view.parent === this) {
            if (mTransitioningFragmentViews == null) {
                mTransitioningFragmentViews = ArrayList()
            }
            mTransitioningFragmentViews!!.add(view)
        }
        super.startViewTransition(view)
    }

    override fun endViewTransition(view: View) {
        if (mTransitioningFragmentViews != null) {
            mTransitioningFragmentViews!!.remove(view)
            if (mDisappearingFragmentChildren != null
                && mDisappearingFragmentChildren!!.remove(view)
            ) {
                mDrawDisappearingViewsFirst = true
            }
        }
        super.endViewTransition(view)
    }

    // Used to indicate the container should change the default drawing order.
    fun setDrawDisappearingViewsLast(drawDisappearingViewsFirst: Boolean) {
        mDrawDisappearingViewsFirst = drawDisappearingViewsFirst
    }

    override fun removeViewAt(index: Int) {
        val view = getChildAt(index)
        addDisappearingFragmentView(view)
        super.removeViewAt(index)
    }

    override fun removeViewInLayout(view: View) {
        addDisappearingFragmentView(view)
        super.removeViewInLayout(view)
    }

    override fun removeView(view: View) {
        addDisappearingFragmentView(view)
        super.removeView(view)
    }

    override fun removeViews(start: Int, count: Int) {
        for (i in start until start + count) {
            val view = getChildAt(i)
            addDisappearingFragmentView(view)
        }
        super.removeViews(start, count)
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        for (i in start until start + count) {
            val view = getChildAt(i)
            addDisappearingFragmentView(view)
        }
        super.removeViewsInLayout(start, count)
    }

    override fun removeAllViewsInLayout() {
        for (i in childCount - 1 downTo 0) {
            val view = getChildAt(i)
            addDisappearingFragmentView(view)
        }
        super.removeAllViewsInLayout()
    }

    override fun removeDetachedView(
        child: View,
        animate: Boolean
    ) {
        if (animate) {
            addDisappearingFragmentView(child)
        }
        super.removeDetachedView(child, animate)
    }

    /**
     * This method adds a [View] to the list of disappearing views only if it meets the
     * proper conditions to be considered a disappearing view.
     *
     * @param v [View] that might be added to list of disappearing views
     */
    private fun addDisappearingFragmentView(v: View) {
        if (v.animation != null || (mTransitioningFragmentViews != null
                    && mTransitioningFragmentViews!!.contains(v))
        ) {
            if (mDisappearingFragmentChildren == null) {
                mDisappearingFragmentChildren = ArrayList()
            }
            mDisappearingFragmentChildren!!.add(v)
        }
    }
}
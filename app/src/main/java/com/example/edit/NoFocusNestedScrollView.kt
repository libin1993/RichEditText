package com.example.edit

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView


/**
 * @author: libin
 * @date: 2023/12/14 17:52
 * @description:ScrollView中EditText抢占焦点,导致ScrollView自动滚动
 */
class NoFocusNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {
    override fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect?): Int {
        return 0
    }
}
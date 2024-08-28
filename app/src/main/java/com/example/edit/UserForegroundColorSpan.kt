package com.example.edit


import android.text.style.ForegroundColorSpan

/**
 * @author: libin
 * @date: 2024/8/26 11:28
 * @description:
 */
open class UserForegroundColorSpan(val mentionUserBean: MentionUserBean, val color: Int) :
    ForegroundColorSpan(color)
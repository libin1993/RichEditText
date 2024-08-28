package com.example.edit

import android.text.style.ForegroundColorSpan

/**
 * @author: libin
 * @date: 2024/8/26 11:28
 * @description:@用户高亮
 */
open class TopicForegroundColorSpan(val topicBean: TopicBean, val color: Int) :
    ForegroundColorSpan(color)
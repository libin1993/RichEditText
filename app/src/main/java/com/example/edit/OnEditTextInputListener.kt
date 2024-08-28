package com.example.edit

/**
 * @author: libin
 * @date: 2024/8/27 15:13
 * @description:文本框输入#、@以及字数变化回调
 */
interface OnEditTextInputListener {
    fun notifyAt()
    fun notifyTopic()
    fun onTextLengthChanged(count:Int)
}

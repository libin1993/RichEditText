package com.example.edit

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.use

/**
 * @author: libin
 * @date: 2024/8/27 14:15
 * @description:插入话题、@相同名称用户
 */
class RichEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) :
    AppCompatEditText(context, attrs, defStyleAttr) {
    private var mRichMaxLength = 9999 //最长输入
    private var mOnEditTextInputListener: OnEditTextInputListener? = null //输入监控回调
    private var mTopicColor: Int = 0 //话题颜色
    private var mUserColor: Int = 0 //用户颜色
    private var mTopicOnce = false //同一话题最多只能选择一次
    private var mUserOnce = true //同一用户最多只能@一次
    private var mLastSelectedRange: Range? = null
    private var mRangeArrayList: MutableList<Range>? = null
    private var isSelected: Boolean = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RichEditText).use { ta ->
            mRichMaxLength = ta.getInteger(R.styleable.RichEditText_richMaxLength, 9999)
            mUserColor = ta.getColor(
                R.styleable.RichEditText_richUserColor,
                Color.RED
            )
            mTopicColor = ta.getColor(
                R.styleable.RichEditText_richTopicColor,
                Color.RED
            )
            mTopicOnce = ta.getBoolean(
                R.styleable.RichEditText_richTopicOnce,
                false
            )
            mUserOnce = ta.getBoolean(
                R.styleable.RichEditText_richUserOnce,
                true
            )
        }
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(mRichMaxLength))
        mRangeArrayList = mutableListOf()
        initListener()
    }


    /**
     * 监听字符变化
     */
    private fun initListener() {
        addTextChangedListener(object : TextWatcher {
            private var beforeCount: Int = 0

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                beforeCount = s.toString().length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val content = s.toString()
                if (content.length - beforeCount == 1 && selectionEnd > 0 && content[selectionEnd - 1] == '@') {
                    mOnEditTextInputListener?.notifyAt()
                } else if (content.length - beforeCount == 1 && selectionEnd > 0 && content[selectionEnd - 1] == '#') {
                    mOnEditTextInputListener?.notifyTopic()
                }
            }

            override fun afterTextChanged(s: Editable) {
                mOnEditTextInputListener?.onTextLengthChanged(s.length)
                updateSpans(s)
            }
        })

    }


    /**
     * 初始化插入的文本
     *
     * @param text     需要处理的文本
     * @param topicList 需要处理的话题
     * @param userList 需要处理的@某人列表
     */
    fun initText(
        text: String?,
        topicList: List<TopicBean>?,
        userList: List<MentionUserBean>?
    ) {
        if (text.isNullOrEmpty()) {
            return
        }

        if (userList.isNullOrEmpty() || topicList.isNullOrEmpty()) {
            setText(text)
        } else {
            val ssb = SpannableStringBuilder(text)
            if (!topicList.isNullOrEmpty()) {
                initTopic(text, ssb, topicList)
            }

            if (!userList.isNullOrEmpty()) {
                initUser(text, ssb, userList)
            }
            setText(ssb)
        }
        setSelection(getText()!!.length)
    }


    /**
     * 初始化话题
     * @param text      输入文本
     * @param topicList 话题列表
     */
    private fun initTopic(
        text: String,
        ssb: SpannableStringBuilder,
        topicList: List<TopicBean>
    ) {
        val contentLength = text.length
        for (topicBean in topicList) {
            val start = topicBean.start ?: 0 //话题起始位置
            val end = topicBean.end ?: 0 //话题结束位置
            if (end <= contentLength && text.substring(start, end) == topicBean.topic_name) {
                ssb.setSpan(
                    TopicForegroundColorSpan(topicBean, mTopicColor),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    /**
     * 初始化@用户
     *
     * @param text 输入文本
     * @param ssb 处理过的文本
     * @param userList  用户列表
     */
    private fun initUser(
        text: String,
        ssb: SpannableStringBuilder,
        userList: List<MentionUserBean>
    ) {
        val contentLength = text.length
        for (userModel in userList) {
            val start = userModel.start ?: 0 //当前@用户起始位置
            val end = userModel.end ?: 0 //当前@用户结束位置
            if (end <= contentLength && text.substring(start + 1, end) == userModel.nick_name) {
                ssb.setSpan(
                    UserForegroundColorSpan(userModel, mUserColor),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }


    /**
     * 输入#插入话题
     * @param topicBean 话题
     * @param insertByInput 是否输入#插入话题
     */
    fun insertTopic(topicBean: TopicBean?, insertByInput: Boolean? = false) {
        //先移除#
        if (insertByInput == true) {
            if (selectionEnd == 0) {
                text!!.delete(0, 1)
            } else {
                val index = text.toString().indexOf("#", selectionEnd - 1)
                if (index != -1) {
                    text!!.delete(index, index + 1)
                }
            }
        }
        if (topicBean == null) {
            return
        }
        val curString = text.toString()
        if (curString.length + topicBean.topic_name.length > mRichMaxLength) {
            Toast.makeText(context, "最长可输入${mRichMaxLength}个字符", Toast.LENGTH_SHORT).show()
            return
        }

        //同一话题最多只能选择一次
        if (mTopicOnce && !text.isNullOrEmpty()) {
            val topicArray = text!!.getSpans(
                0, text!!.length,
                TopicForegroundColorSpan::class.java
            )
            if (!topicArray.isNullOrEmpty()) {
                var isContain = false
                run loop@{
                    topicArray.forEach {
                        if (it.topicBean.topic_id == topicBean.topic_id) {
                            Toast.makeText(context, "你已经选择过该话题啦", Toast.LENGTH_SHORT).show()
                            isContain = true
                            return@loop
                        }
                    }
                }
                if (isContain) {
                    return
                }
            }
        }

        val index = selectionStart
        text!!.insert(index, topicBean.topic_name)
        text!!.setSpan(
            TopicForegroundColorSpan(topicBean, mTopicColor),
            index,
            index + topicBean.topic_name.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setSelection(index + topicBean.topic_name.length)
    }

    /**
     * 插入用户
     * @param mentionUserBean 用户
     * @param insertByInput 是否输入@插入用户
     */
    fun insertUser(mentionUserBean: MentionUserBean?, insertByInput: Boolean? = false) {
        if (insertByInput == true) {
            if (selectionEnd == 0) {
                text!!.delete(0, 1)
            } else {
                val index = text.toString().indexOf("@", selectionEnd - 1)
                if (index != -1) {
                    text!!.delete(index, index + 1)
                }
            }
        }

        if (mentionUserBean == null) {
            return
        }
        val curString = text.toString()
        if (curString.length + mentionUserBean.nick_name.length + 2 > mRichMaxLength) {
            Toast.makeText(context, "最长可输入${mRichMaxLength}个字符", Toast.LENGTH_SHORT).show()
            return
        }
        //同一用户只能@一次
        if (mUserOnce && !text.isNullOrEmpty()) {
            val userArray = text!!.getSpans(
                0, text!!.length,
                UserForegroundColorSpan::class.java
            )
            if (!userArray.isNullOrEmpty()) {
                var isContain = false
                run loop@{
                    userArray.forEach {
                        if (it.mentionUserBean.account_id == mentionUserBean.account_id) {
                            Toast.makeText(context, "你已经@过Ta啦", Toast.LENGTH_SHORT).show()
                            isContain = true
                            return@loop
                        }
                    }
                }
                if (isContain) {
                    return
                }
            }
        }

        val index = selectionStart
        text!!.insert(index, "@${mentionUserBean.nick_name}")
        text!!.setSpan(
            UserForegroundColorSpan(
                mentionUserBean,
                mUserColor
            ),
            index,
            index + mentionUserBean.nick_name.length + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setSelection(index + mentionUserBean.nick_name.length + 1)
    }


    /**
     * 查询所有话题
     * @return MutableList<String>?
     */
    fun getTopicList(): MutableList<TopicBean>? {
        val text = text ?: return null
        val topicArray = text.getSpans(
            0, text.length,
            TopicForegroundColorSpan::class.java
        )
        if (topicArray.isNullOrEmpty()) {
            return null
        }
        val topicList = mutableListOf<TopicBean>()
        topicArray.mapTo(topicList) {
            TopicBean(
                it.topicBean.topic_name,
                it.topicBean.topic_id,
                text.getSpanStart(it),
                text.getSpanEnd(it)
            )
        }
        return topicList
    }


    /**
     * 查询所有@用户
     * @return MutableList<MentionUserBean>?
     */
    fun getUserList(): MutableList<MentionUserBean>? {
        val text = text ?: return null
        val userArray = text.getSpans(
            0, text.length,
            UserForegroundColorSpan::class.java
        )
        if (userArray.isNullOrEmpty()) {
            return null
        }
        val userList = mutableListOf<MentionUserBean>()
        userArray.mapTo(userList) {
            MentionUserBean(
                it.mentionUserBean.nick_name,
                it.mentionUserBean.account_id,
                text.getSpanStart(it),
                text.getSpanEnd(it)
            )
        }
        return userList
    }


    /**
     * 话题颜色
     *
     * @param topicColor
     */
    fun setTopicColor(topicColor: Int) {
        this.mTopicColor = topicColor
    }

    /**
     * @用户颜色
     *
     * @param userColor
     */
    fun setUserColor(userColor: Int) {
        this.mUserColor = userColor
    }


    /**
     * 输入#、@回调
     *
     * @param onEditTextInputListener 跳转回调
     */
    fun setOnEditTextInputListener(onEditTextInputListener: OnEditTextInputListener?) {
        this.mOnEditTextInputListener = onEditTextInputListener
    }


    fun getEditTextMaxLength(): Int = mRichMaxLength


    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection =
        HackInputConnection(super.onCreateInputConnection(outAttrs), true, this)

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        //avoid infinite recursion after calling setSelection()
        if (mLastSelectedRange != null && mLastSelectedRange!!.isEqual(selStart, selEnd)) {
            return
        }

        //if user cancel a selection of mention string, reset the state of 'mIsSelected'
        val closestRange = getRangeOfClosestMentionString(selStart, selEnd)
        if (closestRange != null && closestRange.to == selEnd) {
            isSelected = false
        }

        val nearbyRange = getRangeOfNearbyMentionString(selStart, selEnd) ?: return
        //if there is no mention string nearby the cursor, just skip

        //forbid cursor located in the mention string.
        if (selStart == selEnd) {
            setSelection(nearbyRange.getAnchorPosition(selStart))
        } else {
            if (selEnd < nearbyRange.to) {
                setSelection(selStart, nearbyRange.to)
            }
            if (selStart > nearbyRange.from) {
                setSelection(nearbyRange.from, selEnd)
            }
        }
    }

    /**
     * 长按选中输入会替换内容，但是高亮未更新，需校验更新高亮内容
     * @param editable Editable
     */
    private fun updateSpans(editable: Editable) {
        //reset state
        isSelected = false
        mRangeArrayList?.clear()
        val topicArray = editable.getSpans(
            0, editable.length,
            TopicForegroundColorSpan::class.java
        )
        if (!topicArray.isNullOrEmpty()) {
            topicArray.forEach {
                val spanStart = editable.getSpanStart(it)
                val spanEnd = editable.getSpanEnd(it)
                if (spanStart >= 0 && spanEnd <= editable.length && editable.substring(
                        spanStart,
                        spanEnd
                    ) == it.topicBean.topic_name
                ) {
                    mRangeArrayList?.add(Range(editable.getSpanStart(it), editable.getSpanEnd(it)))
                } else {
                    editable.removeSpan(it)
                }
            }
        }
        val userArray = editable.getSpans(
            0, editable.length,
            UserForegroundColorSpan::class.java
        )
        if (!userArray.isNullOrEmpty()) {
            userArray.forEach {
                val spanStart = editable.getSpanStart(it)
                val spanEnd = editable.getSpanEnd(it)
                if (spanStart >= 0 && spanEnd <= editable.length && editable.substring(
                        spanStart,
                        spanEnd
                    ) == "@${it.mentionUserBean.nick_name}"
                ) {
                    mRangeArrayList?.add(Range(spanStart, spanEnd))
                } else {
                    editable.removeSpan(it)
                }
            }
        }
    }

    fun getRangeOfClosestMentionString(selStart: Int, selEnd: Int): Range? =
        mRangeArrayList?.firstOrNull { it.contains(selStart, selEnd) }

    private fun getRangeOfNearbyMentionString(selStart: Int, selEnd: Int): Range? =
        mRangeArrayList?.firstOrNull { it.isWrappedBy(selStart, selEnd) }

    //handle the deletion action for mention string, such as '@test'
    private inner class HackInputConnection(
        target: InputConnection,
        mutable: Boolean,
        editText: RichEditText
    ) : InputConnectionWrapper(target, mutable) {
        private val editText: EditText

        init {
            this.editText = editText
        }

        override fun sendKeyEvent(event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
                val selectionStart = editText.selectionStart
                val selectionEnd = editText.selectionEnd
                val closestRange = getRangeOfClosestMentionString(selectionStart, selectionEnd)
                if (closestRange == null) {
                    isSelected = false
                    return super.sendKeyEvent(event)
                }
                //if mention string has been selected or the cursor is at the beginning of mention string, just use default action(delete)
                if (isSelected || selectionStart == closestRange.from) {
                    isSelected = false
                    return super.sendKeyEvent(event)
                } else {
                    //select the mention string
                    isSelected = true
                    mLastSelectedRange = closestRange
                    setSelection(closestRange.from, closestRange.to)
                }
                return true
            }
            return super.sendKeyEvent(event)
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL
                    )
                ) && sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL)
                )
            } else if (beforeLength < 0 && afterLength == 0) {
                val selectionStart = editText.selectionStart
                val selectionEnd = editText.selectionEnd
                if (selectionStart == selectionEnd) {
                    setSelection(selectionStart - beforeLength, selectionStart - beforeLength)
                    super.deleteSurroundingText(-beforeLength, afterLength)
                }
            }
            return super.deleteSurroundingText(beforeLength, afterLength)
        }
    }

    //helper class to record the position of mention string in EditText
    inner class Range(internal var from: Int, internal var to: Int) {

        fun isWrappedBy(start: Int, end: Int): Boolean =
            start in (from + 1) until to || end in (from + 1) until to

        fun contains(start: Int, end: Int): Boolean = from <= start && to >= end

        fun isEqual(start: Int, end: Int): Boolean =
            from == start && to == end || from == end && to == start

        fun getAnchorPosition(value: Int): Int {
            return if (value - from - (to - value) >= 0) {
                to
            } else {
                from
            }
        }
    }

}
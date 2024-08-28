package com.example.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.edit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    companion object {
        const val REQUEST_USER_CODE_INPUT = 1111
        const val REQUEST_USER_CODE_CLICK = 2222
        const val REQUEST_TOPIC_CODE_INPUT = 3333
        const val REQUEST_TOPIC_CODE_CLICK = 4444
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initView()
    }

    private fun initView() {
        mBinding.etContent.setOnEditTextInputListener(object : OnEditTextInputListener {
            override fun notifyAt() {
                val intent = Intent(this@MainActivity, UserListActivity::class.java)
                startActivityForResult(intent, REQUEST_USER_CODE_INPUT)
            }

            override fun notifyTopic() {
                val intent = Intent(this@MainActivity, TopicListActivity::class.java)
                startActivityForResult(intent, REQUEST_TOPIC_CODE_INPUT)
            }

            override fun onTextLengthChanged(count: Int) {
                mBinding.tvCount.text = "${count}/${mBinding.etContent.getEditTextMaxLength()}"
            }
        })

        mBinding.tvSelectTopic.setOnClickListener(this)
        mBinding.tvSelectUser.setOnClickListener(this)
        mBinding.btnUser.setOnClickListener(this)
        mBinding.btnTopic.setOnClickListener(this)
        mBinding.btnContent.setOnClickListener(this)
        mBinding.etContent.initText(
            "@1@嗨@3#1#哈#哼#2", mutableListOf(
                TopicBean("#1", "1", 6, 8), TopicBean("#2", "2", 12, 14)
            ), mutableListOf(
                MentionUserBean("1", "1", 0, 2), MentionUserBean("3", "3", 4, 6)
            )
        )
    }


    override fun onClick(view: View) {
        when (view) {
            mBinding.btnTopic -> {
                mBinding.tvTopic.text = mBinding.etContent.getTopicList().toString()
            }

            mBinding.tvSelectUser -> {
                val intent = Intent(this@MainActivity, UserListActivity::class.java)
                startActivityForResult(intent, REQUEST_USER_CODE_CLICK)
            }

            mBinding.btnContent -> {
                mBinding.tvContent.text = mBinding.etContent.text.toString()
            }

            mBinding.tvSelectTopic -> {
                val intent = Intent(this@MainActivity, TopicListActivity::class.java)
                startActivityForResult(intent, REQUEST_TOPIC_CODE_CLICK)
            }

            mBinding.btnUser -> {
                mBinding.tvUser.text = mBinding.etContent.getUserList().toString()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_USER_CODE_CLICK -> {
                if (resultCode == Activity.RESULT_OK) {
                    mBinding.etContent.insertUser(
                        data?.getSerializableExtra(
                            UserListActivity.DATA
                        ) as MentionUserBean
                    )
                }
            }

            REQUEST_TOPIC_CODE_CLICK -> {
                if (resultCode == Activity.RESULT_OK) {
                    mBinding.etContent.insertTopic(
                        data?.getSerializableExtra(
                            TopicListActivity.DATA
                        ) as TopicBean
                    )
                }
            }

            REQUEST_USER_CODE_INPUT -> mBinding.etContent.insertUser(
                data?.getSerializableExtra(
                    UserListActivity.DATA
                ) as MentionUserBean?, true
            )

            REQUEST_TOPIC_CODE_INPUT -> mBinding.etContent.insertTopic(
                data?.getSerializableExtra(
                    TopicListActivity.DATA
                ) as TopicBean?, true
            )
        }

    }
}

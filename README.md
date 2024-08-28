

## 类似抖音、小红书输入框，支持@相同名称用户、#话题
## 参考 https://github.com/CarGuo/GSYRickText

## DEMO效果图
<img src="https://github.com/libin1993/RichEditText/blob/main/demo.jpg" width="295px" height="643px"/>

## 使用方式
```
mBinding.etContent.initText(
            "@1@嗨@3#1#哈#哼#2", mutableListOf(
                TopicBean("#1", "1", 6, 8), TopicBean("#2", "2", 12, 14)
            ), mutableListOf(
                MentionUserBean("1", "1", 0, 2), MentionUserBean("3", "3", 4, 6)
            )
        )
        
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
```


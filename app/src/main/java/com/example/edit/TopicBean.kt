package com.example.edit

import java.io.Serializable

/**
 * @author: libin
 * @date: 2024/8/27 15:10
 * @description:话题
 */
class TopicBean(
    var topic_name: String,
    var topic_id: String,
    var start: Int? = 0, //起始位置
    var end: Int? = 0 //结束位置
) : Serializable {

    override fun toString(): String {
        return "TopicBean(topic_name='$topic_name', topic_id='$topic_id', start=$start, end=$end)"
    }
}
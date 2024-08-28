package com.example.edit

import java.io.Serializable

/**
 * @author: libin
 * @date: 2024/8/27 15:10
 * @description:@用户
 */
data class MentionUserBean(
    var nick_name: String,
    var account_id: String,
    var start: Int? = 0, //起始位置
    var end: Int? = 0 //结束位置
) : Serializable {

    override fun toString(): String {
        return "MentionUserBean(nick_name='$nick_name', account_id='$account_id', start=$start, end=$end)"
    }
}

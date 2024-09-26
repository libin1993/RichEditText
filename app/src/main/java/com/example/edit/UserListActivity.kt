package com.example.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.edit.databinding.ActivityUserListBinding


class UserListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userList = mutableListOf(
            MentionUserBean("1", "1000")
        )
        for (i in 0..100) {
            userList.add(MentionUserBean(i.toString(), i.toString()))
        }

        val adapter = ArrayAdapter(this, R.layout.layout_user_list_item, userList)
        binding.userList.adapter = adapter
        binding.userList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val intent = Intent()
                intent.putExtra(DATA, userList[position])
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    companion object {
        const val DATA = "data"
    }

}

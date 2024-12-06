package com.example.gta.view.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gta.R


class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // UI 요소 초기화
        val messageInput: EditText = findViewById(R.id.message_input)
        val sendIcon: ImageView = findViewById(R.id.send_icon)
        val chatContainer: LinearLayout = findViewById(R.id.chatContainer)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

        // 아이콘 클릭 이벤트
        sendIcon.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessage(chatContainer, message)
                messageInput.text.clear()

                // 스크롤뷰 아래로 이동
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }
        }
    }

    private fun addMessage(container: LinearLayout, message: String) {
        val newMessageView = layoutInflater.inflate(R.layout.chat_item_right, container, false)

        // 메시지 내용 설정
        val messageTextView: TextView = newMessageView.findViewById(R.id.messageTextView)
        messageTextView.text = message

        // 컨테이너에 추가
        container.addView(newMessageView)
    }

}
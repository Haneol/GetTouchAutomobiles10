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
import com.example.gta.data.model.ChatMessage


class ChatActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance() // Firebase Database 초기화
    private val chatRef = database.reference.child("chats") // "chats" 노드 참조

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // UI 요소 초기화
        val messageInput: EditText = findViewById(R.id.message_input)
        val sendIcon: ImageView = findViewById(R.id.send_icon)
        val chatContainer: LinearLayout = findViewById(R.id.chatContainer)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

        // 메시지 전송
        sendIcon.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                val chatMessage = ChatMessage(
                    id = "",
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
                sendMessageToFirebase(chatMessage)
                messageInput.text.clear()
            }
        }

        // Firebase에서 메시지 읽기
        loadMessagesFromFirebase(chatContainer, scrollView)
    }

    // Firebase에 메시지 저장
    private fun sendMessageToFirebase(chatMessage: ChatMessage) {
        chatRef.push().setValue(chatMessage)
    }

    // Firebase에서 메시지 읽기
    private fun loadMessagesFromFirebase(container: LinearLayout, scrollView: ScrollView) {
        chatRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews() // 기존 메시지 초기화

                for (messageSnapshot in snapshot.children) {
                    val chatMessage = messageSnapshot.getValue(ChatMessage::class.java)
                    chatMessage?.let {
                        addMessage(container, it.message)
                    }
                }

                // 스크롤뷰 아래로 이동
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
            }
        })
    }

    // 메시지 추가 (UI 업데이트)
    private fun addMessage(container: LinearLayout, message: String) {
        val newMessageView = layoutInflater.inflate(R.layout.chat_item_right, container, false)

        // 메시지 내용 설정
        val messageTextView: TextView = newMessageView.findViewById(R.id.messageTextView)
        messageTextView.text = message

        // 컨테이너에 추가
        container.addView(newMessageView)
    }
}
}
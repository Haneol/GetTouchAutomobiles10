package com.example.gta.view.activities

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChatActivity : AppCompatActivity() {
    private lateinit var chatRef: DatabaseReference
    private lateinit var consultRef: DatabaseReference // Consult 참조 추가
    private var consultId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val database = FirebaseDatabase.getInstance("https://gettouchautomobiles-default-rtdb.asia-southeast1.firebasedatabase.app/")
        consultId = intent.getStringExtra("consultId") ?: "defaultRoom"
        chatRef = database.reference.child("chats").child(consultId!!)
        consultRef = database.reference.child("consults") // Consult 데이터베이스 참조 초기화

        val messageInput: EditText = findViewById(R.id.message_input)
        val sendIcon: ImageView = findViewById(R.id.send_icon)
        val chatContainer: LinearLayout = findViewById(R.id.chatContainer)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

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

        loadMessagesFromFirebase(chatContainer, scrollView)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveConsultToFirebase() // 뒤로가기 시 Consult 데이터 저장
    }

    private fun saveConsultToFirebase() {
        val title = "엔진오일 관련상담" // 원하는 제목을 설정
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // 현재 날짜

        val consultData = mapOf(
            "id" to consultId,
            "title" to title,
            "date" to date
        )

        // Firebase에 저장
        val consultRef = FirebaseDatabase.getInstance()
            .reference.child("consults").child(consultId!!)
        consultRef.setValue(consultData)
            .addOnSuccessListener {
                Log.d("Firebase", "Consult saved successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to save consult: ${exception.message}")
            }
    }

    private fun sendMessageToFirebase(chatMessage: ChatMessage) {
        chatRef.push().setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("Firebase", "Message sent successfully: ${chatMessage.message}")

                // 현재 날짜 가져오기
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Firebase Consult 노드 업데이트
                val consultRef = FirebaseDatabase.getInstance()
                    .reference.child("consults").child(consultId!!)
                consultRef.child("lastMessageDate").setValue(currentDate)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to send message: ${exception.message}")
            }
    }

    private fun loadMessagesFromFirebase(container: LinearLayout, scrollView: ScrollView) {
        chatRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()
                for (messageSnapshot in snapshot.children) {
                    val chatMessage = messageSnapshot.getValue(ChatMessage::class.java)
                    chatMessage?.let {
                        addMessage(container, it.message)
                    }
                }
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load messages: ${error.message}")
            }
        })
    }

    private fun addMessage(container: LinearLayout, message: String) {
        val newMessageView = layoutInflater.inflate(R.layout.chat_item_right, container, false)
        val messageTextView: TextView = newMessageView.findViewById(R.id.messageTextView)
        messageTextView.text = message
        container.addView(newMessageView)
    }
}
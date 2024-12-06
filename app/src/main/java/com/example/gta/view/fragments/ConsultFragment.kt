package com.example.gta.view.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gta.R
import com.example.gta.data.model.Consult
import com.example.gta.view.activities.ChatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*


class ConsultFragment : Fragment() {
    private lateinit var consultItemsLayout: LinearLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var consultRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_consult, container, false)

        consultItemsLayout = rootView.findViewById(R.id.consultItemsLayout)
        val fabAddConsult: FloatingActionButton = rootView.findViewById(R.id.fabAddConsult)

        // Firebase 초기화
        database = FirebaseDatabase.getInstance("https://gettouchautomobiles-default-rtdb.asia-southeast1.firebasedatabase.app/")
        consultRef = database.reference.child("consults")

        // Consult 데이터 로드
        loadConsultData()

        // + 버튼 클릭 이벤트
        fabAddConsult.setOnClickListener {
            createNewChatRoom()
        }

        return rootView
    }

    private fun loadConsultData() {
        consultRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                consultItemsLayout.removeAllViews() // 기존 뷰 초기화

                for (child in snapshot.children) {
                    val consult = child.getValue(Consult::class.java)
                    consult?.let { addConsultItem(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load consult data: ${error.message}")
            }
        })
    }

    private fun addConsultItem(consult: Consult) {
        // 기존 레이아웃에 새 Consult 아이템 추가
        val newConsultItem = LayoutInflater.from(requireContext()).inflate(R.layout.consult_item, consultItemsLayout, false)

        // 제목과 날짜를 Firebase 데이터로 업데이트
        val titleTextView: TextView = newConsultItem.findViewById(R.id.titleTextView)
        val dateTextView: TextView = newConsultItem.findViewById(R.id.dateTextView)

        titleTextView.text = consult.title
        dateTextView.text = consult.date

        // 아이템 클릭 시 ChatActivity로 이동
        newConsultItem.setOnClickListener {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("consultId", consult.id)
            startActivity(intent)
        }

        // 새로운 아이템을 레이아웃에 추가
        consultItemsLayout.addView(newConsultItem)
    }

    private fun createNewChatRoom() {
        // 새로운 채팅방 ID 생성
        val roomId = "room" + System.currentTimeMillis()

        // 현재 날짜 가져오기
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 새로운 Consult 데이터
        val consultData = mapOf(
            "id" to roomId,
            "title" to "엔진오일 관련상담",
            "date" to currentDate
        )

        consultRef.child(roomId).setValue(consultData)
            .addOnSuccessListener {
                Log.d("Firebase", "New chat room created: $roomId")

                // ChatActivity로 이동
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("consultId", roomId)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to create chat room: ${exception.message}")
            }
    }
}
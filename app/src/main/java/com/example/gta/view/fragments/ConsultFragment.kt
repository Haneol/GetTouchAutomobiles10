package com.example.gta.view.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.gta.R
import com.example.gta.view.activities.ChatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ConsultFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_consult, container, false)

        // FloatingActionButton을 찾고, 클릭 이벤트를 처리합니다.
        val fabAddConsult: FloatingActionButton = rootView.findViewById(R.id.fabAddConsult)
        fabAddConsult.setOnClickListener {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            startActivity(intent)
            addConsultItem(rootView) // 버튼 클릭 시 새로운 아이템을 추가
        }

        return rootView
    }

    // 새로운 ConsultItem을 동적으로 추가하는 메소드
    private fun addConsultItem(view: View) {
        // consult_items_layout은 ConsultItem을 추가할 레이아웃을 나타냅니다.
        val consultItemsLayout: LinearLayout = view.findViewById(R.id.consultItemsLayout)

        // 새로운 ConsultItem을 동적으로 생성하여 레이아웃에 추가
        val newConsultItem = LayoutInflater.from(requireContext()).inflate(R.layout.consult_item, consultItemsLayout, false)

        // 새로운 아이템에 값을 설정하는 부분
        val titleTextView: TextView = newConsultItem.findViewById(R.id.titleTextView)
        val dateTextView: TextView = newConsultItem.findViewById(R.id.dateTextView)

        // 데이터 연결
        titleTextView.text = "새로운 엔진오일 상담"
        dateTextView.text = "2024-12-05"

        // 새로운 아이템을 consultItemsLayout에 추가
        consultItemsLayout.addView(newConsultItem)
    }


}
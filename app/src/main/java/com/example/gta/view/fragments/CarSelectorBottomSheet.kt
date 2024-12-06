package com.example.gta.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gta.R
import com.example.gta.data.model.Car
import com.example.gta.data.repository.FirebaseRepository
import com.example.gta.view.adapters.CarSelectAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class CarSelectorBottomSheet : BottomSheetDialogFragment() {
    private lateinit var adapter: CarSelectAdapter
    private val repository = FirebaseRepository()

    interface CarSelectedListener {
        fun onCarSelected(car: Car)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_car_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCars)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = CarSelectAdapter(emptyList()) { selectedCar ->
            (parentFragment as? CarSelectedListener)?.onCarSelected(selectedCar)
            dismiss()
        }
        recyclerView.adapter = adapter

        // Firebase에서 차량 목록 가져오기
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            repository.observeUserCars(userId,
                onSuccess = { cars ->
                    adapter.updateCars(cars)
                },
                onFailure = { exception ->
                    Toast.makeText(context,
                        "차량 목록을 불러오는데 실패했습니다: ${exception.message}",
                        Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    companion object {
        const val TAG = "CarSelectorBottomSheet"
    }
}
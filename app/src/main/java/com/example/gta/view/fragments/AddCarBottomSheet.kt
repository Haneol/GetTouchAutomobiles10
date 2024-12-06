package com.example.gta.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.gta.R
import com.example.gta.data.model.Car
import com.example.gta.data.model.CarDataManager
import com.example.gta.data.repository.FirebaseRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.random.Random

class AddCarBottomSheet : BottomSheetDialogFragment() {
    private val repository = FirebaseRepository()

    interface CarAddListener {
        fun onCarAdded(car: Car)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_car, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Spinner 설정
        val spinner = view.findViewById<Spinner>(R.id.spinnerCarModel)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            CarDataManager.carList.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // 추가 버튼 클릭 리스너
        view.findViewById<Button>(R.id.buttonAddCar).setOnClickListener {
            val selectedCar = CarDataManager.carList[spinner.selectedItemPosition]
            val tag = view.findViewById<EditText>(R.id.editTextCarTag).text.toString()

            if (tag.isBlank()) {
                Toast.makeText(context, "태그를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 새로운 차량 객체 생성
            val newCar = Car(
                id = "", // Firebase에서 자동 생성됨
                name = "${selectedCar.name} - $tag",
                image = selectedCar.image,
                realTemperature = (Random.nextDouble() * 18 + 18).round(1), // 18.0 ~ 36.0
                distance = Random.nextInt(100, 901) // 100 ~ 900
            )

            // Firebase에 저장
            val userId = Firebase.auth.currentUser?.uid
            if (userId != null) {
                repository.addCar(
                    car = newCar,
                    onSuccess = {
                        Toast.makeText(context, "차량이 추가되었습니다", Toast.LENGTH_SHORT).show()
                        (parentFragment as? CarAddListener)?.onCarAdded(newCar)
                        dismiss()
                    },
                    onFailure = { exception ->
                        Toast.makeText(context, "오류가 발생했습니다: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun Double.round(decimals: Int): Double {
        return "%.${decimals}f".format(this).toDouble()
    }
}
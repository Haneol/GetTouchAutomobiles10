package com.example.gta.view.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.gta.R
import com.example.gta.data.model.Car
import com.example.gta.data.model.CarDataManager.carList
import com.example.gta.data.repository.FirebaseRepository
import com.example.gta.view.activities.PhotoActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class HomeFragment : Fragment(),
    ControlBottomSheet.CarControlListener,
    CarSelectorBottomSheet.CarSelectedListener,
    AddCarBottomSheet.CarAddListener {
    private var currentCar: Car? = null
    private val repository = FirebaseRepository()
    private var selectedCarId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setupInitialState(view)
        loadUserCars()
        setupClickListeners(view)

        return view
    }

    private fun setupInitialState(view: View) {
        // Empty state를 처음에 보여줌
        view.findViewById<LinearLayout>(R.id.carContentLayout)?.visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.textViewEmpty)?.apply {
            visibility = View.VISIBLE
        }
        view.findViewById<TextView>(R.id.tvCarAdd2)?.setOnClickListener {
            AddCarBottomSheet().show(childFragmentManager, "addCar")
        }
    }

    private fun loadUserCars() {
        val userId = Firebase.auth.currentUser?.uid
        userId?.let { uid ->
            repository.observeUserCars(uid,
                onSuccess = { cars ->
                    if (cars.isNotEmpty()) {
                        if (selectedCarId == null) {
                            currentCar = cars[0]
                            selectedCarId = cars[0].id
                        } else {
                            // 이전에 선택했던 차량 찾기
                            currentCar = cars.find { it.id == selectedCarId } ?: cars[0]
                        }
                        updateCarUI()
                        showContent(true)
                    } else {
                        showContent(false)
                    }
                },
                onFailure = { exception ->
                    if (Firebase.auth.currentUser != null) {
                        Toast.makeText(context,
                            "차량 정보를 불러오는데 실패했습니다: ${exception.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                    showContent(false)
                }
            )
        }
    }

    private fun setupClickListeners(view: View) {
        // 차량 선택기
        view.findViewById<LinearLayout>(R.id.carSelector)?.setOnClickListener {
            currentCar?.let {
                CarSelectorBottomSheet().show(childFragmentManager, CarSelectorBottomSheet.TAG)
            }
        }

        // 차량 추가 버튼
        view.findViewById<ImageView>(R.id.imageViewAdd)?.setOnClickListener {
            AddCarBottomSheet().show(childFragmentManager, "addCar")
        }

        // 제어 더보기
        view.findViewById<TextView>(R.id.textViewControl)?.setOnClickListener {
            currentCar?.let { car ->
                ControlBottomSheet.newInstance(car, this)
                    .show(parentFragmentManager, ControlBottomSheet.TAG)
            }
        }

        // 잠금 버튼
        setupLockButtons(view)

        // 시동 버튼
        setupEngineButton(view)

        // 사진 버튼
        view.findViewById<TextView>(R.id.tvCarDetail)?.setOnClickListener {
            currentCar?.let { car ->
                val intent = Intent(requireContext(), PhotoActivity::class.java)
                intent.putExtra("carId", car.id)
                startActivity(intent)
            }
        }
    }

    private fun setupLockButtons(view: View) {
        val lockButton = view.findViewById<CardView>(R.id.lockButton)
        val unlockButton = view.findViewById<CardView>(R.id.unlockButton)

        lockButton?.setOnClickListener {
            currentCar?.let { car ->
                if (!car.doorLock) {
                    car.doorLock = true
                    updateDoorLockState(lockButton, unlockButton, true)
                    repository.updateCar(car,
                        onSuccess = { },
                        onFailure = { exception ->
                            Toast.makeText(context,
                                "상태 업데이트 실패: ${exception.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        unlockButton?.setOnClickListener {
            currentCar?.let { car ->
                if (car.doorLock) {
                    car.doorLock = false
                    updateDoorLockState(lockButton, unlockButton, false)
                    repository.updateCar(car,
                        onSuccess = { },
                        onFailure = { exception ->
                            Toast.makeText(context,
                                "상태 업데이트 실패: ${exception.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    private fun setupEngineButton(view: View) {
        val engineButton = view.findViewById<CardView>(R.id.carButton)
        engineButton?.setOnClickListener {
            currentCar?.let { car ->
                car.engineOn = !car.engineOn
                updateEngineButtonState(engineButton, car.engineOn)
                repository.updateCar(car,
                    onSuccess = { /* 필요시 처리 */ },
                    onFailure = { exception ->
                        Toast.makeText(context,
                            "상태 업데이트 실패: ${exception.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun showContent(show: Boolean) {
        view?.apply {
            findViewById<LinearLayout>(R.id.carContentLayout)?.visibility =
                if (show) View.VISIBLE else View.GONE
            findViewById<LinearLayout>(R.id.textViewEmpty)?.visibility =
                if (show) View.GONE else View.VISIBLE
        }
    }

    private fun updateCarUI() {
        val car = currentCar ?: return

        view?.apply {
            // 차량 이름
            findViewById<TextView>(R.id.textViewCar)?.text = car.name

            // 차량 이미지
            context?.let { ctx ->
                Glide.with(ctx)
                    .load(car.image)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .into(findViewById<ImageView>(R.id.imageViewCar))
            }

            // 온도와 거리
            findViewById<TextView>(R.id.textViewRealTemp)?.text = "${car.realTemperature}°C"
            findViewById<TextView>(R.id.textViewDistance)?.text = "${car.distance}km"

            // 잠금 상태
            val lockButton = findViewById<CardView>(R.id.lockButton)
            val unlockButton = findViewById<CardView>(R.id.unlockButton)
            if (lockButton != null && unlockButton != null) {
                updateDoorLockState(lockButton, unlockButton, car.doorLock)
            }

            // 시동 상태
            findViewById<CardView>(R.id.carButton)?.let { engineButton ->
                updateEngineButtonState(engineButton, car.engineOn)
            }
        }
    }

    private fun updateDoorLockState(lockButton: CardView, unlockButton: CardView, isLocked: Boolean) {
        lockButton.isSelected = isLocked
        unlockButton.isSelected = !isLocked

        view?.findViewById<TextView>(R.id.textView7)?.apply {
            text = if (isLocked) "Locked" else "Unlocked"
            setTextColor(resources.getColor(if (isLocked) R.color.lock_green else R.color.unlock_red, null))
        }
    }

    private fun updateEngineButtonState(button: CardView, isOn: Boolean) {
        button.isSelected = isOn
        view?.findViewById<TextView>(R.id.textView5)?.apply {
            text = if (isOn) "ON" else "OFF"
            setTextColor(resources.getColor(if (isOn) R.color.engine_green else R.color.engine_red, null))
        }
    }

    override fun onCarStateChanged(car: Car) {
        currentCar = car
        updateCarUI()
    }

    override fun onCarSelected(car: Car) {
        currentCar = car
        selectedCarId = car.id
        updateCarUI()
    }

    override fun onCarAdded(car: Car) {
        currentCar = car
        selectedCarId = car.id
        updateCarUI()
        showContent(true)
    }
}
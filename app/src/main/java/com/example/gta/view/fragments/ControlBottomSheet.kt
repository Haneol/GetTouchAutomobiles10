package com.example.gta.view.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.gta.R
import com.example.gta.data.model.Car
import com.example.gta.data.repository.FirebaseRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ControlBottomSheet : BottomSheetDialogFragment() {
    private var car: Car? = null
    private var listener: CarControlListener? = null

    interface CarControlListener {
        fun onCarStateChanged(car: Car)
    }

    fun setListener(listener: CarControlListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        car = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(CAR_KEY, Car::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(CAR_KEY)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // BottomSheet가 닫힐 때 현재 car 상태를 전달
        car?.let { currentCar ->
            listener?.onCarStateChanged(currentCar)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_control, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lockButton = view.findViewById<CardView>(R.id.buttonLock)
        val unlockButton = view.findViewById<CardView>(R.id.buttonUnlock)
        val engineOnButton = view.findViewById<CardView>(R.id.buttonEngineOn)
        val engineOffButton = view.findViewById<CardView>(R.id.buttonEngineOff)
        val lightButton = view.findViewById<CardView>(R.id.buttonLight)

        car?.let { currentCar ->
            updateDoorLockState(lockButton, unlockButton, currentCar.doorLock)
            updateEngineState(engineOnButton, engineOffButton, currentCar.engineOn)
            updateHazardLightState(lightButton, currentCar.hazardLight)

            // 문 잠금/열림 버튼
            lockButton.setOnClickListener {
                if (!currentCar.doorLock) {
                    currentCar.doorLock = true
                    updateDoorLockState(lockButton, unlockButton, true)
                    listener?.onCarStateChanged(currentCar)
                }
            }

            unlockButton.setOnClickListener {
                if (currentCar.doorLock) {
                    currentCar.doorLock = false
                    updateDoorLockState(lockButton, unlockButton, false)
                    listener?.onCarStateChanged(currentCar)
                }
            }

            // 시동 버튼
            engineOnButton.setOnClickListener {
                if (!currentCar.engineOn) {
                    currentCar.engineOn = true
                    updateEngineState(engineOnButton, engineOffButton, true)
                    listener?.onCarStateChanged(currentCar)
                }
            }

            engineOffButton.setOnClickListener {
                if (currentCar.engineOn) {
                    currentCar.engineOn = false
                    updateEngineState(engineOnButton, engineOffButton, false)
                    listener?.onCarStateChanged(currentCar)
                }
            }

            // 비상등 토글 버튼
            lightButton.setOnClickListener {
                currentCar.hazardLight = !currentCar.hazardLight
                updateHazardLightState(lightButton, currentCar.hazardLight)
                listener?.onCarStateChanged(currentCar)
            }

            val seekBar = view.findViewById<SeekBar>(R.id.temperatureSeekBar)
            val tempText = view.findViewById<TextView>(R.id.textViewTemp)

            seekBar.max = 32
            seekBar.min = 16
            seekBar.progress = currentCar.temperature
            tempText.text = "${seekBar.progress}°C"

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tempText.text = "${progress}°C"
                    currentCar.temperature = progress
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // SeekBar 조작이 끝났을 때 Firebase 업데이트
                    car?.let { currentCar ->
                        FirebaseRepository().updateCar(currentCar,
                            onSuccess = { /* 필요시 처리 */ },
                            onFailure = { exception ->
                                Toast.makeText(context,
                                    "온도 업데이트 실패: ${exception.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            })
        }
    }

    private fun updateDoorLockState(lockButton: CardView, unlockButton: CardView, isLocked: Boolean) {
        lockButton.isSelected = isLocked
        unlockButton.isSelected = !isLocked
        car?.let { currentCar ->
            FirebaseRepository().updateCar(currentCar,
                onSuccess = { /* 필요시 처리 */ },
                onFailure = { exception ->
                    Toast.makeText(context,
                        "잠금 업데이트 실패: ${exception.message}",
                        Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateEngineState(onButton: CardView, offButton: CardView, isOn: Boolean) {
        onButton.isSelected = isOn
        offButton.isSelected = !isOn
        car?.let { currentCar ->
            FirebaseRepository().updateCar(currentCar,
                onSuccess = { /* 필요시 처리 */ },
                onFailure = { exception ->
                    Toast.makeText(context,
                        "시동 업데이트 실패: ${exception.message}",
                        Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateHazardLightState(lightButton: CardView, isOn: Boolean) {
        lightButton.isSelected = isOn
        car?.let { currentCar ->
            FirebaseRepository().updateCar(currentCar,
                onSuccess = { /* 필요시 처리 */ },
                onFailure = { exception ->
                    Toast.makeText(context,
                        "비상등 업데이트 실패: ${exception.message}",
                        Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    companion object {
        const val TAG = "ControlBottomSheet"
        private const val CAR_KEY = "car_key"

        fun newInstance(car: Car, listener: CarControlListener): ControlBottomSheet {
            return ControlBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(CAR_KEY, car)
                }
                setListener(listener)
            }
        }
    }
}
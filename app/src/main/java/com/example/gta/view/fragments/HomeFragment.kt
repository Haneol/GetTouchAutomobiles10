package com.example.gta.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.gta.R
import com.example.gta.data.model.Car

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val carList = mutableListOf<Car>().apply {
            add(Car("1", "쏘나타 디 엣지", "https://www.hyundai.com/contents/repn-car/side-45/main-sonata-the-edge-25my-45side.png"))
        }

        val textViewCar = view.findViewById<TextView>(R.id.textViewCar)
        textViewCar.text = carList[0].name

        val carSelector = view.findViewById<LinearLayout>(R.id.carSelector)
        carSelector.setOnClickListener {
            Toast.makeText(context, "car selector", Toast.LENGTH_SHORT).show()
        }

        val buttonAdd = view.findViewById<ImageView>(R.id.imageViewAdd)
        buttonAdd.setOnClickListener {
            Toast.makeText(context, "car add", Toast.LENGTH_SHORT).show()
        }

        // 차량 이미지
        val imageViewCar = view.findViewById<ImageView>(R.id.imageViewCar)
        context?.let {
            Glide.with(it)
                .load(carList[0].image)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(imageViewCar)
        }

        return view
    }
}
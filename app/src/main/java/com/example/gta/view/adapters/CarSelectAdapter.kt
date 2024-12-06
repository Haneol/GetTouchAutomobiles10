package com.example.gta.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gta.R
import com.example.gta.data.model.Car

class CarSelectAdapter(
    private var cars: List<Car>,
    private val onCarSelected: (Car) -> Unit
) : RecyclerView.Adapter<CarSelectAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewCar: ImageView = view.findViewById(R.id.imageViewCar)
        val textViewCarName: TextView = view.findViewById(R.id.textViewCarName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_select, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.textViewCarName.text = car.name

        Glide.with(holder.imageViewCar.context)
            .load(car.image)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .into(holder.imageViewCar)

        holder.itemView.setOnClickListener {
            onCarSelected(car)
        }
    }

    override fun getItemCount() = cars.size

    fun updateCars(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
package com.example.gta.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Car(
    var id: String = "",
    var userId: String = "",
    val name: String = "",
    val image: String = "",

    var doorLock: Boolean = true,
    var engineOn: Boolean = false,
    var hazardLight: Boolean = false,
    var temperature: Int = 24,

    var realTemperature: Double = 24.7,
    var distance: Int = 476
) : Parcelable

package com.example.gta.data.repository

import android.net.Uri
import com.example.gta.data.model.Car
import com.example.gta.data.model.PhotoItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseRepository {
    private val db = Firebase.firestore

    fun observeUserCars(
        userId: String,
        onSuccess: (List<Car>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .collection("cars")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onFailure(e)
                    return@addSnapshotListener
                }

                val cars = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Car::class.java)
                } ?: emptyList()

                onSuccess(cars)
            }
    }

    fun addCar(car: Car, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        val carId = db.collection("users")
            .document(userId)
            .collection("cars")
            .document().id

        car.id = carId
        car.userId = userId

        db.collection("users")
            .document(userId)
            .collection("cars")
            .document(carId)
            .set(car)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateCar(car: Car, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("cars")
            .document(car.id)
            .set(car)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun addPhoto(
        carId: String,
        uri: Uri,
        onSuccess: (PhotoItem) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val photoId = db.collection("users").document(userId)
            .collection("cars").document(carId)
            .collection("photos").document().id

        // Storage에 이미지 업로드
        val imageRef = Firebase.storage.reference.child("images/$userId/$carId/$photoId.jpg")

        imageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                // Firestore에 사진 정보 저장
                val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date())

                val photoItem = PhotoItem(
                    id = photoId,
                    date = currentDate,
                    imageUri = downloadUri.toString()
                )

                db.collection("users").document(userId)
                    .collection("cars").document(carId)
                    .collection("photos").document(photoId)
                    .set(photoItem)
                    .addOnSuccessListener {
                        onSuccess(photoItem)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getPhotos(
        carId: String,
        onSuccess: (List<PhotoItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("cars").document(carId)
            .collection("photos")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val photos = result.documents.mapNotNull { doc ->
                    doc.toObject(PhotoItem::class.java)
                }
                onSuccess(photos)
            }
            .addOnFailureListener(onFailure)
    }
}
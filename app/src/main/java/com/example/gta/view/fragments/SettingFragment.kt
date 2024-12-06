package com.example.gta.view.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.gta.R
import com.example.gta.view.activities.IntroActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SettingFragment : Fragment() {
    val db: FirebaseFirestore = Firebase.firestore
    private var textViewNickname: TextView? = null
    private var textViewEmail: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        textViewNickname = view.findViewById(R.id.textViewNickname)
        textViewEmail = view.findViewById(R.id.textViewEmail)

        loadUserData()
        setupLogout(view)

        return view
    }
    private fun initializeViews(view: View) {
        textViewNickname = view.findViewById(R.id.textViewNickname)
        textViewEmail = view.findViewById(R.id.textViewEmail)
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    document?.let {
                        textViewEmail?.text = it.getString("email")
                        textViewNickname?.text = it.getString("nickname")
                    }
                }
        }
    }

    private fun setupLogout(view: View) {
        view.findViewById<TextView>(R.id.textViewLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), IntroActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
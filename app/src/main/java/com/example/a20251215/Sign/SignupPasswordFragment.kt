package com.example.a20251215.Sign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.a20251215.R

class SignupPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val finishbtn = view.findViewById<Button>(R.id.btn_signup_finish)
        finishbtn.setOnClickListener {
            Toast.makeText(context, "회원가입 완료", Toast.LENGTH_SHORT).show()

            activity?.finish()
        }
    }
}
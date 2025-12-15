package com.example.a20251215.Setting

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.a20251215.LoginActivity
import com.example.a20251215.R
import com.example.a20251215.WithdrawActivity

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("UserInfo", android.content.Context.MODE_PRIVATE)

        val memberId = sharedPref.getInt("member_id", -1)
        val username = sharedPref.getString("username", "이름 없음")
        val loginId = sharedPref.getString("login_id", "아이디 없음")
        val nickname = sharedPref.getString("nickname", "닉네임 없음")
        val email = sharedPref.getString("email", "이메일 없음")

        // 로그 확인
        Log.d("SettingsFragment", "member_id: $memberId")
        Log.d("SettingsFragment", "username: $username")
        Log.d("SettingsFragment", "login_id: $loginId")
        Log.d("SettingsFragment", "nickname: $nickname")
        Log.d("SettingsFragment", "email: $email")

        // 텍스트뷰에 유저 정보 반영
        view.findViewById<TextView>(R.id.tvAccountTitle).text = username
        view.findViewById<TextView>(R.id.tvAccountIdValue).text = loginId
        view.findViewById<TextView>(R.id.tvAccountSub).text = nickname

        // 닉네임 수정
        view.findViewById<TextView>(R.id.itemNickname).setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.putExtra("member_id", memberId)
            startActivity(intent)
        }

        // 비밀번호 재설정
        view.findViewById<TextView>(R.id.itemPassword).setOnClickListener {
            val intent = Intent(requireContext(), PasswordResetActivity::class.java)
            intent.putExtra("member_id", memberId)
            startActivity(intent)
        }

        // 로그아웃 커스텀 다이얼로그
        view.findViewById<TextView>(R.id.itemLogout).setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_logout_dialog, null)

            val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .create()

            val btnConfirm = dialogView.findViewById<Button>(R.id.btnPositive)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnNegative)

            btnConfirm.setOnClickListener {
                sharedPref.edit().clear().apply()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        // 회원탈퇴
        view.findViewById<TextView>(R.id.itemWithdraw).setOnClickListener {
            val intent = Intent(requireContext(), WithdrawActivity::class.java)
            intent.putExtra("member_id", memberId)
            startActivity(intent)
        }
    }
}

package com.example.a20251215

import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {

    // 이메일 형식 체크
    val EMAIL_PATTERN = Patterns.EMAIL_ADDRESS

    // 아이디 형식 : 영문 + 숫자 (4자 이상)
    val ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,}$")

    // 비밀번호 형식 : 영문 + 숫자 + 특수문자 포함, 8~20자
    val PW_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,20}$")
}
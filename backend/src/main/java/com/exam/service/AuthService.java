package com.exam.service;

import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.ResetPasswordDTO;
import com.exam.dto.SendCodeDTO;
import com.exam.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void logout(Long userId);

    void register(RegisterDTO dto);

    void sendCode(SendCodeDTO dto);

    void resetPassword(ResetPasswordDTO dto);
}

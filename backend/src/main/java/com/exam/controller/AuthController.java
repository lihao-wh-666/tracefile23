package com.exam.controller;

import com.exam.common.Result;
import com.exam.config.RsaConfig;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.ResetPasswordDTO;
import com.exam.dto.SendCodeDTO;
import com.exam.service.AuthService;
import com.exam.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RsaConfig rsaConfig;

    @GetMapping("/public-key")
    public Result<Map<String, String>> getPublicKey() {
        Map<String, String> data = new HashMap<>();
        data.put("publicKey", rsaConfig.getPublicKeyBase64());
        return Result.ok(data);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        authService.register(dto);
        return Result.ok();
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody @Valid SendCodeDTO dto) {
        authService.sendCode(dto);
        return Result.ok();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        authService.resetPassword(dto);
        return Result.ok();
    }
}

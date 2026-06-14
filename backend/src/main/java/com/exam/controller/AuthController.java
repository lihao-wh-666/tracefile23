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
        String rawKey = rsaConfig.getPublicKeyBase64();
        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN PUBLIC KEY-----\n");
        int lineLength = 64;
        for (int i = 0; i < rawKey.length(); i += lineLength) {
            int end = Math.min(i + lineLength, rawKey.length());
            pemBuilder.append(rawKey, i, end).append("\n");
        }
        pemBuilder.append("-----END PUBLIC KEY-----");
        data.put("publicKey", pemBuilder.toString());
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

    @PostMapping("/logout")
    public Result<Void> logout() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            try {
                Long userId = Long.parseLong(authentication.getName());
                authService.logout(userId);
            } catch (NumberFormatException ignored) {
            }
        }
        return Result.ok();
    }
}

package com.exam.controller;

import com.exam.common.Result;
import com.exam.config.RsaConfig;
import com.exam.dto.LoginDTO;
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
}

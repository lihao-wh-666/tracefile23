package com.exam.config;

import cn.hutool.crypto.asymmetric.RSA;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Base64;

@Configuration
public class RsaConfig {

    private RSA rsaInstance;
    private String publicKeyBase64;

    @PostConstruct
    public void init() {
        this.rsaInstance = new RSA();
        this.publicKeyBase64 = Base64.getEncoder().encodeToString(rsaInstance.getPublicKey().getEncoded());
    }

    @Bean
    public RSA rsa() {
        return rsaInstance;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }
}

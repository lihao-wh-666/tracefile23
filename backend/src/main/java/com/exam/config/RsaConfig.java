package com.exam.config;

import cn.hutool.crypto.asymmetric.RSA;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

@Configuration
public class RsaConfig {

    private String publicKeyBase64;
    private String privateKeyBase64;

    @PostConstruct
    public void init() {
        RSA rsa = new RSA();
        PublicKey publicKey = rsa.getPublicKey();
        PrivateKey privateKey = rsa.getPrivateKey();
        this.publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        this.privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    @Bean
    public RSA rsa() {
        return new RSA(privateKeyBase64, publicKeyBase64);
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }
}

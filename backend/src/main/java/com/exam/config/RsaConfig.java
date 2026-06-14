package com.exam.config;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Configuration
public class RsaConfig {

    @Value("${rsa.private-key:}")
    private String configuredPrivateKeyBase64;

    @Value("${rsa.private-key-password:}")
    private String privateKeyPassword;

    private RSA rsaInstance;
    private String publicKeyBase64;

    @PostConstruct
    public void init() throws Exception {
        if (StringUtils.hasText(configuredPrivateKeyBase64)) {
            String trimmedKey = configuredPrivateKeyBase64.trim();
            byte[] privateKeyBytes = Base64.getDecoder().decode(trimmedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

            PublicKey publicKey = derivePublicKeyFromPrivate(privateKey, keyFactory);

            String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            this.rsaInstance = new RSA(privateKeyStr, publicKeyStr);
            this.publicKeyBase64 = publicKeyStr;

            String testPlain = "admin123_test_verify";
            String encrypted = this.rsaInstance.encryptBase64(testPlain, KeyType.PublicKey);
            String decrypted = this.rsaInstance.decryptStr(encrypted, KeyType.PrivateKey);
            if (!testPlain.equals(decrypted)) {
                throw new RuntimeException("RSA自测失败：加密解密不匹配, expected=" + testPlain + ", actual=" + decrypted);
            }
            System.out.println("[RsaConfig] RSA初始化成功，使用配置的私钥推导公钥。自测通过 (加密解密匹配).");
            System.out.println("[RsaConfig] PublicKey (first 60): " + publicKeyStr.substring(0, Math.min(60, publicKeyStr.length())));
        } else {
            this.rsaInstance = new RSA();
            this.publicKeyBase64 = Base64.getEncoder().encodeToString(rsaInstance.getPublicKey().getEncoded());
            System.out.println("[RsaConfig] RSA初始化成功，使用随机生成的密钥对.");
        }
    }

    private PublicKey derivePublicKeyFromPrivate(PrivateKey privateKey, KeyFactory keyFactory) throws Exception {
        if (privateKey instanceof RSAPrivateCrtKey) {
            RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    rsaPrivateCrtKey.getModulus(),
                    rsaPrivateCrtKey.getPublicExponent()
            );
            return keyFactory.generatePublic(publicKeySpec);
        }
        throw new RuntimeException("无法从提供的私钥中推导公钥，请确保私钥是PKCS#8格式的完整RSA私钥");
    }

    @Bean
    public RSA rsa() {
        return rsaInstance;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }
}

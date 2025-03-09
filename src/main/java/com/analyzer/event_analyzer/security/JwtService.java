package com.analyzer.event_analyzer.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.expiration-ms}")
    private long expirationMs;

    // Versione semplificata per generare un token
    public String generateToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        long expirationTime = now + expirationMs;

        String data = userDetails.getUsername() + ":" + expirationTime;
        String signature = createSignature(data);

        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8))
                + "." + signature;
    }

    // Crea una firma HMAC per il token
    private String createSignature(String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    // Estrae il nome utente dal token
    public String extractUsername(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid token format");
        }

        String decodedData = new String(
                Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        return decodedData.split(":")[0];
    }

    // Verifica se il token è scaduto
    private boolean isTokenExpired(String token) {
        long expirationTime = extractExpirationTime(token);
        return expirationTime < System.currentTimeMillis();
    }

    private long extractExpirationTime(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid token format");
        }

        String decodedData = new String(
                Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        return Long.parseLong(decodedData.split(":")[1]);
    }

    // Valida il token
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            String[] parts = token.split("\\.");

            String data = new String(
                    Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String providedSignature = parts[1];
            String expectedSignature = createSignature(data);

            boolean signatureValid = expectedSignature.equals(providedSignature);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && signatureValid;
        } catch (Exception e) {
            return false;
        }
    }
}
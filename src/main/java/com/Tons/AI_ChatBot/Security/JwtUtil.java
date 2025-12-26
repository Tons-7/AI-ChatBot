package com.Tons.AI_ChatBot.Security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class JwtUtil {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long EXPIRATION = 1000 * 60 * 60;

    public JwtUtil() throws Exception {
        byte[] privateBytes = Files.readAllBytes(Paths.get("src/main/resources/keys/jwt-private.pem"));
        byte[] publicBytes = Files.readAllBytes(Paths.get("src/main/resources/keys/jwt-public.pem"));

        this.privateKey = readPrivateKey(new String(privateBytes));
        this.publicKey = readPublicKey(new String(publicBytes));
    }

    private PrivateKey readPrivateKey(String key) throws Exception {
        String clean = key.replaceAll("-----\\w+ PRIVATE KEY-----", "").replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey readPublicKey(String key) throws Exception {
        String clean = key.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .issuer("AI-ChatBot")
                .subject(username)
                .audience().add("AI-ChatBot-Client").and()
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String validateAndExtractUsername(String token) {
        Jws<Claims> jwt = Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer("AI-ChatBot")
                .requireAudience("AI-ChatBot-Client")
                .build()
                .parseSignedClaims(token);

        return jwt.getPayload().getSubject();
    }
}

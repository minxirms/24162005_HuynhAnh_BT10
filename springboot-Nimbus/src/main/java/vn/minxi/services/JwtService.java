package vn.minxi.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // 1. Tạo JWT Token sử dụng Nimbus
    public String generateToken(UserDetails userDetails) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer("springboot-JWT")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + jwtExpiration))
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);

        try {
            JWSSigner signer = new MACSigner(secretKey.getBytes());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Lỗi khi tạo JWT token với Nimbus", e);
        }
    }

    // 2. Lấy Username (Subject) từ Token
    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Token không hợp lệ", e);
        }
    }

    // 3. Kiểm tra tính hợp lệ của Token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

            // Xác minh chữ ký
            boolean isSignatureValid = signedJWT.verify(verifier);

            // Kiểm tra thời gian hết hạn
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isNotExpired = expiration != null && expiration.after(new Date());

            // Kiểm tra username
            String username = signedJWT.getJWTClaimsSet().getSubject();

            return isSignatureValid && isNotExpired && username.equals(userDetails.getUsername());
        } catch (JOSEException | ParseException e) {
            return false;
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }
}
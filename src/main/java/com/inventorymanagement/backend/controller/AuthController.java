package com.inventorymanagement.backend.controller;

import com.inventorymanagement.backend.dto.auth.LoginRequest;
import com.inventorymanagement.backend.dto.auth.RefreshTokenRequest;
import com.inventorymanagement.backend.dto.auth.Response;
import com.inventorymanagement.backend.dto.auth.TokenRefreshResponse;
import com.inventorymanagement.backend.service.jwt.JwtService;
import com.inventorymanagement.backend.service.jwt.RefreshTokenService;
import com.inventorymanagement.backend.service.jwt.TokenBlackListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlackListService blackListService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        String accessToken = jwtService.generateToken(loginRequest.getUsername(), role);
        String refreshToken = refreshTokenService.createRefreshToken(loginRequest.getUsername(), role);
        return new ResponseEntity<>(new Response(accessToken, refreshToken), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long remainingValidity = jwtService.getRemainingValidityMs(token);
            if (remainingValidity > 0) {
                blackListService.blackListToken(token, remainingValidity);
            }
        }

        if (refreshTokenRequest != null && refreshTokenRequest.getRefreshToken() != null) {
            refreshTokenService.revokeRefreshToken(refreshTokenRequest.getRefreshToken());
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshTokenService.TokenInfo info = refreshTokenService.rotateRefreshToken(refreshTokenRequest.getRefreshToken());
        // Issue new access token
        String newAccessToken = jwtService.generateToken(info.username(), info.role());
        // Issue new refresh token (rotation)
        String newRefreshToken = refreshTokenService.createRefreshToken(info.username(), info.role());
        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long remainingValidity = jwtService.getRemainingValidityMs(token);
            if (remainingValidity > 0) {
                blackListService.blackListToken(token, remainingValidity);
            }
        }
        return ResponseEntity.noContent().build();
    }
}

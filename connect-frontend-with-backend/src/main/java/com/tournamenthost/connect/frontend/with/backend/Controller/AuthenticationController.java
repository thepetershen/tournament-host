package com.tournamenthost.connect.frontend.with.backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tournamenthost.connect.frontend.with.backend.DTO.ForgotPasswordRequest;
import com.tournamenthost.connect.frontend.with.backend.DTO.LoginResponse;
import com.tournamenthost.connect.frontend.with.backend.DTO.LoginUserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.RegisterUserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.ResetPasswordRequest;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Security.JwtService;
import com.tournamenthost.connect.frontend.with.backend.Service.AuthenticationService;

import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);

            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDTO loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(jwtToken);
            loginResponse.setExpiresIn(jwtService.getExpirationTime());

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authenitcation Failed"); // or a custom error response
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authenticationService.initiatePasswordReset(request.getEmail());
            // Always return success message (don't reveal if email exists)
            return ResponseEntity.ok(Map.of("message", "If your email exists in our system, you will receive a password reset link"));
        } catch (Exception e) {
            // Return same message even on error (security best practice)
            return ResponseEntity.ok(Map.of("message", "If your email exists in our system, you will receive a password reset link"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authenticationService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<?> validateResetToken(@PathVariable String token) {
        boolean isValid = authenticationService.validateResetToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
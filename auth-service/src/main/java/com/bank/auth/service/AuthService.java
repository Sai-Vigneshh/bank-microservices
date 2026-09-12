package com.bank.auth.service;

import com.bank.auth.dto.AuthResponse;
import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.entity.Role;
import com.bank.auth.entity.User;
import com.bank.auth.exception.InvalidCredentialsException;
import com.bank.auth.exception.UserAlreadyExistsException;
import com.bank.auth.repository.UserRepository;
import com.bank.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_CUSTOMER)
                .build();
        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }
    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));;
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }
            String token = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
        }

}
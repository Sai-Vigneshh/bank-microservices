package com.bank.auth.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse
{
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long  userId;
    private String email;
    private String role;
}
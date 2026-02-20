package com.jayanta.usermanagement.dto;

import com.jayanta.usermanagement.model.AppUser;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private AppUserDto user;
}

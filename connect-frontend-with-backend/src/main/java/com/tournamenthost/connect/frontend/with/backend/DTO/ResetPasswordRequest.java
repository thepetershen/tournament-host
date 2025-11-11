package com.tournamenthost.connect.frontend.with.backend.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
}

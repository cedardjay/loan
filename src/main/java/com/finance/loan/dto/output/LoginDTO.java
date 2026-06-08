package com.finance.loan.dto.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginDTO {
    private String role;
    private String token;
    private String expirationTime;
}

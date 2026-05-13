package com.inventorymanagement.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class Response {

    private String accessToken;
    private String refreshToken;

}

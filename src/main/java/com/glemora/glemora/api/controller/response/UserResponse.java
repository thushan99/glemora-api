package com.glemora.glemora.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {

    private String username;
    private String name;
    private String email;
    private String profilePic;
}

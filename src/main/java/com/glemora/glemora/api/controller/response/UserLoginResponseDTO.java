package com.glemora.glemora.api.controller.response;


import com.glemora.glemora.api.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserLoginResponseDTO {

    private String token;
    private List<Role> userType;
}

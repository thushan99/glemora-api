package com.glemora.glemora.api.controller.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserAuthRequestDTO {

    private String name;
    private String username;
    private String password;
    private String email;
    private List<Long> role;
}

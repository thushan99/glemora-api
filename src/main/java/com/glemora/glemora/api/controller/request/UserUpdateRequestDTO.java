package com.glemora.glemora.api.controller.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserUpdateRequestDTO {

    private String name;
    private String username;
    private MultipartFile profilePic;
}

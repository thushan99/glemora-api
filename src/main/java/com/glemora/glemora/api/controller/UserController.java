package com.glemora.glemora.api.controller;

import com.glemora.glemora.api.controller.request.UserAuthRequestDTO;
import com.glemora.glemora.api.controller.request.UserLoginRequest;
import com.glemora.glemora.api.controller.request.UserUpdateRequestDTO;
import com.glemora.glemora.api.controller.response.MessageResponse;
import com.glemora.glemora.api.controller.response.UserLoginResponseDTO;
import com.glemora.glemora.api.controller.response.UserResponse;
import com.glemora.glemora.api.exception.UserAlreadyRegisteredException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.model.User;
import com.glemora.glemora.api.service.UserService;
import jakarta.annotation.security.RolesAllowed;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private UserService userService;

    @PostMapping(value = "/sign-up",headers = "X-Api-Version=v1")
    public ResponseEntity<UserLoginResponseDTO> authenticate(@RequestBody UserAuthRequestDTO userAuthRequestDTO) throws UserAlreadyRegisteredException {

        UserLoginResponseDTO userLoginResponseDTO = userService.create(userAuthRequestDTO);
        MessageResponse messageResponse = MessageResponse.builder()
                .message("User Registered successfully!")
                .build();

        return new ResponseEntity<>(userLoginResponseDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/sign-in",headers = "X-Api-Version=v1")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginRequest userLoginRequest) throws  UserNotFoundException {

        UserLoginResponseDTO userLoginResponseDTO = userService.login(userLoginRequest);
        return new ResponseEntity<>(userLoginResponseDTO, HttpStatus.OK);
    }

    @RolesAllowed("ADMIN")
    @GetMapping(value = "/employees", headers = "X-Api-Version=v1")
    public List<User> getEmployees() {

        return userService.getAll();
    }

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping(value = "/me", headers = "X-Api-Version=v1")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getById(userDetails.getUsername());

        return ResponseEntity.ok(new UserResponse(
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getProfilePic())
        );
    }

    @RolesAllowed({"ADMIN", "USER"})
    @PutMapping(value = "/update-me", headers = "X-Api-Version=v1")
    public ResponseEntity<String> updateMe(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute UserUpdateRequestDTO userUpdateRequestDTO) throws IOException {

        userService.update(userDetails.getUsername(), userUpdateRequestDTO);
        return ResponseEntity.ok("Updated Successfully");
    }

    @RolesAllowed("ADMIN")
    @GetMapping("/admin")
    public String sayHiAdmin() {

        return "Hi Admin";
    }

    @RolesAllowed("USER")
    @GetMapping("/user")
    public String sayHiUser() {

        return "Hi User";
    }

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping("/user-admin")
    public String sayHiUserAndAdmin() {

        return "Hi User And Admin";
    }
}

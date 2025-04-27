package com.glemora.glemora.api.service;


import com.glemora.glemora.api.controller.request.*;
import com.glemora.glemora.api.controller.response.UserLoginResponseDTO;
import com.glemora.glemora.api.exception.UserAlreadyRegisteredException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.model.User;

import java.io.IOException;
import java.util.List;

public interface UserService {

    UserLoginResponseDTO create(UserAuthRequestDTO userAuthRequestDTO) throws UserAlreadyRegisteredException;

    UserLoginResponseDTO login(UserLoginRequest userLoginRequest) throws UserNotFoundException;

    List<User> getAll();

    User getById(String username);

    User update(String username, UserUpdateRequestDTO userUpdateRequestDTO) throws IOException;

    void updateUser(Long userId, AdminUserUpdateRequestDTO adminUserUpdateRequestDTO) throws IOException, UserNotFoundException;

    void deleteUser(Long userId) throws UserNotFoundException;

    void updateUserRole(Long userId, UserRoleUpdateRequest roleUpdateRequest) throws UserNotFoundException;

}

package com.glemora.glemora.api.service.Impl;

import com.cloudinary.Cloudinary;
import com.glemora.glemora.api.controller.request.UserAuthRequestDTO;
import com.glemora.glemora.api.controller.request.UserLoginRequest;
import com.glemora.glemora.api.controller.request.UserUpdateRequestDTO;
import com.glemora.glemora.api.controller.response.UserLoginResponseDTO;
import com.glemora.glemora.api.exception.UserAlreadyRegisteredException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.model.Role;
import com.glemora.glemora.api.model.User;
import com.glemora.glemora.api.repository.RoleRepository;
import com.glemora.glemora.api.repository.UserRepository;
import com.glemora.glemora.api.security.ApplicationConfig;
import com.glemora.glemora.api.security.JwtService;
import com.glemora.glemora.api.service.UserService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationConfig applicationConfig;
    private final RoleRepository roleRepository;
    private JwtService jwtService;
    private final Cloudinary cloudinary;

    @Override
    public UserLoginResponseDTO create(UserAuthRequestDTO userAuthRequestDTO) throws UserAlreadyRegisteredException {

        User existingUser = userRepository.findByUsername(userAuthRequestDTO.getUsername());
        if (existingUser != null) {
            throw new UserAlreadyRegisteredException("User Already registered in " + userAuthRequestDTO.getUsername());
        }

        User existingEmailUser = userRepository.findByEmail(userAuthRequestDTO.getEmail());
        if (existingEmailUser != null) {
            throw new UserAlreadyRegisteredException("User Already registered in " + userAuthRequestDTO.getEmail());
        }

        User user = new User();
//        Long empIdCount = userRepository.count() + 1;
////        String empId = String.format("E%03d", empIdCount);
////        user.setEmployeeId(empId);
        user.setName(userAuthRequestDTO.getName());
        user.setUsername(userAuthRequestDTO.getUsername());
        user.setPassword(applicationConfig.passwordEncoder().encode(userAuthRequestDTO.getPassword()));

        List<Role> roles = roleRepository.findAllById(userAuthRequestDTO.getRole());

        user.setRoles(roles);
        user.setEmail(userAuthRequestDTO.getEmail());

        userRepository.save(user);

        List<String> roles1 = List.of(userAuthRequestDTO.getRole().toString());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("username", user.getUsername());
        extraClaims.put("password", user.getPassword());
        extraClaims.put("roles", roles1);
        extraClaims.put("name", user.getName());

        String token = jwtService.generateToken(user, extraClaims);
        return UserLoginResponseDTO.builder()
                .token(token)
                .userType(user.getRoles())
                .build();
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequest userLoginRequest) throws UserNotFoundException {

        User user = userRepository.findByUsername(userLoginRequest.getUsername());

        if (user != null) {
            if (!applicationConfig.passwordEncoder().matches(userLoginRequest.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid username or password");
            }

            List<String> roles = userRepository.findRolesByUsername(user.getUsername())
                    .stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("username", user.getUsername());
            extraClaims.put("password", user.getPassword());
            extraClaims.put("roles", roles);
            extraClaims.put("name", user.getName());

            String token = jwtService.generateToken(user, extraClaims);
            return UserLoginResponseDTO.builder()
                    .token(token)
                    .userType(user.getRoles())
                    .build();
        }

        else {
            throw new UserNotFoundException("User not found with username " + userLoginRequest.getUsername());
        }
    }

    @Override
    public List<User> getAll() {

        return userRepository.findAll();
    }

    @Override
    public User getById(String username) {

        return userRepository.findByUsername(username);
    }

    @Override
    public User update(String username, UserUpdateRequestDTO userUpdateRequestDTO) throws IOException {

        User user = userRepository.findByUsername(username);
        User updateUser = new User();

        if (userUpdateRequestDTO.getProfilePic().isEmpty())
            updateUser.setProfilePic("N/A");

        else {
            String profilePic = cloudinary.uploader()
                    .upload(userUpdateRequestDTO.getProfilePic().getBytes(),
                            Map.of("public_id", UUID.randomUUID().toString()))
                    .get("url")
                    .toString();

            updateUser.setProfilePic(profilePic);
        }

        updateUser.setName(userUpdateRequestDTO.getName());
        updateUser.setUsername(userUpdateRequestDTO.getUsername());
        updateUser.setRoles(user.getRoles());
        updateUser.setEmail(user.getEmail());
        updateUser.setPassword(user.getPassword());
        updateUser.setId(user.getId());
        userRepository.save(updateUser);

        return updateUser;
    }
}

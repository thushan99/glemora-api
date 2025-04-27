package com.glemora.glemora.api.service.Impl;

import com.cloudinary.Cloudinary;
import com.glemora.glemora.api.controller.request.*;
import com.glemora.glemora.api.controller.response.UserLoginResponseDTO;
import com.glemora.glemora.api.exception.UserAlreadyRegisteredException;
import com.glemora.glemora.api.exception.UserNotFoundException;
import com.glemora.glemora.api.model.Order;
import com.glemora.glemora.api.model.Role;
import com.glemora.glemora.api.model.User;
import com.glemora.glemora.api.repository.*;
import com.glemora.glemora.api.security.ApplicationConfig;
import com.glemora.glemora.api.security.JwtService;
import com.glemora.glemora.api.service.UserService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationConfig applicationConfig;
    private final RoleRepository roleRepository;
    private JwtService jwtService;
    private final Cloudinary cloudinary;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserAddressRepository userAddressRepository;

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
        return UserLoginResponseDTO.builder().token(token).userType(user.getRoles()).build();
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequest userLoginRequest) throws UserNotFoundException {

        User user = userRepository.findByUsername(userLoginRequest.getUsername());

        if (user != null) {
            if (!applicationConfig.passwordEncoder().matches(userLoginRequest.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid username or password");
            }

            List<String> roles = userRepository.findRolesByUsername(user.getUsername()).stream().map(String::toUpperCase).collect(Collectors.toList());

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("username", user.getUsername());
            extraClaims.put("password", user.getPassword());
            extraClaims.put("roles", roles);
            extraClaims.put("name", user.getName());

            String token = jwtService.generateToken(user, extraClaims);
            return UserLoginResponseDTO.builder().token(token).userType(user.getRoles()).build();
        } else {
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

        if (userUpdateRequestDTO.getProfilePic().isEmpty()) updateUser.setProfilePic("N/A");

        else {
            String profilePic = cloudinary.uploader().upload(userUpdateRequestDTO.getProfilePic().getBytes(), Map.of("public_id", UUID.randomUUID().toString())).get("url").toString();

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

    @Override
    public void updateUser(Long userId, AdminUserUpdateRequestDTO adminUserUpdateRequestDTO) throws IOException, UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update user details
        if (adminUserUpdateRequestDTO.getName() != null) {
            user.setName(adminUserUpdateRequestDTO.getName());
        }
        if (adminUserUpdateRequestDTO.getUsername() != null) {
            user.setUsername(adminUserUpdateRequestDTO.getUsername());
        }
        if (adminUserUpdateRequestDTO.getEmail() != null) {
            user.setEmail(adminUserUpdateRequestDTO.getEmail());
        }


        MultipartFile profilePic = adminUserUpdateRequestDTO.getProfilePic();
        if (profilePic != null && !profilePic.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(profilePic.getBytes(), Map.of("resource_type", "auto"));
            user.setProfilePic(uploadResult.get("secure_url").toString());
        } else if (adminUserUpdateRequestDTO.getProfilePic() == null) {

            user.setProfilePic(null);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) throws UserNotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }

        List<Order> orders = orderRepository.findByUserId(userId);

        for (Order order : orders) {
            orderItemRepository.deleteByOrderId(order.getId()); // First delete child records
        }

        orderRepository.deleteByUserId(userId); // Then delete parent records
        cartRepository.deleteByUserId(userId);
        userAddressRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }


    @Override
    public void updateUserRole(Long userId, UserRoleUpdateRequest roleUpdateRequest) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Role> newRoles = new ArrayList<>();
        for (String roleName : roleUpdateRequest.getRoleNames()) {
            Role role = roleRepository.findByName(roleName);
            if (role == null) {
                throw new RuntimeException("Role not found: " + roleName);
            }
            newRoles.add(role);
        }
        user.setRoles(newRoles);
        userRepository.save(user);
    }
}

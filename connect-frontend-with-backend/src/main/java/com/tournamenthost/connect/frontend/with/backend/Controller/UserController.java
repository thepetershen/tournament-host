package com.tournamenthost.connect.frontend.with.backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.tournamenthost.connect.frontend.with.backend.DTO.RegisterUserDTO;
import com.tournamenthost.connect.frontend.with.backend.DTO.UserDTO;
import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(currentUser.getId());
        userDTO.setName(currentUser.getName());
        userDTO.setUsername(currentUser.getUsername());
        // Add other fields as needed

        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateAuthenticatedUser(@RequestBody RegisterUserDTO updatedUserDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        User updatedUser = userService.setUser(currentUser, updatedUserDTO.getUsername(), updatedUserDTO.getName(), updatedUserDTO.getPassword());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(updatedUser.getId());
        userDTO.setName(updatedUser.getName());
        userDTO.setUsername(updatedUser.getUsername());
        // Add other fields as needed

        return ResponseEntity.ok(userDTO);
    }
}
package com.tournamenthost.connect.frontend.with.backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.tournamenthost.connect.frontend.with.backend.DTO.UserCreationRequest;
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
        userDTO.setEmail(currentUser.getEmail());
        userDTO.setUsername(currentUser.getUsername());
        // Add other fields as needed

        return ResponseEntity.ok(userDTO);
    }

}
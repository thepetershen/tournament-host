package com.tournamenthost.connect.frontend.with.backend.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepo;

    public User setUser(User user, String username, String name, String password) {
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepo.existsByUsername(username)) {
                throw new IllegalArgumentException("User with username " + username + " already exists");
            }
            user.setUsername(username);
        }

        if (name != null) {
            user.setName(name);
        }

        if (password != null && !password.isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(password));
        }

        return userRepo.save(user);
    }

}   

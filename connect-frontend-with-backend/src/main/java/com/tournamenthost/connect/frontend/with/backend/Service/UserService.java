package com.tournamenthost.connect.frontend.with.backend.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tournamenthost.connect.frontend.with.backend.Model.User;
import com.tournamenthost.connect.frontend.with.backend.Repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepo;

    public User createUser(String username, String email, String password) {
        if(userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " +email + " already exists");
        }
        User user = new User(email, username, password);
        return userRepo.save(user);
    }

    public User getUser(Long id) {
        User answer = userRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " not found"));
        
        return answer;
    }

    public User setUser(Long id, String username, String email, String password) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " not found"));

        if(userRepo.existsByEmail(email) && email != user.getEmail()) {
            throw new IllegalArgumentException("User with email " +email + " already exists");
        }
        user.setEmail(email);
        user.setPassword(password);
        user.setUsername(username);
        return user;
    }

}   

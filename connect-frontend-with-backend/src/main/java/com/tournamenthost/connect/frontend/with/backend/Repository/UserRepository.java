package com.tournamenthost.connect.frontend.with.backend.Repository;


import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.User;


public interface UserRepository extends CrudRepository<User, Long>{
    
    boolean existsByUsername(String email);

    Optional<User> findByUsername(String username);

}

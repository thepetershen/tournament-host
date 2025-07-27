package com.tournamenthost.connect.frontend.with.backend.Repository;


import org.springframework.data.repository.CrudRepository;

import com.tournamenthost.connect.frontend.with.backend.Model.User;


public interface UserRepository extends CrudRepository<User, Long>{
    
    boolean existsByEmail(String email);

    User findByUsername(String username);

}

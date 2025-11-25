package com.tournamenthost.connect.frontend.with.backend.Repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tournamenthost.connect.frontend.with.backend.Model.User;


public interface UserRepository extends CrudRepository<User, Long>{
    
    boolean existsByUsername(String email);

    Optional<User> findByUsername(String username);


    @Query("SELECT u FROM User u WHERE LOWER(REPLACE(u.username, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:query, ' ', ''), '%')) OR LOWER(REPLACE(u.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:query, ' ', ''), '%'))")
    List<User> findByUsernameOrNameContainingIgnoreCaseAndSpaces(@Param("query") String query, Pageable pageable);
}

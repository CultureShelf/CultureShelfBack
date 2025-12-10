package com.example.cultureshelf.web;

import java.util.List;

import com.example.cultureshelf.entity.User;
import com.example.cultureshelf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public User create(@RequestBody User user) {
        return userRepository.save(user);
    }


    @GetMapping("/users")
    List<User> all() {
        return userRepository.findAll();
    }
}

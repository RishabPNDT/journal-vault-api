package com.rj.journal.App.service;

import com.rj.journal.App.entity.User;
import com.rj.journal.App.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository users, BCryptPasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String password) {
        if (users.findByUserName(username) != null) throw new IllegalStateException("Username is already taken");
        User user = new User(username, passwordEncoder.encode(password));
        return users.save(user);
    }

    public void saveUser(User user) { users.save(user); }
    public User findByUserName(String userName) { return users.findByUserName(userName); }
}

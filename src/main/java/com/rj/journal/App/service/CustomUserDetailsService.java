package com.rj.journal.App.service;

import com.rj.journal.App.entity.User;
import com.rj.journal.App.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUserName(username);

        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}

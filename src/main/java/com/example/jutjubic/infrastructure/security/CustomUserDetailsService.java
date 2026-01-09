package com.example.jutjubic.infrastructure.security;

import com.example.jutjubic.infrastructure.entity.UserEntity;
import com.example.jutjubic.infrastructure.repository.JpaUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Ucitavanje korisnika iz baze
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private JpaUserRepository userRepository;

    // Vraca usera
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Pronadji korisnika u bazi po email-u
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Korisnik sa email-om '%s' ne postoji.", email)
                ));

        // 2. Svi korisnici imaju user role
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); //Bukvalno hardcoded

        // 3. Vrati Spring Security User objekat
        return new User(
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.isActivated(),
                true,
                true,
                true,
                authorities
        );
    }
}
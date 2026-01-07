package com.example.jutjubic.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "address")
    private String address;

    // Mora da aktivira nalog preko linka sa mail-a
    @Column(name = "activated", nullable = false)
    private boolean activated = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_password_reset")
    private LocalDateTime lastPasswordReset;

    // Pre cuvanja u bazu sacuvaj vreme pravljenja
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastPasswordReset = LocalDateTime.now();
    }

    // Setter za password - pamti i vreme promene sifre
    public void setPassword(String password) {
        this.password = password;
        this.lastPasswordReset = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
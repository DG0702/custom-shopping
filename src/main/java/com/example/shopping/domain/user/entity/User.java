package com.example.shopping.domain.user.entity;

import com.example.shopping.domain.common.entity.TimeStamped;
import com.example.shopping.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(unique = true)
    public String email;
    public String password;
    public String address;
    @Enumerated(EnumType.STRING)
    public Role role;
}

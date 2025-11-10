package com.example.shopping.domain.user.entity;

import com.example.shopping.global.common.entity.BaseEntity;
import com.example.shopping.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    private String address;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public User(String email, String password, String name, String address, UserRole userRole){
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        this.userRole = userRole;
    }

    public void changeRole(UserRole newRole){
        this.userRole = newRole;
    }
}

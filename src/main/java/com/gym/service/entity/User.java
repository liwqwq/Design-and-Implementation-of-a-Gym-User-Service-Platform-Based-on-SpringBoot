package com.gym.service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    @JsonIgnore
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    private LocalDate birthday;
    
    private String gender;
    
    private String address;

    /** 健康状况说明（如慢性病、运动禁忌）；非空时教练端显示为有疾病需注意 */
    @Column(length = 500)
    private String medicalNotes;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean active;
    
    @Column(nullable = false, columnDefinition = "varchar(255) default 'USER'")
    private String role;
}
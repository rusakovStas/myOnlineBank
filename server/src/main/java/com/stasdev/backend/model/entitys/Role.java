package com.stasdev.backend.model.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long roleId;

    private String role;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    private Set<ApplicationUser> users = new HashSet<>();


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<ApplicationUser> getUsers() {
        return users;
    }

    public void setUsers(Set<ApplicationUser> users) {
        this.users = users;
    }

    public Role(String role, Set<ApplicationUser> users) {
        this.role = role;
        this.users = users;
    }

    public Role(String role) {
        this.role = role;
    }

    public Role(){

    }
}

package com.jayanta.usermanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jayanta.usermanagement.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data @NoArgsConstructor
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 500)
    private String password;

    @JoinColumn(name = "role_id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;

    @Column(length = 36)
    private String licenseActivatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime licenseActivatedOn;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime licenseExpiredOn;

    @Column(nullable = false)
    private Boolean isInternal = false;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedAt;

    @Column(length = 36)
    private String modifiedBy;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName().getAuthority()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public boolean isLicenseValid() {
        if (isInternal || role.getName() == UserRole.Admin) return true;
        if (licenseExpiredOn == null) return false;
        return licenseExpiredOn.isAfter(LocalDateTime.now());
    }
}

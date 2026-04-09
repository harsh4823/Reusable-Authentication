package com.auth.auth_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Realm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "realm_id")
    private Long realmId;

    @Column(unique = true,nullable = false)
    private String realmName;

    private String displayName;

    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id",nullable = false)
    private AuthUser owner;

    @OneToMany(mappedBy = "memberRealm",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<AuthUser> members = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

}

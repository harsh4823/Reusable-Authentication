package com.auth.auth_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name","realm_id"})
)
public class Role {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realm_id")
    private Realm realm;
}

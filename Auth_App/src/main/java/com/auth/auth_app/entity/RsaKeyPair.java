package com.auth.auth_app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class RsaKeyPair {

    @Id
    private String pairId;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String publicKey;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String privateKey;
}

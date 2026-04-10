package com.auth.auth_app.model;

import java.time.LocalDateTime;

public record RealmResponse(
   Long realmId,
   String realmName,
   String displayName,
   boolean enabled,
   String ownerEmail,
   LocalDateTime createdAt
) {}

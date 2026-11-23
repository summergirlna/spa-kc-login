package com.example.spakclogin.registration.repository;

import com.example.spakclogin.registration.model.AppUser;
import org.apache.ibatis.annotations.*;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper
public interface AppUserRepository {

    @Select("""
            SELECT COUNT(*)
            FROM app_users
            WHERE email = #{email}
            """)
    int countByEmail(@Param("email") String email);

    @Select("""
            SELECT COALESCE(MAX(id), 0) + 1
            FROM app_users
            """)
    long nextIdValue();

    @Insert("""
            INSERT INTO app_users (
                user_id,
                email,
                keycloak_user_id,
                mfa_type,
                status,
                created_at,
                updated_at
            )
            VALUES (
                #{userId},
                #{email},
                #{keycloakUserId},
                #{mfaType},
                #{status},
                #{createdAt},
                #{updatedAt}
            )
            """)
    void insert(
            @Param("userId") String userId,
            @Param("email") String email,
            @Param("keycloakUserId") String keycloakUserId,
            @Param("mfaType") String mfaType,
            @Param("status") String status,
            @Param("createdAt") OffsetDateTime createdAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Select("""
            SELECT
                id,
                user_id AS userId,
                email,
                keycloak_user_id AS keycloakUserId,
                mfa_type AS mfaType,
                status,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM app_users
            WHERE keycloak_user_id = #{keycloakUserId}
            """)
    Optional<AppUser> findByKeycloakUserId(@Param("keycloakUserId") String keycloakUserId);

    @Update("""
            UPDATE app_users
            SET email = #{email},
                updated_at = #{updatedAt}
            WHERE user_id = #{userId}
            """)
    void updateEmail(
            @Param("userId") String userId,
            @Param("email") String email,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
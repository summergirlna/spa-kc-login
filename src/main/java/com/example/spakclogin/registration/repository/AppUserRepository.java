package com.example.spakclogin.registration.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.OffsetDateTime;

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
}
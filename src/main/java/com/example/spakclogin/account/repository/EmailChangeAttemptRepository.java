package com.example.spakclogin.account.repository;

import com.example.spakclogin.account.model.EmailChangeAttempt;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper
public interface EmailChangeAttemptRepository {

    @Insert("""
            INSERT INTO email_change_attempts (
                change_id,
                user_id,
                keycloak_user_id,
                current_email,
                new_email,
                confirmation_code,
                status,
                verified_at,
                expires_at,
                created_at,
                updated_at
            )
            VALUES (
                #{changeId},
                #{userId},
                #{keycloakUserId},
                #{currentEmail},
                #{newEmail},
                #{confirmationCode},
                #{status},
                NULL,
                #{expiresAt},
                #{createdAt},
                #{updatedAt}
            )
            """)
    void insert(
            @Param("changeId") String changeId,
            @Param("userId") String userId,
            @Param("keycloakUserId") String keycloakUserId,
            @Param("currentEmail") String currentEmail,
            @Param("newEmail") String newEmail,
            @Param("confirmationCode") String confirmationCode,
            @Param("status") String status,
            @Param("expiresAt") OffsetDateTime expiresAt,
            @Param("createdAt") OffsetDateTime createdAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Select("""
            SELECT
                change_id AS changeId,
                user_id AS userId,
                keycloak_user_id AS keycloakUserId,
                current_email AS currentEmail,
                new_email AS newEmail,
                confirmation_code AS confirmationCode,
                status,
                verified_at AS verifiedAt,
                expires_at AS expiresAt,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM email_change_attempts
            WHERE change_id = #{changeId}
            """)
    Optional<EmailChangeAttempt> findByChangeId(@Param("changeId") String changeId);

    @Update("""
            UPDATE email_change_attempts
            SET status = #{status},
                verified_at = #{verifiedAt},
                updated_at = #{updatedAt}
            WHERE change_id = #{changeId}
            """)
    void updateVerified(
            @Param("changeId") String changeId,
            @Param("status") String status,
            @Param("verifiedAt") OffsetDateTime verifiedAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE email_change_attempts
            SET status = #{status},
                updated_at = #{updatedAt}
            WHERE change_id = #{changeId}
            """)
    void updateStatus(
            @Param("changeId") String changeId,
            @Param("status") String status,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
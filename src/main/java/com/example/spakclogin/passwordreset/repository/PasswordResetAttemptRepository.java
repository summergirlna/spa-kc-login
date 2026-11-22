package com.example.spakclogin.passwordreset.repository;

import com.example.spakclogin.passwordreset.model.PasswordResetAttempt;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper
public interface PasswordResetAttemptRepository {

    @Insert("""
            INSERT INTO password_reset_attempts (
                reset_id,
                email,
                confirmation_code,
                status,
                verified_at,
                expires_at,
                created_at,
                updated_at
            )
            VALUES (
                #{resetId},
                #{email},
                #{confirmationCode},
                #{status},
                NULL,
                #{expiresAt},
                #{createdAt},
                #{updatedAt}
            )
            """)
    void insert(
            @Param("resetId") String resetId,
            @Param("email") String email,
            @Param("confirmationCode") String confirmationCode,
            @Param("status") String status,
            @Param("expiresAt") OffsetDateTime expiresAt,
            @Param("createdAt") OffsetDateTime createdAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Select("""
            SELECT
                reset_id AS resetId,
                email,
                confirmation_code AS confirmationCode,
                status,
                verified_at AS verifiedAt,
                expires_at AS expiresAt,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM password_reset_attempts
            WHERE reset_id = #{resetId}
            """)
    Optional<PasswordResetAttempt> findByResetId(@Param("resetId") String resetId);

    @Update("""
            UPDATE password_reset_attempts
            SET status = #{status},
                verified_at = #{verifiedAt},
                updated_at = #{updatedAt}
            WHERE reset_id = #{resetId}
            """)
    void updateVerified(
            @Param("resetId") String resetId,
            @Param("status") String status,
            @Param("verifiedAt") OffsetDateTime verifiedAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE password_reset_attempts
            SET status = #{status},
                updated_at = #{updatedAt}
            WHERE reset_id = #{resetId}
            """)
    void updateStatus(
            @Param("resetId") String resetId,
            @Param("status") String status,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
package com.example.spakclogin.registration.repository;

import com.example.spakclogin.registration.model.RegistrationAttempt;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper
public interface RegistrationAttemptRepository {

    @Insert("""
            INSERT INTO registration_attempts (
                registration_id,
                email,
                confirmation_code,
                status,
                verified_at,
                expires_at,
                created_at,
                updated_at
            )
            VALUES (
                #{registrationId},
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
            @Param("registrationId") String registrationId,
            @Param("email") String email,
            @Param("confirmationCode") String confirmationCode,
            @Param("status") String status,
            @Param("expiresAt") OffsetDateTime expiresAt,
            @Param("createdAt") OffsetDateTime createdAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Select("""
            SELECT
                registration_id AS registrationId,
                email,
                confirmation_code AS confirmationCode,
                status,
                verified_at AS verifiedAt,
                expires_at AS expiresAt,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM registration_attempts
            WHERE registration_id = #{registrationId}
            """)
    Optional<RegistrationAttempt> findByRegistrationId(@Param("registrationId") String registrationId);

    @Update("""
            UPDATE registration_attempts
            SET status = #{status},
                verified_at = #{verifiedAt},
                updated_at = #{updatedAt}
            WHERE registration_id = #{registrationId}
            """)
    void updateVerified(
            @Param("registrationId") String registrationId,
            @Param("status") String status,
            @Param("verifiedAt") OffsetDateTime verifiedAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE registration_attempts
            SET status = #{status},
                updated_at = #{updatedAt}
            WHERE registration_id = #{registrationId}
            """)
    void updateStatus(
            @Param("registrationId") String registrationId,
            @Param("status") String status,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Update("""
            UPDATE registration_attempts
            SET confirmation_code = #{confirmationCode},
                status = #{status},
                verified_at = NULL,
                expires_at = #{expiresAt},
                updated_at = #{updatedAt}
            WHERE registration_id = #{registrationId}
            """)
    void updateConfirmationCode(
            @Param("registrationId") String registrationId,
            @Param("confirmationCode") String confirmationCode,
            @Param("status") String status,
            @Param("expiresAt") OffsetDateTime expiresAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
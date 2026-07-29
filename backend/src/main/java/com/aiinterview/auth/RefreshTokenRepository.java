package com.aiinterview.auth;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Redis 기반 RefreshToken 저장소 인터페이스.
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
}

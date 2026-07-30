package com.aiinterview.auth;

import org.springframework.data.repository.CrudRepository;

/** Redis 기반 Access Token 블랙리스트 저장소. */
public interface BlacklistedAccessTokenRepository extends CrudRepository<BlacklistedAccessToken, String> {
}

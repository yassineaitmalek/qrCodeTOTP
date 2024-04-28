package com.test.repositories.local;

import org.springframework.stereotype.Repository;

import com.test.models.local.GAuthSecret;
import com.test.repositories.config.AbstractRepository;

import java.util.Optional;

@Repository
public interface GAuthSecretRepository extends AbstractRepository<GAuthSecret, String> {

  Optional<GAuthSecret> findTopByUserName(String userName);
}

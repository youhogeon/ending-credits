package com.youhogeon.credits.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.youhogeon.credits.entity.Config;

public interface ConfigRepository extends JpaRepository<Config, String> {
    
}

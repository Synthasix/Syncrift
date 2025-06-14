package com.hexplatoon.syncrift_backend.repository;

import com.hexplatoon.syncrift_backend.entity.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
}

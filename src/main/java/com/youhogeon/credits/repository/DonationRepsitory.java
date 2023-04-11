package com.youhogeon.credits.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.youhogeon.credits.entity.Donation;

public interface DonationRepsitory extends JpaRepository<Donation, Long> {

    @Query(value = "SELECT d FROM Donation d WHERE d.seq IN (" +
        "SELECT MAX(sub.seq) FROM Donation sub WHERE sub.type = ?1 AND sub.createdAt > ?2 GROUP BY sub.id" +
        ")")
    List<Donation> findByType(Donation.Type type, LocalDateTime from);

    @Query(value = "SELECT d FROM Donation d WHERE d.seq IN (" +
        "SELECT MAX(sub.seq) FROM Donation sub WHERE sub.type = ?1 and sub.platform = ?2 and sub.createdAt > ?3 GROUP BY sub.id" +
        ")")
    List<Donation> findByTypeAndPlatform(Donation.Type type, Donation.Platform platform, LocalDateTime from);

    @Query(value = "SELECT COUNT(DISTINCT CONCAT(d.id, '∥', d.type, '∥', d.platform)) FROM Donation d WHERE d.createdAt > ?1")
    Long count(LocalDateTime from);

}

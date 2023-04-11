package com.youhogeon.credits.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    private Platform platform = Platform.TWITCH;

    private Type type;

	private String id;

	private String nickname;

    @Column(length = 1000)
	private String comment;
    
	private int amount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static enum Type {
        DONATION, FOLLOW, SUBSCRIPTION, RAID, BITS
    }

    public static enum Platform {
        TWIP, TOONATION, TWITCH
    }

}

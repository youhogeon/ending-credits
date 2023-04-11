package com.youhogeon.credits.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Config {
    
    @Id
    @Column(name = "id")
    private String key;

    @Column(name = "data")
    private String value;

}

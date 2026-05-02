package com.kota.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "yoco_settings")
public class YocoSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "secret_key", length = 500)
    private String secretKey;

    @Column(name = "webhook_secret", length = 500)
    private String webhookSecret;

    @Column(nullable = false, length = 20)
    private String environment = "test";

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

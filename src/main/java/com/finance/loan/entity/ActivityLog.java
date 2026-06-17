package com.finance.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actorEmail;
    private String actorRole;
    private String action;
    private String entityType;
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime timestamp;
}
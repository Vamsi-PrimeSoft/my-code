package com.medscheduler.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Tablet name
    private int quantity;       // Stock left

    // ✅ Custom times for each period
    @Column(name = "morning_time")
    private LocalTime morningTime;

    @Column(name = "afternoon_time")
    private LocalTime afternoonTime;

    @Column(name = "night_time")
    private LocalTime nightTime;

    @ManyToOne
    @JoinColumn(name = "family_id")
    private Family family;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalTime getMorningTime() { return morningTime; }
    public void setMorningTime(LocalTime morningTime) { this.morningTime = morningTime; }

    public LocalTime getAfternoonTime() { return afternoonTime; }
    public void setAfternoonTime(LocalTime afternoonTime) { this.afternoonTime = afternoonTime; }

    public LocalTime getNightTime() { return nightTime; }
    public void setNightTime(LocalTime nightTime) { this.nightTime = nightTime; }

    public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
}

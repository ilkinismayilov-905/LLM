package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_group_subject_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TeacherGroupSubject teacherGroupSubject;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private boolean isActive;

    public boolean isLessonOngoing(LocalDateTime currentTime) {
        return currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
    }

    public boolean hasLessonStarted(LocalDateTime currentTime) {
        return !currentTime.isBefore(startTime);
    }

    public boolean isWithinFirstFifteenMinutes(LocalDateTime currentTime) {
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(startTime.plusMinutes(15));
    }

    public boolean hasLessonEnded(LocalDateTime currentTime) {
        return !currentTime.isBefore(endTime);
    }
}


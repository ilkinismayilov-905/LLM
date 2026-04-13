package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints =  @UniqueConstraint(columnNames = {
        "student_id", "subject_id"
}))

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Min(0) @Max(10)
    private Integer attendanceScore;

    @Min(0) @Max(10)
    private Integer seminarScore;

    @Min(0) @Max(10)
    private Integer col1;

    @Min(0) @Max(10)
    private Integer col2;

    @Min(0) @Max(10)
    private Integer col3;

    @Min(0) @Max(50)
    private Integer examScore;

    private Integer totalScore;

    @Enumerated(EnumType.STRING)
    private GradeStatus status;



}

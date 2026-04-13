package com.example.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "teacher_group_subject",
uniqueConstraints = {
        @UniqueConstraint(columnNames = {"teacher_id","group_id","subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherGroupSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;


}

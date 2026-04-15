package com.example.repository;

import com.example.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

    List<Grade> findAllByStudentId(Long studentId);

    List<Grade> findAllByTeacherId(Long teacherId);
}

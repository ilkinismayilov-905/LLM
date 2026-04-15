package com.example.repository;

import com.example.entity.TeacherGroupSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherGroupSubjectRepository extends JpaRepository<TeacherGroupSubject, Long> {

    boolean existsByTeacherIdAndGroupIdAndSubjectId(Long teacherId, Long groupId, Long subjectId);

    TeacherGroupSubject findByTeacherIdAndGroupIdAndSubjectId(Long teacherId, Long groupId, Long subjectId);

    List<TeacherGroupSubject> findAllByTeacherId(Long teacherId);
}
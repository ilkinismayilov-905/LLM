package com.example.repository;

import com.example.entity.Subject;
import com.example.entity.TeacherGroupSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherGroupSubjectRepository extends JpaRepository<TeacherGroupSubject, Long> {

    boolean existsByTeacherIdAndGroupIdAndSubjectId(Long teacherId, Long groupId, Long subjectId);

    TeacherGroupSubject findByTeacherIdAndGroupIdAndSubjectId(Long teacherId, Long groupId, Long subjectId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"group", "subject"})
    List<TeacherGroupSubject> findAllByTeacherId(Long teacherId);

    /**
     * Fetch all distinct subjects for a group with details to avoid N+1 queries
     */
    @Query("SELECT DISTINCT s FROM TeacherGroupSubject tgs " +
            "JOIN tgs.subject s " +
            "WHERE tgs.group.id = :groupId")
    List<Subject> findDistinctSubjectsByGroupIdWithDetails(@Param("groupId") Long groupId);
}
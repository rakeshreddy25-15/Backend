package com.studyleague.repository;

import com.studyleague.model.PracticeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeSubmissionRepository extends JpaRepository<PracticeSubmission, String> {
    List<PracticeSubmission> findByPracticeTestId(String practiceTestId);
    List<PracticeSubmission> findByPracticeTestIdIn(List<String> practiceTestIds);
    List<PracticeSubmission> findByStudentId(String studentId);
}




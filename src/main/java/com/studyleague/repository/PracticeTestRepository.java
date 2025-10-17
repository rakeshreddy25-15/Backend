package com.studyleague.repository;

import com.studyleague.model.PracticeTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeTestRepository extends JpaRepository<PracticeTest, String> {
    List<PracticeTest> findByCourseIdOrderByCreatedAtDesc(String courseId);
}




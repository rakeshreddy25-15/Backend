package com.studyleague.repository;

import com.studyleague.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, String> {
    List<Material> findByCourseIdOrderByCreatedAtDesc(String courseId);
}




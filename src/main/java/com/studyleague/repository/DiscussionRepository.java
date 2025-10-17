package com.studyleague.repository;

import com.studyleague.model.Discussion;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface DiscussionRepository extends CrudRepository<Discussion, Long> {
    List<Discussion> findByCourseIdOrderByCreatedAtDesc(String courseId);
}

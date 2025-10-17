package com.studyleague.repository;

import com.studyleague.model.DiscussionPost;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface DiscussionPostRepository extends CrudRepository<DiscussionPost, Long> {
    List<DiscussionPost> findByDiscussionIdOrderByCreatedAtAsc(String discussionId);
}

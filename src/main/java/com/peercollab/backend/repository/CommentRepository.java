package com.peercollab.backend.repository;

import com.peercollab.backend.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}

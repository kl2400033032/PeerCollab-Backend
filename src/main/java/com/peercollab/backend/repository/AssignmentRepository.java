package com.peercollab.backend.repository;

import com.peercollab.backend.entity.Assignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByAssignedStudent_IdOrderByDueDateAsc(Long studentId);
}

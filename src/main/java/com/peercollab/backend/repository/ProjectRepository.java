package com.peercollab.backend.repository;

import com.peercollab.backend.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Override
    Page<Project> findAll(Specification<Project> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "assignment"})
    List<Project> findByStudent_Id(Long studentId);

    @EntityGraph(attributePaths = {"student", "assignment"})
    Optional<Project> findDetailedById(Long id);

    Optional<Project> findByAssignmentId(Long assignmentId);

    long countByStudent_Id(Long studentId);
}

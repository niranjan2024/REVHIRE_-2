package com.revhire.repository;

import com.revhire.entity.JobPosting;
import com.revhire.entity.JobStatus;
import com.revhire.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByEmployerOrderByCreatedAtDesc(User employer);
    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobStatus status);
    long countByEmployer(User employer);
    long countByEmployerAndStatus(User employer, JobStatus status);
}

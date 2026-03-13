package com.revhire.repository;

import com.revhire.entity.ApplicationStatus;
import com.revhire.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    @Modifying
    @Query("delete from JobApplication ja where ja.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);

    @Query("select ja from JobApplication ja where ja.job.employer.id = :employerId order by ja.appliedAt desc")
    List<JobApplication> findByEmployerId(@Param("employerId") Long employerId);

    List<JobApplication> findByJobSeekerIdOrderByAppliedAtDesc(Long jobSeekerId);

    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    JobApplication findByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    Optional<JobApplication> findByIdAndJobSeekerId(Long id, Long jobSeekerId);

    @Query("select count(ja) from JobApplication ja where ja.job.employer.id = :employerId")
    long countByEmployerId(@Param("employerId") Long employerId);

    @Query("select count(ja) from JobApplication ja where ja.job.employer.id = :employerId and ja.status = :status")
    long countByEmployerIdAndStatus(@Param("employerId") Long employerId, @Param("status") ApplicationStatus status);
}

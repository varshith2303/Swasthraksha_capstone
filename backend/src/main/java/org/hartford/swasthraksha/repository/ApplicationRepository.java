package org.hartford.swasthraksha.repository;

import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findAll();

    public List<Application> getByStatus(ApplicationStatus status);

    public List<Application> getByUserEmail(String email);

    public List<Application> getByStatusNot(ApplicationStatus status);

    public Application getByApplicationNumber(String applicationNumber);

    public List<Application> findByAssignedToEmail(String email);

    
    public List<Application> findByAssignedToEmailAndStatus(String email, ApplicationStatus status);
}

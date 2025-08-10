package de.his.patient.domain.repository;

import de.his.patient.domain.model.Patient;
import de.his.patient.domain.model.InsuranceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByKvnrAndDeletedAtIsNull(String kvnr);

    @Query("SELECT p FROM Patient p WHERE " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.kvnr LIKE CONCAT('%', :searchTerm, '%')) AND " +
           "p.deletedAt IS NULL")
    Page<Patient> searchPatients(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<Patient> findByInsuranceStatusAndDeletedAtIsNull(InsuranceStatus status);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.deletedAt IS NULL")
    Long countActivePatients();
}

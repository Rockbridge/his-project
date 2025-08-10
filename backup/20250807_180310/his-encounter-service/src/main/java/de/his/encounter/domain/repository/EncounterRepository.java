package de.his.encounter.domain.repository;

import de.his.encounter.domain.model.Encounter;
import de.his.encounter.domain.model.EncounterStatus;
import de.his.encounter.domain.model.EncounterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, UUID> {

        // Timeline-Queries für his
        @Query("SELECT e FROM Encounter e WHERE e.patientId = :patientId " +
                        "ORDER BY e.encounterDate DESC")
        Page<Encounter> findByPatientIdOrderByEncounterDateDesc(
                        @Param("patientId") UUID patientId, Pageable pageable);

        @Query("SELECT e FROM Encounter e WHERE e.patientId = :patientId " +
                        "AND e.encounterDate BETWEEN :fromDate AND :toDate " +
                        "ORDER BY e.encounterDate DESC")
        Page<Encounter> findByPatientIdAndDateRange(
                        @Param("patientId") UUID patientId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate,
                        Pageable pageable);

        // Status-basierte Queries
        List<Encounter> findByStatusAndEncounterDateBefore(
                        EncounterStatus status, LocalDateTime date);

        @Query("SELECT e FROM Encounter e WHERE e.practitionerId = :practitionerId " +
                        "AND e.status = :status ORDER BY e.encounterDate ASC")
        List<Encounter> findByPractitionerAndStatus(
                        @Param("practitionerId") UUID practitionerId,
                        @Param("status") EncounterStatus status);

        // Aggregation Queries für Statistiken
        @Query("SELECT COUNT(e) FROM Encounter e WHERE e.patientId = :patientId")
        Long countByPatientId(@Param("patientId") UUID patientId);

        @Query("SELECT e.type, COUNT(e) FROM Encounter e WHERE e.patientId = :patientId " +
                        "GROUP BY e.type")
        List<Object[]> countByPatientIdGroupByType(@Param("patientId") UUID patientId);

        // Letzte Encounters
        @Query("SELECT e FROM Encounter e WHERE e.patientId = :patientId " +
                        "AND e.status = 'COMPLETED' ORDER BY e.encounterDate DESC")
        Optional<Encounter> findLastCompletedEncounter(@Param("patientId") UUID patientId);
}
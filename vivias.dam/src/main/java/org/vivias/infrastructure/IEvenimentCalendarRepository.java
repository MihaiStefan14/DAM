package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vivias.domain.prognoza.EvenimentCalendar;

// Spring Data JPA Repository
public interface IEvenimentCalendarRepository extends JpaRepository<EvenimentCalendar, Long> {

	@Query("SELECT e FROM EvenimentCalendar e WHERE e.perioada.inceput <= :sfarsit AND e.perioada.sfarsit >= :inceput")
	List<EvenimentCalendar> findInPerioada(@Param("inceput") LocalDate inceput, @Param("sfarsit") LocalDate sfarsit);
}

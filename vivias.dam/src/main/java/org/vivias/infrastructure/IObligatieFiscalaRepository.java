package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.plati.ObligatieFiscala;

// Spring Data JPA Repository
public interface IObligatieFiscalaRepository extends JpaRepository<ObligatieFiscala, Long> {

	List<ObligatieFiscala> findByScadentaBetweenOrderByScadentaAsc(LocalDate inceput, LocalDate sfarsit);
}

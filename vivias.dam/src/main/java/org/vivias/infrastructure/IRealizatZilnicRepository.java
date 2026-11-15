package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.prognoza.RealizatZilnic;

// Spring Data JPA Repository
public interface IRealizatZilnicRepository extends JpaRepository<RealizatZilnic, Long> {

	RealizatZilnic findByData(LocalDate data);

	// istoricul folosit de CalculPrognozaService
	List<RealizatZilnic> findByDataBetweenOrderByDataAsc(LocalDate inceput, LocalDate sfarsit);
}

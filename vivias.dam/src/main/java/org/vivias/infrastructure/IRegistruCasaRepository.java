package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vivias.domain.casierie.OperatiuneCasa;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.StareRegistru;

// Spring Data JPA Repository
public interface IRegistruCasaRepository extends JpaRepository<RegistruCasa, Long> {

	// Queryable Named Operations
	// un singur registru pe zi: null daca ziua nu are inca registru
	RegistruCasa findByData(LocalDate data);

	List<RegistruCasa> findByStare(StareRegistru stare);

	// Queryable Annotated Operations
	// zilele INCHISE dinaintea datei, cea mai recenta prima: soldul ei faptic devine soldul de deschidere
	@Query("SELECT r FROM RegistruCasa r WHERE r.stare = org.vivias.domain.casierie.StareRegistru.INCHIS "
			+ "AND r.data < :data ORDER BY r.data DESC")
	List<RegistruCasa> findInchiseInainte(@Param("data") LocalDate data);

	@Query("SELECT o FROM RegistruCasa r JOIN r.operatii o WHERE r.idRegistru = :id ORDER BY o.operatiuneId")
	List<OperatiuneCasa> findOperatiiByIdRegistru(@Param("id") Long idRegistru);
}

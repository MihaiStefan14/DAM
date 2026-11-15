package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.casierie.AvansSpreDecontare;
import org.vivias.domain.casierie.StareAvans;

// Spring Data JPA Repository
public interface IAvansSpreDecontareRepository extends JpaRepository<AvansSpreDecontare, Long> {

	List<AvansSpreDecontare> findByStare(StareAvans stare);

	// verificarea plafonului de 5.000 lei pe zi din avansul unei persoane
	List<AvansSpreDecontare> findByIdAngajatAndDataAcordare(Long idAngajat, LocalDate dataAcordare);
}

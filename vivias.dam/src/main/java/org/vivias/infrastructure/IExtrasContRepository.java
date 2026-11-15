package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.vivias.domain.plati.ExtrasCont;
import org.vivias.domain.plati.TranzactieBancara;

// Spring Data JPA Repository
public interface IExtrasContRepository extends JpaRepository<ExtrasCont, Long> {

	// un extras nu se importa de doua ori
	boolean existsByData(LocalDate data);

	ExtrasCont findByData(LocalDate data);

	@Query("SELECT t FROM ExtrasCont e JOIN e.tranzactii t "
			+ "WHERE t.stare = org.vivias.domain.plati.StareReconciliere.NERECONCILIATA ORDER BY e.data")
	List<TranzactieBancara> findTranzactiiNereconciliate();
}

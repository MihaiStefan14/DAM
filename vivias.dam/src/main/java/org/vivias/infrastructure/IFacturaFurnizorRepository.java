package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vivias.domain.plati.FacturaFurnizor;
import org.vivias.domain.plati.StareFactura;

// Spring Data JPA Repository
public interface IFacturaFurnizorRepository extends JpaRepository<FacturaFurnizor, Long> {

	FacturaFurnizor findByIdFurnizorAndNumar(Long idFurnizor, String numar);

	List<FacturaFurnizor> findByStare(StareFactura stare);

	// ScadentarService: facturile neachitate cu scadenta in perioada prognozei
	@Query("SELECT f FROM FacturaFurnizor f WHERE f.stare = org.vivias.domain.plati.StareFactura.NEACHITATA "
			+ "AND f.scadenta BETWEEN :inceput AND :sfarsit ORDER BY f.scadenta")
	List<FacturaFurnizor> findNeachitateCuScadentaIntre(@Param("inceput") LocalDate inceput,
			@Param("sfarsit") LocalDate sfarsit);
}

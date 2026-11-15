package org.vivias.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.plati.IncasareAsteptata;

// Spring Data JPA Repository
public interface IIncasareAsteptataRepository extends JpaRepository<IncasareAsteptata, Long> {

	// ReconciliereService: incasarile inca neconfirmate din extras
	List<IncasareAsteptata> findByConfirmataFalse();
}

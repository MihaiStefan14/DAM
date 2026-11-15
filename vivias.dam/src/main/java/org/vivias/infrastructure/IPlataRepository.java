package org.vivias.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.plati.Plata;
import org.vivias.domain.plati.StarePlata;

// Spring Data JPA Repository
public interface IPlataRepository extends JpaRepository<Plata, Long> {

	List<Plata> findByStare(StarePlata stare);

	List<Plata> findByIdFactura(Long idFactura);

	// la invalidarea prognozei se anuleaza platile propuse din ea
	List<Plata> findByIdPrognozaSursaAndStare(Long idPrognozaSursa, StarePlata stare);
}

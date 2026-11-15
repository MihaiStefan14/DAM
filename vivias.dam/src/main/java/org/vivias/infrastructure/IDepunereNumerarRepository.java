package org.vivias.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.casierie.DepunereNumerar;
import org.vivias.domain.casierie.StareDepunere;

// Spring Data JPA Repository
public interface IDepunereNumerarRepository extends JpaRepository<DepunereNumerar, Long> {

	List<DepunereNumerar> findByStare(StareDepunere stare);

	// propunerea de ieri, nedusa la banca, se actualizeaza in loc sa se creeze alta
	DepunereNumerar findFirstByStareOrderByDataDesc(StareDepunere stare);
}

package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vivias.domain.prognoza.Prognoza;
import org.vivias.domain.prognoza.StarePrognoza;

// Spring Data JPA Repository
public interface IPrognozaRepository extends JpaRepository<Prognoza, Long> {

	List<Prognoza> findByStare(StarePrognoza stare);

	// regula "o singura prognoza activa pe aceleasi zile", verificata inainte de aprobare
	@Query("SELECT p FROM Prognoza p WHERE p.stare IN (org.vivias.domain.prognoza.StarePrognoza.APROBATA, "
			+ "org.vivias.domain.prognoza.StarePrognoza.IN_URMARIRE) "
			+ "AND p.perioada.inceput <= :sfarsit AND p.perioada.sfarsit >= :inceput")
	List<Prognoza> findActiveSuprapuse(@Param("inceput") LocalDate inceput, @Param("sfarsit") LocalDate sfarsit);
}

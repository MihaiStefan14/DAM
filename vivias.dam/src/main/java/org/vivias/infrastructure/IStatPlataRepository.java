package org.vivias.infrastructure;

import java.time.YearMonth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.plati.StatPlata;

// Spring Data JPA Repository
public interface IStatPlataRepository extends JpaRepository<StatPlata, Long> {

	StatPlata findByLuna(YearMonth luna);
}

package org.vivias.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.casierie.IncasareCard;
import org.vivias.domain.casierie.StareCard;

// Spring Data JPA Repository
public interface IIncasareCardRepository extends JpaRepository<IncasareCard, Long> {

	List<IncasareCard> findByStare(StareCard stare);

	IncasareCard findByDataVanzare(LocalDate dataVanzare);
}

package org.vivias.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.plati.Furnizor;

// Spring Data JPA Repository
public interface IFurnizorRepository extends JpaRepository<Furnizor, Long> {

	Furnizor findByCifValoare(String cif);

	List<Furnizor> findByDenumireContainingIgnoreCase(String denumire);
}

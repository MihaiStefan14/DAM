package org.vivias.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vivias.domain.casierie.RegistruCasaAuditView;

// Jurnalul de audit al registrului de casa
public interface IRegistruCasaAuditRepository extends JpaRepository<RegistruCasaAuditView, Long> {

	// jurnalul complet, in ordinea inregistrarii
	List<RegistruCasaAuditView> findAllByOrderByIdAsc();

	List<RegistruCasaAuditView> findByIdRegistruOrderByIdAsc(Long idRegistru);
}

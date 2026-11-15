package org.vivias.domain.casierie;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Domain Entity View folosita de serviciul de auditare AOP (analog
 * ProjectAuditView din modelul msd.t2_c4.spring_domain). Corespunde
 * conceptului "Jurnal de audit" din Tabelul 1.1/1.2, Cap.1 (Securitate,
 * audit si GDPR - Generic Subdomain). Persistata ca jurnal: o linie pe operatiune.
 */
@Entity
@Table(name = "RegistruCasaAudit")
public class RegistruCasaAuditView {

	@Id
	@GeneratedValue
	private Long id;

	private Long idRegistru;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "valoare", column = @Column(name = "suma_valoare")),
			@AttributeOverride(name = "moneda", column = @Column(name = "suma_moneda")) })
	private Suma suma;

	@Enumerated(EnumType.STRING)
	private EOperatiuneCasa operatiune;

	private LocalDateTime dataOra = LocalDateTime.now();

	// necesar doar pentru JPA
	protected RegistruCasaAuditView() {
		super();
	}

	public RegistruCasaAuditView(Long idRegistru, Suma suma, EOperatiuneCasa operatiune) {
		super();
		this.idRegistru = idRegistru;
		this.suma = suma;
		this.operatiune = operatiune;
	}

	public Long getId() {
		return id;
	}

	public Long getIdRegistru() {
		return idRegistru;
	}

	public Suma getSuma() {
		return suma;
	}

	public EOperatiuneCasa getOperatiune() {
		return operatiune;
	}

	public LocalDateTime getDataOra() {
		return dataOra;
	}

	@Override
	public String toString() {
		return "RegistruCasaAuditView [idRegistru=" + idRegistru + ", suma=" + suma + ", operatiune=" + operatiune
				+ "]";
	}

	public enum EOperatiuneCasa {
		REGISTRU_DESCHIS, RAPORT_Z_ATASAT, OPERATIUNE_INREGISTRATA, DIFERENTA_CONSTATATA, DIFERENTA_JUSTIFICATA,
		DIFERENTA_REGULARIZATA, ZI_INCHISA;
	}
}

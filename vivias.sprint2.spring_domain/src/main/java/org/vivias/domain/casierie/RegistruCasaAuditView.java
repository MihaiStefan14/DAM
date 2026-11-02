package org.vivias.domain.casierie;

/**
 * Domain Entity View folosita de serviciul de auditare AOP (analog
 * ProjectAuditView din modelul msd.t2_c4.spring_domain). Corespunde
 * conceptului "Jurnal de audit" din Tabelul 1.1/1.2, Cap.1 (Securitate,
 * audit si GDPR - Generic Subdomain).
 */
public class RegistruCasaAuditView {
	private Long idRegistru;
	private Suma suma;
	private EOperatiuneCasa operatiune;

	public RegistruCasaAuditView(Long idRegistru, Suma suma, EOperatiuneCasa operatiune) {
		super();
		this.idRegistru = idRegistru;
		this.suma = suma;
		this.operatiune = operatiune;
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

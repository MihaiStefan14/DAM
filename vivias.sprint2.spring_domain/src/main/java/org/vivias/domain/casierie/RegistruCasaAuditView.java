package org.vivias.domain.casierie;

/**
 * Domain Entity View folosita de serviciul de auditare AOP (analog
 * ProjectAuditView din modelul msd.t2_c4.spring_domain). Corespunde
 * conceptului "Jurnal de audit" din Tabelul 1.1/1.2, Cap.1 (Securitate,
 * audit si GDPR - Generic Subdomain).
 */
public class RegistruCasaAuditView {
	private Integer registruId;
	private Suma suma;
	private EOperatiuneCasa operatiune;

	public RegistruCasaAuditView(Integer registruId, Suma suma, EOperatiuneCasa operatiune) {
		super();
		this.registruId = registruId;
		this.suma = suma;
		this.operatiune = operatiune;
	}

	public Integer getRegistruId() {
		return registruId;
	}

	public Suma getSuma() {
		return suma;
	}

	public EOperatiuneCasa getOperatiune() {
		return operatiune;
	}

	@Override
	public String toString() {
		return "RegistruCasaAuditView [registruId=" + registruId + ", suma=" + suma + ", operatiune=" + operatiune
				+ "]";
	}

	public enum EOperatiuneCasa {
		INCASARE_ADAUGATA, PLATA_ADAUGATA, ZI_INCHISA, DIFERENTA_REGULARIZATA;
	}
}

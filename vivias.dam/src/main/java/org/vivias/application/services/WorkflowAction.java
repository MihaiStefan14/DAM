package org.vivias.application.services;

/*
 * Pasii workflow-ului zilei de casa, adresabili prin evenimente
 * (vezi IPlanningRegistruCasaBusinessWorkflowService pentru parametri).
 */
public enum WorkflowAction {
	DESCHIDE_REGISTRU_INITIAL, // [LocalDate data, Suma soldDeschidere] -> Long idRegistru
	DESCHIDE_REGISTRU, // [LocalDate data] -> Long idRegistru
	ATASEAZA_RAPORT_Z, // [Long idRegistru, Integer numar, Suma vanzariCard, Suma vanzariNumerar] -> Suma soldScriptic
	INREGISTREAZA_OPERATIUNE, // [Long idRegistru, TipOperatiune tip, Suma suma, String document] -> Integer operatiuneId
	INCHIDE_ZI, // [Long idRegistru, Suma soldFaptic] -> StareRegistru
	JUSTIFICA_DIFERENTA, // [Long idRegistru, String nota] -> StareRegistru
	APROBA_REGULARIZARE, // [Long idRegistru, Long idAdministrator] -> StareRegistru
	GET_REGISTRU_SUMAR; // [Long idRegistru] -> RegistruCasaSumarView
}

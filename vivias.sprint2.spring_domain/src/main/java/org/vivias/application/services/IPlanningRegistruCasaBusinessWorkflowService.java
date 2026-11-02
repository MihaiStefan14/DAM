package org.vivias.application.services;

import java.time.LocalDate;

import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipOperatiune;

/*
 * Business Workflow Steps/Actions - ziua de casa a insulei (APOO: CU5
 * InchideZiuaDeCasa, Figura 4; DAM Sprint 1: User Story 5 - "Inchiderea
 * zilei in registrul de casa"):
 * (0) Configurare initiala: primul registru, cu soldul de deschidere introdus
 * (1) Deschide registrul zilei: soldul de deschidere = soldul final al ultimei zile inchise
 * (2) Ataseaza raportul Z: vanzarile pe card si in numerar ale zilei
 * (3) Inregistreaza o operatiune de casa: plata furnizor, depunere, avans, restituire
 * (4) Inchide ziua: soldFaptic -> INCHIS (+ ZiCasaInchisaEvent) sau CU_DIFERENTE
 * (5) Justifica diferenta: nota explicativa a vanzatorului
 * (6) Aproba regularizarea (administrator) -> REGULARIZAT -> INCHIS (+ ZiCasaInchisaEvent)
 * (7) Obtine sumarul registrului: RegistruCasaSumarView
 *
 * Un singur registru pe zi: (0) si (1) refuza o data care are deja registru.
 */
public interface IPlanningRegistruCasaBusinessWorkflowService {

	Long deschideRegistruInitial(LocalDate data, Suma soldDeschidere);

	Long deschideRegistru(LocalDate data);

	Suma ataseazaRaportZ(Long idRegistru, int numarRaport, Suma vanzariCard, Suma vanzariNumerar);

	Integer inregistreazaOperatiune(Long idRegistru, TipOperatiune tip, Suma suma, String document);

	StareRegistru inchideZi(Long idRegistru, Suma soldFaptic);

	StareRegistru justificaDiferenta(Long idRegistru, String notaExplicativa);

	StareRegistru aprobaRegularizareSiFinalizeaza(Long idRegistru, Long idAdministrator);

	RegistruCasaSumarView getRegistruSumar(Long idRegistru);
}

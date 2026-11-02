package org.vivias.application.services;

import java.time.LocalDate;

import org.vivias.domain.casierie.Incasare.TipIncasare;
import org.vivias.domain.casierie.Plata.TipDestinatie;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;

/*
 * Business Workflow Steps/Actions (User Story 5, Sprint 1 - "Inchiderea
 * zilei in registrul de casa"; Figura 2, Cap.1 - swimlane "Casierie si
 * incasari"):
 * (1) Deschide registrul de casa al zilei: data, soldInitial
 * (2) Inregistreaza o incasare: registruId, suma, tip
 * (3) Inregistreaza o plata: registruId, suma, destinatie
 * (4) Inchide ziua: registruId, soldFaptic -> calculeaza soldCalculat si diferenta
 * (5) Aproba regularizarea diferentei (daca exista) -> inchide definitiv ziua
 * (6) Obtine sumarul registrului: RegistruCasaSumarView
 */
public interface IPlanningRegistruCasaBusinessWorkflowService {

	Integer deschideRegistru(LocalDate data, Suma soldInitial);

	Integer inregistreazaIncasare(Integer registruId, Suma suma, TipIncasare tip, String observatii);

	Integer inregistreazaPlata(Integer registruId, Suma suma, String destinatie, TipDestinatie tipDestinatie);

	StareRegistru inchideZi(Integer registruId, Suma soldFaptic);

	StareRegistru aprobaRegularizareSiFinalizeaza(Integer registruId);

	RegistruCasaSumarView getRegistruSumar(Integer registruId);
}

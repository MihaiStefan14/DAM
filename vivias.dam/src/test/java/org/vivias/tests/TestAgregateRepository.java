package org.vivias.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.vivias.domain.casierie.AvansSpreDecontare;
import org.vivias.domain.casierie.DepunereNumerar;
import org.vivias.domain.casierie.EvenimentDomeniu;
import org.vivias.domain.casierie.IncasareCard;
import org.vivias.domain.casierie.StareAvans;
import org.vivias.domain.casierie.StareCard;
import org.vivias.domain.casierie.StareDepunere;
import org.vivias.domain.casierie.Suma.Moneda;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.plati.CIF;
import org.vivias.domain.plati.CursBNR;
import org.vivias.domain.plati.ExtrasCont;
import org.vivias.domain.plati.FacturaFurnizor;
import org.vivias.domain.plati.Furnizor;
import org.vivias.domain.plati.IBAN;
import org.vivias.domain.plati.IncasareAsteptata;
import org.vivias.domain.plati.ObligatieFiscala;
import org.vivias.domain.plati.Plata;
import org.vivias.domain.plati.SensTranzactie;
import org.vivias.domain.plati.StareFactura;
import org.vivias.domain.plati.StarePlata;
import org.vivias.domain.plati.StatPlata;
import org.vivias.domain.plati.SursaIncasare;
import org.vivias.domain.plati.SursaPlata;
import org.vivias.domain.plati.TipObligatie;
import org.vivias.domain.prognoza.AbatereDepasitaEvent;
import org.vivias.domain.prognoza.EstimareZi;
import org.vivias.domain.prognoza.EvenimentCalendar;
import org.vivias.domain.prognoza.MetodaPrognoza;
import org.vivias.domain.prognoza.PragAbatere;
import org.vivias.domain.prognoza.Prognoza;
import org.vivias.domain.prognoza.RealizatZilnic;
import org.vivias.domain.prognoza.RecomandariPlataEvent;
import org.vivias.domain.prognoza.Scadenta;
import org.vivias.domain.prognoza.StarePrognoza;
import org.vivias.domain.prognoza.SursaRealizat;
import org.vivias.domain.prognoza.TipEveniment;
import org.vivias.domain.prognoza.TipScadenta;
import org.vivias.domain.shared.Perioada;
import org.vivias.infrastructure.IAvansSpreDecontareRepository;
import org.vivias.infrastructure.IDepunereNumerarRepository;
import org.vivias.infrastructure.IEvenimentCalendarRepository;
import org.vivias.infrastructure.IExtrasContRepository;
import org.vivias.infrastructure.IFacturaFurnizorRepository;
import org.vivias.infrastructure.IFurnizorRepository;
import org.vivias.infrastructure.IIncasareAsteptataRepository;
import org.vivias.infrastructure.IIncasareCardRepository;
import org.vivias.infrastructure.IObligatieFiscalaRepository;
import org.vivias.infrastructure.IPlataRepository;
import org.vivias.infrastructure.IPrognozaRepository;
import org.vivias.infrastructure.IRealizatZilnicRepository;
import org.vivias.infrastructure.IStatPlataRepository;

/*
 * Spring Data JPA Repository Tests pentru agregatele din APOO (Figurile 6.a - 6.c)
 * @Transactional: fiecare test se anuleaza (rollback) la final
 */
@SpringBootTest
@Transactional
public class TestAgregateRepository {
	private static Logger logger = Logger.getLogger(TestAgregateRepository.class.getName());

	// Casierie si incasari
	@Autowired private IAvansSpreDecontareRepository avansRepository;
	@Autowired private IDepunereNumerarRepository depunereRepository;
	@Autowired private IIncasareCardRepository incasareCardRepository;

	// Prognoza trezoreriei
	@Autowired private IPrognozaRepository prognozaRepository;
	@Autowired private IRealizatZilnicRepository realizatRepository;
	@Autowired private IEvenimentCalendarRepository calendarRepository;

	// Plati si relatia cu banca
	@Autowired private IFurnizorRepository furnizorRepository;
	@Autowired private IFacturaFurnizorRepository facturaRepository;
	@Autowired private IPlataRepository plataRepository;
	@Autowired private IObligatieFiscalaRepository obligatieRepository;
	@Autowired private IStatPlataRepository statPlataRepository;
	@Autowired private IExtrasContRepository extrasRepository;
	@Autowired private IIncasareAsteptataRepository incasareAsteptataRepository;

	private static final String CIF_VALID = "RO18547290";
	private static final String IBAN_VALID = "RO49AAAA1B31007593840000";

	private static Suma lei(long valoare) {
		return new Suma(BigDecimal.valueOf(valoare));
	}

	@Test
	public void test1_Casierie() {
		// avansul: acordat 1000, decontat cu bonuri de 820 -> 180 de restituit
		AvansSpreDecontare avans = avansRepository
				.save(AvansSpreDecontare.acorda(7L, lei(1000), "Marfa de la depozit", LocalDate.of(2035, 1, 5)));
		assertTrue(avans.esteNejustificat(LocalDate.of(2035, 1, 11), 3), "4 zile lucratoare > 3");
		assertEquals(lei(180), avans.deconteaza(lei(820)));
		avansRepository.save(avans);
		assertEquals(StareAvans.DECONTAT, avansRepository.findById(avans.getIdAvans()).orElseThrow().getStare());

		// depunerea: propusa, apoi dusa la banca
		DepunereNumerar depunere = depunereRepository.save(DepunereNumerar.propune(lei(500), LocalDate.of(2035, 1, 5)));
		assertEquals(depunere.getIdDepunere(),
				depunereRepository.findFirstByStareOrderByDataDesc(StareDepunere.PROPUSA).getIdDepunere());
		depunere.marcheazaDepusa();
		depunereRepository.save(depunere);
		assertEquals(1, depunereRepository.findByStare(StareDepunere.IN_TRANZIT).size());

		// cardul: 1000 brut, 985 primit -> 15 comision
		IncasareCard card = incasareCardRepository.save(new IncasareCard(LocalDate.of(2035, 1, 5), lei(1000)));
		assertTrue(card.esteIntarziata(LocalDate.of(2035, 1, 9), 2));
		assertEquals(lei(15), card.confirmaDecontarea(lei(985)));
		assertEquals(StareCard.DECONTATA,
				incasareCardRepository.findByDataVanzare(LocalDate.of(2035, 1, 5)).getStare());
	}

	@Test
	public void test2_PrognozaTrezoreriei() {
		Perioada saptamana = new Perioada(LocalDate.of(2035, 2, 1), LocalDate.of(2035, 2, 7));
		List<EstimareZi> estimari = saptamana.getInceput().datesUntil(saptamana.getSfarsit().plusDays(1))
				.map(d -> new EstimareZi(d, lei(1000), lei(900), null)).toList();
		List<Scadenta> scadente = List.of(new Scadenta(41L, TipScadenta.FACTURA, lei(3000), LocalDate.of(2035, 2, 3)),
				new Scadenta(null, TipScadenta.CHIRIE, lei(2500), LocalDate.of(2035, 2, 5)));

		// 7 zile; sold: 2000 + 7 x 900 - 3000 - 2500 = 2800
		Prognoza prognoza = Prognoza.creeaza(saptamana, MetodaPrognoza.MEDIE_MOBILA, lei(2000), lei(1800), estimari,
				scadente);
		prognoza = prognozaRepository.save(prognoza);
		assertEquals(7, prognoza.getZile().size());
		assertEquals(lei(2800), prognoza.getZile().get(6).getSoldEstimat());
		assertTrue(prognoza.getZile().get(2).isDeficit(), "ziua 3: 2000 + 3 x 900 - 3000 = 1700 < 1800");
		assertFalse(prognoza.getZile().get(1).isDeficit(), "ziua 2: 3800");

		// aprobarea: o singura recomandare (doar scadenta cu factura)
		List<EvenimentDomeniu> evenimente = prognoza.aproba(scadente);
		prognoza = prognozaRepository.save(prognoza);
		assertEquals(StarePrognoza.APROBATA, prognoza.getStare());
		assertEquals(1, prognoza.getRecomandari().size());
		assertTrue(evenimente.get(0) instanceof RecomandariPlataEvent);
		assertEquals(1, prognozaRepository
				.findActiveSuprapuse(LocalDate.of(2035, 2, 6), LocalDate.of(2035, 2, 20)).size());

		// realizatul primei zile: 600 vandut vs 1000 prognozat -> abatere 40% si 400 lei, sub pragul de 500 lei
		Optional<AbatereDepasitaEvent> alerta = prognoza.inregistreazaRealizat(LocalDate.of(2035, 2, 1), lei(600),
				lei(2500), false, PragAbatere.implicit());
		assertFalse(alerta.isPresent());
		assertEquals(StarePrognoza.IN_URMARIRE, prognoza.getStare());
		alerta = prognoza.inregistreazaRealizat(LocalDate.of(2035, 2, 2), lei(400), lei(2500), false,
				PragAbatere.implicit());
		assertTrue(alerta.isPresent(), "cumulat: 1000 vs 2000 -> 50% si 1000 lei");
		prognozaRepository.save(prognoza);

		RealizatZilnic realizat = realizatRepository.save(new RealizatZilnic(LocalDate.of(2035, 1, 31), lei(300),
				lei(700), SursaRealizat.RAPORT_Z, null));
		assertEquals(lei(1000), realizat.totalVanzari());
		assertFalse(realizat.esteComplet());
		assertEquals(1, realizatRepository
				.findByDataBetweenOrderByDataAsc(LocalDate.of(2035, 1, 1), LocalDate.of(2035, 1, 31)).size());

		calendarRepository.save(new EvenimentCalendar(new Perioada(LocalDate.of(2035, 2, 4), LocalDate.of(2035, 2, 4)),
				TipEveniment.MALL_INCHIS, "Inventar"));
		assertEquals(1, calendarRepository.findInPerioada(saptamana.getInceput(), saptamana.getSfarsit()).size());
	}

	@Test
	public void test3_PlatiSiBanca() {
		assertThrows(IllegalArgumentException.class, () -> new CIF("RO18547291"), "cifra de control gresita");
		assertThrows(IllegalArgumentException.class, () -> new IBAN("RO49AAAA1B31007593840001"));

		Furnizor furnizor = furnizorRepository.save(new Furnizor(new CIF(CIF_VALID), "Depozit Iasi SRL",
				new IBAN(IBAN_VALID), 30));
		assertEquals(furnizor.getIdPartener(), furnizorRepository.findByCifValoare("18547290").getIdPartener());

		FacturaFurnizor factura = facturaRepository.save(new FacturaFurnizor(furnizor.getIdPartener(), "F-102",
				LocalDate.of(2035, 3, 1), LocalDate.of(2035, 3, 31), lei(4000)));
		assertEquals(1, facturaRepository
				.findNeachitateCuScadentaIntre(LocalDate.of(2035, 3, 25), LocalDate.of(2035, 4, 5)).size());

		// plata prin banca: propusa -> aprobata -> trimisa -> executata; factura devine achitata
		Plata plata = Plata.propune(factura.getIdFactura(), furnizor.getDenumire(), furnizor.getIban(), lei(4000),
				LocalDate.of(2035, 3, 30), null, null);
		plata.aproba(1L);
		plata.trimiteLaBanca();
		factura.marcheazaAchitata(SursaPlata.BANCA, plata.executa().getEventId());
		plataRepository.save(plata);
		facturaRepository.save(factura);
		assertEquals(StarePlata.EXECUTATA, plataRepository.findByIdFactura(factura.getIdFactura()).get(0).getStare());
		assertEquals(StareFactura.ACHITATA, facturaRepository.findById(factura.getIdFactura()).orElseThrow().getStare());

		// chiria in euro: recalculata in lei inainte de aprobare
		Plata chirie = Plata.propune(null, "Mall Moldova", new IBAN(IBAN_VALID), new Suma(BigDecimal.valueOf(1000), Moneda.EUR),
				LocalDate.of(2035, 3, 5), null, null);
		assertThrows(IllegalStateException.class, () -> chirie.aproba(1L));
		chirie.recalculeazaInLei(new CursBNR(Moneda.EUR, new BigDecimal("4.9750"), LocalDate.of(2035, 3, 4)));
		assertEquals(new Suma(new BigDecimal("4975.00")), plataRepository.save(chirie).getSuma());

		ObligatieFiscala tva = obligatieRepository.save(new ObligatieFiscala(TipObligatie.TVA,
				new Perioada(LocalDate.of(2035, 2, 1), LocalDate.of(2035, 2, 28)), lei(1800), LocalDate.of(2035, 3, 25)));
		tva.confirmaSuma(lei(1750));
		assertFalse(obligatieRepository.save(tva).isEstimata());

		StatPlata stat = new StatPlata(YearMonth.of(2035, 3));
		stat.adaugaLinie(7L, lei(3500), new IBAN(IBAN_VALID));
		stat.adaugaLinie(8L, lei(3200), new IBAN(IBAN_VALID));
		statPlataRepository.save(stat);
		assertEquals(lei(6700), statPlataRepository.findByLuna(YearMonth.of(2035, 3)).totalNet());

		ExtrasCont extras = new ExtrasCont(LocalDate.of(2035, 3, 30), lei(12000));
		extras.adaugaTranzactie(lei(4000), "OP plata F-102", SensTranzactie.DEBIT);
		extras.adaugaTranzactie(lei(500), "Depunere numerar", SensTranzactie.CREDIT);
		extras = extrasRepository.save(extras);
		extras.marcheazaReconciliata(extras.getTranzactii().get(0).getId());
		extrasRepository.save(extras);
		assertTrue(extrasRepository.existsByData(LocalDate.of(2035, 3, 30)));
		assertEquals(1, extrasRepository.findTranzactiiNereconciliate().size());

		IncasareAsteptata depunere = incasareAsteptataRepository
				.save(new IncasareAsteptata(SursaIncasare.DEPUNERE, "12", lei(500), LocalDate.of(2035, 3, 30)));
		assertEquals(1, incasareAsteptataRepository.findByConfirmataFalse().size());
		depunere.confirma(lei(500));
		incasareAsteptataRepository.save(depunere);
		assertTrue(incasareAsteptataRepository.findByConfirmataFalse().isEmpty());
		logger.info("Plati si banca: " + furnizor + ", " + factura + ", " + stat);
	}
}

package org.vivias.application.services;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.vivias.domain.casierie.PlafoaneNumerar;
import org.vivias.domain.casierie.RaportZ;
import org.vivias.domain.casierie.RegistruCasa;
import org.vivias.domain.casierie.RegistruCasaSumarView;
import org.vivias.domain.casierie.StareRegistru;
import org.vivias.domain.casierie.Suma;
import org.vivias.domain.casierie.TipOperatiune;
import org.vivias.domain.casierie.ZiCasaInchisaEvent;
import org.vivias.domain.services.DomainEvent;
import org.vivias.domain.services.IConsolidatingRegistruCasaSumarViewDomainService;
import org.vivias.domain.services.IRegistruCasaEntityFactory;
import org.vivias.domain.services.IRegistruCasaEntityRepository;

/*
 * Serviciu subtire (APOO, cap. 3 - stratul de aplicatie): incarca agregatul,
 * ii cere radacinii operatia, valideaza si publica evenimentele. Regulile
 * unui singur registru stau in RegistruCasa; aici sta doar regula care
 * priveste mai multe registre ("un singur registru pe zi", soldul de deschidere
 * preluat din ziua anterioara).
 */
@Service("PlanningRegistruCasaBusinessWorkflowServiceFacade")
public class PlanningRegistruCasaBusinessWorkflowServiceImpl implements IPlanningRegistruCasaBusinessWorkflowService {

	// Support Services
	@Autowired
	private IRegistruCasaEntityRepository entityRepository;

	@Autowired
	@Qualifier("RegistruCasaEntityFactoryBase")
	private IRegistruCasaEntityFactory entityFactory;

	@Autowired
	private IConsolidatingRegistruCasaSumarViewDomainService consolidatingService;

	// parametru al aplicatiei (AppScanConfig)
	@Autowired
	private PlafoaneNumerar plafoane;

	// - DomainEvent: invoca Validation Service (ValidatingRegistruCasaDomainServiceImpl
	//   arunca RuntimeException daca agregatul e invalid)
	// - ZiCasaInchisaEvent: evenimentul de domeniu catre Prognoza
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	// (0) Configurare initiala
	@Override
	public Long deschideRegistruInitial(LocalDate data, Suma soldDeschidere) {
		return deschide(data, soldDeschidere);
	}

	// (1) Deschide registrul zilei, cu soldul final al ultimei zile inchise
	@Override
	public Long deschideRegistru(LocalDate data) {
		RegistruCasa ziuaAnterioara = entityRepository.getUltimulInchisInainte(data);
		if (ziuaAnterioara == null)
			throw new IllegalStateException(
					"Nu exista o zi de casa inchisa inainte de " + data + ": introduceti intai soldul de deschidere.");
		return deschide(data, ziuaAnterioara.getSoldFaptic());
	}

	private Long deschide(LocalDate data, Suma soldDeschidere) {
		if (entityRepository.getByData(data) != null)
			throw new IllegalStateException("Exista deja un registru de casa pentru " + data + ".");
		RegistruCasa registru = entityFactory.buildRegistruCasa(data, soldDeschidere);
		valideaza(registru); // inainte de add: un registru invalid nu ajunge in repository
		entityRepository.add(registru);
		return registru.getIdRegistru();
	}

	// (2) Ataseaza raportul Z
	@Override
	public Suma ataseazaRaportZ(Long idRegistru, int numarRaport, Suma vanzariCard, Suma vanzariNumerar) {
		RegistruCasa registru = getRegistru(idRegistru);
		registru.ataseazaRaportZ(new RaportZ(numarRaport, vanzariCard, vanzariNumerar));
		return registru.calculeazaSoldScriptic();
	}

	// (3) Inregistreaza o operatiune de casa
	@Override
	public Integer inregistreazaOperatiune(Long idRegistru, TipOperatiune tip, Suma suma, String document) {
		RegistruCasa registru = getRegistru(idRegistru);
		return registru.inregistreazaOperatiune(tip, suma, document).getOperatiuneId();
	}

	// (4) Inchide ziua (CU5, pasii 1-6)
	@Override
	public StareRegistru inchideZi(Long idRegistru, Suma soldFaptic) {
		RegistruCasa registru = getRegistru(idRegistru);

		registru.incepeInchiderea();
		registru.introduceSoldFaptic(soldFaptic);
		ZiCasaInchisaEvent ziInchisa = null;
		if (registru.getStare() == StareRegistru.IN_VERIFICARE) // fara diferenta
			ziInchisa = registru.inchide(plafoane);

		valideaza(registru);
		publica(ziInchisa);
		return registru.getStare();
	}

	// (5) Nota explicativa pentru diferenta (CU5, alternativa 1)
	@Override
	public StareRegistru justificaDiferenta(Long idRegistru, String notaExplicativa) {
		RegistruCasa registru = getRegistru(idRegistru);
		registru.justificaDiferenta(notaExplicativa);
		return registru.getStare();
	}

	// (6) Administratorul aproba regularizarea; cazul continua cu inchiderea (CU5, alternativa 1)
	@Override
	public StareRegistru aprobaRegularizareSiFinalizeaza(Long idRegistru, Long idAdministrator) {
		RegistruCasa registru = getRegistru(idRegistru);

		registru.aprobaRegularizare(idAdministrator);
		ZiCasaInchisaEvent ziInchisa = registru.inchide(plafoane);

		valideaza(registru);
		publica(ziInchisa);
		return registru.getStare();
	}

	// (7) Sumarul registrului
	@Override
	public RegistruCasaSumarView getRegistruSumar(Long idRegistru) {
		RegistruCasa registru = getRegistru(idRegistru);
		return consolidatingService.getRegistruCasaSumarViewDataOf(registru);
	}

	// dupa validare: un registru invalid nu anunta Prognoza ca ziua s-a inchis
	// (in APOO, cap. 4, evenimentul se salveaza in Outbox in aceeasi tranzactie)
	private void publica(ZiCasaInchisaEvent ziInchisa) {
		if (ziInchisa != null)
			applicationEventPublisher.publishEvent(ziInchisa);
	}

	private RegistruCasa getRegistru(Long idRegistru) {
		RegistruCasa registru = entityRepository.getById(idRegistru);
		if (registru == null)
			throw new IllegalArgumentException("Registrul de casa " + idRegistru + " nu exista!");
		return registru;
	}

	private void valideaza(RegistruCasa registru) {
		applicationEventPublisher.publishEvent(new DomainEvent(this, registru));
	}
}

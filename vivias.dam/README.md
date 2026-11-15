# Vivias DAM - Casierie și încasări (VIVIAS INVEST S.R.L.)

Proiectul disciplinei DAM pentru sistemul de gestiune a plăților și prognoza
trezoreriei VIVIAS INVEST, construit incremental, sprint cu sprint:

| Sprint | Cerință | Proiect-model urmat |
|---|---|---|
| Sprint 2 | Domain Entities + Business Rules (Validation / Computation / Workflow) | `msd.t2_c4.spring_domain` |
| Sprint 3 | Persistence Model + Repositories (JPA, Spring Boot) | `msd.t2_c5.springboot_data` |

Sprint 3 continuă Sprint 2 în același proiect: entitățile și serviciile de
business au rămas, iar acum lucrează cu baza de date. Modelul de persistență
acoperă toate agregatele din APOO (Figurile 6.a - 6.c), în 20 de tabele.

## Corelație cu celelalte livrabile

- **Domeniul** este cel din proiectul de Analiză și Proiectare Orientată
  Obiect al echipei (`Vivias_Capitolele_1-5_complet.pdf`) și din documentul
  Sprint 1 (User Story 5 - "Închiderea zilei în registrul de casă").
- Modelul de domeniu și regulile de business complete (validare, calcul,
  workflow, audit) sunt pe agregatul **`RegistruCasa`** din subdomeniul suport
  **"Casierie și încasări"** (APOO: Figura 6.b, CU5 `InchideZiuaDeCasa`).
- Celelalte agregate din cele 3 subdomenii ale Context Map-ului (Prognoza
  trezoreriei / Casierie și încasări / Plăți și relația cu banca) au
  entitățile, regulile proprii și repository-urile (Sprint 3).
- Testele reproduc ambele specificații:
  - User Story 5 (DAM Sprint 1): sold 1000 lei, încasări 500, plăți 300 ⇒
    sold scriptic 1200; sold faptic 1190 ⇒ diferență `MINUS` 10
    (`Test2`, `Test6`);
  - scenariile Gherkin din `inchide_ziua_de_casa.feature` (APOO CU5):
    închidere fără diferențe, diferență fără notă explicativă, sold peste
    plafonul intern, lipsa raportului Z (`Test3`).

### Ce e la fel ca în APOO și ce e adaptat

| APOO (Figura 6.b / CU5) | Implementare DAM |
|---|---|
| `RegistruCasa` cu `OperatiuneCasa`, `RaportZ`, `StareRegistru` | identic ca structură |
| identitatea registrului = ziua | `idRegistru` (`Long`, ca în Sprint 1) + regula „un singur registru pe zi” în workflow |
| `Suma` cu `java.util.Currency` | `Suma` cu enum-ul `Moneda`, persistat prin `@Convert(MonedaConverter)` |
| `ZiCasaInchisaEvent` salvat în Outbox, trimis prin JMS | publicat cu `ApplicationEventPublisher` (Spring Events) |
| plafoanele din Legea 70/2015 în `VerificarePlafoaneService` | doar plafonul intern de casă (`PlafoaneNumerar`); serviciul de verificare a plafoanelor nu e implementat |
| identificatori tipați (`IdFactura`, `IdPartener`, UUID) | `Long` generat de baza de date (`@GeneratedValue`) |
| `IdAngajat` pentru administrator | `Long` (`aprobatDe`) |

## Sprint 2 - Modelul de domeniu

### Agregatul `RegistruCasa`

- **Root Entity**: `RegistruCasa` (dată, sold de deschidere, raport Z, sold
  faptic, notă explicativă, aprobare, stare).
- **Component Entity**: `OperatiuneCasa` (tip, sumă, document justificativ),
  cu ID local în namespace-ul agregatului și back-reference `@ManyToOne`
  spre rădăcină.
- **Value Objects**: `Suma` (imutabil, valoare + `Moneda`, `@Embeddable`),
  `RaportZ` (`@Embeddable`), `PlafoaneNumerar`, enum-urile `TipOperatiune`,
  `StareRegistru`, `TipDiferenta`.
- **Domain Event**: `ZiCasaInchisaEvent` (vânzări card/numerar, sold casă,
  propunere de depunere), publicat la închiderea zilei, pentru Prognoză.
- **Entity Views**: `RegistruCasaSumarView`, `RegistruCasaAuditView`.

Rădăcina păstrează regulile registrului:

- operațiunile se adaugă doar prin `ataseazaRaportZ` / `inregistreazaOperatiune`
  (lista expusă e read-only), doar cât registrul e `DESCHIS` și doar în
  moneda registrului;
- încasările în numerar vin din raportul Z; vânzările pe card apar în raport,
  dar **nu** intră în soldul casei (banii merg la bancă);
- soldul scriptic nu se salvează, se calculează din operațiunile zilei;
- ziua se închide doar cu raportul Z atașat; o diferență cere notă
  explicativă și aprobarea administratorului.

```
DESCHIS -> IN_VERIFICARE -> INCHIS
                         -> CU_DIFERENTE -> REGULARIZAT -> INCHIS
```

### Business Rules

| Tip regulă            | Clase |
|------------------------|-------|
| Validare (JSR-380 + domeniu) | `IValidatingRegistruCasaDomainService` / Impl |
| Calcul pe câmpuri entitate     | `ICalculatingRegistruCasaDomainService` / Impl (sold scriptic, diferență, propunere de depunere) |
| Calcul pentru entity view       | `IConsolidatingRegistruCasaSumarViewDomainService` / Impl |
| Workflow (use-case), Simple Facade | `IPlanningRegistruCasaBusinessWorkflowService` / Impl |
| Workflow (use-case), Event-based | `PlanningRegistruCasaEventWorkflowServiceImpl` (`WorkflowCallEvent` → `WorkflowResponseEvent`) |
| Auditare (Spring AOP)  | `IAuditingRegistruCasaBusinessWorkflowService` / Impl + `AuditingRegistruCasaApplicationServiceImpl` |

Workflow-ul invocă serviciul de validare prin `DomainEvent` după deschiderea
registrului, după închiderea zilei și după regularizare, iar
`ZiCasaInchisaEvent` se publică abia după validare.

Configurația Spring este în `org.vivias.spring.AppScanConfig` (bean-ul proxy
AOP auditat + `Validator` + parametrul `PlafoaneNumerar`, cu plafonul intern
de 2.000 lei); aplicația pornește din `SpringBootData_ViviasApplication`.

## Sprint 3 - Persistența

### Strategia de persistență

- **Spring Data JPA** (cerința principală): `IRegistruCasaRepository extends JpaRepository<RegistruCasa, Long>`,
  folosit de servicii și de factory.
- **(Bonus) Complete JPA Repository**: interfața CRUD `IRegistruCasaEntityRepository`
  implementată de `RegistruCasaEntityRepositoryJPA`, cu `EntityManager` injectat prin `@Autowired`.

| Mediu | Bază de date | Configurare |
|---|---|---|
| Aplicație | PostgreSQL (ca în template) | `src/main/resources/application.properties`, `ddl-auto=update` |
| Teste | H2 în memorie (mod PostgreSQL) | `src/test/resources/application.properties`, `ddl-auto=create-drop` |

### Agregatele și tabelele (APOO, Figurile 6.a - 6.c)

Fiecare context are pachetul lui; tipurile comune stau în Shared Kernel
(`org.vivias.domain.shared.Perioada`; `Suma` a rămas în `casierie`, unde exista din Sprint 2).

| Context (pachet) | Agregat (rădăcină + componente) | Tabele |
|---|---|---|
| Casierie și încasări (`domain.casierie`) | `RegistruCasa` + `OperatiuneCasa` | RegistruCasa, OperatiuneCasa |
| | `AvansSpreDecontare` | AvansSpreDecontare |
| | `DepunereNumerar` | DepunereNumerar |
| | `IncasareCard` | IncasareCard |
| | jurnalul de audit (`RegistruCasaAuditView`) | RegistruCasaAudit |
| Prognoza trezoreriei (`domain.prognoza`) | `Prognoza` + `ZiPrognozata` + `RecomandarePlata` | Prognoza, ZiPrognozata, RecomandarePlata |
| | `RealizatZilnic` | RealizatZilnic |
| | `EvenimentCalendar` | EvenimentCalendar |
| Plăți și relația cu banca (`domain.plati`) | `Furnizor` | Furnizor |
| | `FacturaFurnizor` | FacturaFurnizor |
| | `Plata` | Plata |
| | `ObligatieFiscala` | ObligatieFiscala |
| | `StatPlata` + `LinieStat` | StatPlata, LinieStat |
| | `ExtrasCont` + `TranzactieBancara` | ExtrasCont, TranzactieBancara |
| | `IncasareAsteptata` | IncasareAsteptata |

Fiecare agregat are atributele și metodele din diagrame, cu regulile lui. Exemple:

- `Prognoza` păzește invarianții din Tabelul 2.1: o zi pentru fiecare dată din perioadă,
  lanțul soldurilor (`sold(zi) = sold(zi anterioară) + încasări - plăți`), marcajul de deficit,
  recomandări cel târziu la scadență, abaterea pe cumulat (peste 15% și peste 500 lei);
  ciclul `CIORNA → APROBATA → IN_URMARIRE → INCHISA`, sau `INVALIDATA`;
- `Plata`: `PROPUSA → APROBATA → TRIMISA_LA_BANCA → EXECUTATA | RESPINSA`, sau `ANULATA`;
  nu se șterge, iar plata în euro se recalculează în lei (`CursBNR`) înainte de aprobare;
- `AvansSpreDecontare`: decontare cu restul de restituit, alertă după 3 zile lucrătoare
  (trimisă o singură dată); `DepunereNumerar`: `PROPUSA → IN_TRANZIT → CONFIRMATA`;
- `IBAN` și `CIF` își verifică cifra de control la creare, deci un IBAN greșit nu poate exista;
- metodele întorc evenimentele de domeniu din diagrame (`RecomandariPlataEvent`,
  `DeficitPrognozatEvent`, `AbatereDepasitaEvent`, `PrognozaInvalidataEvent`,
  `PlataExecutataEvent`, `DepunereNumerarEvent`, `AvansNejustificatEvent`).

Constrângeri în baza de date: un registru de casă pe zi, un `RealizatZilnic` pe zi, un extras
pe zi, un stat de plată pe lună, CIF unic la furnizori, număr de factură unic la același furnizor.

Serviciile de domeniu din diagrame care lucrează peste mai multe agregate
(`CalculPrognozaService`, `ScadentarService`, `ReconciliereService`,
`VerificarePlafoaneService`) nu sunt implementate.

### Modelul de persistență (JPA)

| Element | Implementare |
|---|---|
| Entități | 20 de clase `@Entity`: rădăcinile agregatelor, componentele lor și jurnalul de audit |
| Identificatori | `@Id @GeneratedValue` (`Long`); la `OperatiuneCasa` numărul local `operatiuneId` rămâne coloană |
| Relația rădăcină → componente | `@OneToMany(mappedBy = ..., cascade = ALL, fetch = EAGER, orphanRemoval = true)`; la `Prognoza`, cu două liste, și `@OrderColumn` |
| Relația componentă → rădăcină | `@ManyToOne` (back-reference) |
| Relații între agregate | prin identitate (`idFactura`, `idFurnizor`, `idAngajat`, `idPrognozaSursa`), nu prin obiecte |
| Value Objects | `Suma`, `RaportZ`, `Perioada`, `IntervalSold`, `IBAN`, `CIF` ca `@Embeddable` / `@Embedded`, cu `@AttributeOverrides` |
| Enum-uri | `@Enumerated(EnumType.STRING)` pe toate stările și tipurile |
| Custom Data Types | `@Convert(MonedaConverter)` (moneda prin codul ISO), `@Convert(YearMonthConverter)` (luna statului de plată) |
| Câmpuri calculate | `@Transient` (sold scriptic, diferență) |
| Audit JPA | `@EntityListeners(RegistruCasaAuditListener)` cu `@PrePersist`, `@PreUpdate`, `@PreRemove` |

Modelul nu are ierarhii de clase (tipurile sunt enum-uri), deci nu folosește `@Inheritance`.

### Repository-uri

Câte un Spring Data `JpaRepository` pentru fiecare agregat, în `org.vivias.infrastructure`,
cu operațiile de căutare cerute de cazurile de utilizare, de exemplu:

| Repository | Operații |
|---|---|
| `IPrognozaRepository` | `findByStare`; `@Query findActiveSuprapuse` (regula „o singură prognoză activă pe aceleași zile”) |
| `IFacturaFurnizorRepository` | `findByIdFurnizorAndNumar`; `@Query findNeachitateCuScadentaIntre` (scadențarul) |
| `IPlataRepository` | `findByIdFactura`; `findByIdPrognozaSursaAndStare` (anularea plăților la invalidarea prognozei) |
| `IExtrasContRepository` | `existsByData` (un extras nu se importă de două ori); `@Query findTranzactiiNereconciliate` |
| `IDepunereNumerarRepository` | `findFirstByStareOrderByDataDesc` (propunerea de ieri se actualizează) |
| `IRegistruCasaAuditRepository` | jurnalul de audit, în ordinea înregistrării |

`IRegistruCasaRepository`:

| Operație | Tip |
|---|---|
| `findByData(LocalDate)` | Queryable Named Operation (un singur registru pe zi) |
| `findByStare(StareRegistru)` | Queryable Named Operation |
| `findInchiseInainte(LocalDate)` | `@Query`: zilele închise dinaintea datei, cea mai recentă prima |
| `findOperatiiByIdRegistru(Long)` | `@Query` cu `JOIN` pe operațiunile registrului |

Serviciile care folosesc repository-ul:

- `PlanningRegistruCasaBusinessWorkflowServiceImpl`: încarcă registrul (`findById`),
  aplică operația pe agregat, validează, apoi `save`; un registru invalid nu se salvează;
- `RegistruCasaEntityFactoryBase`: creează registrele (ID generat la salvare) și scrie
  datele inițiale doar când baza de date e goală;
- `RegistruCasaDomainServiceImpl`: numărul de operațiuni prin `findOperatiiByIdRegistru`;
- `AuditingRegistruCasaBusinessWorkflowServiceImpl`: scrie jurnalul de audit în baza de date.

## Teste (33 teste)

Toate testele sunt în același pachet, `src/test/java/org/vivias/tests`.

| Clasă | Sprint | Ce verifică |
|---|---|---|
| `Test1_SimpleDomainService` | 2 | datele seed |
| `Test2_ComputingDomainServices` | 2 | regulile de calcul (User Story 5, card, plafon, monede) |
| `Test3_BusinessWorkflowService` | 2, 3 | scenariile CU5 pe workflow-ul facade; verifică și ce s-a salvat în baza de date |
| `Test4_ValidatingDomainService` | 2 | validarea JSR-380 și regulile de agregat |
| `Test5_AuditedWorkflowService` | 2 | auditarea prin proxy AOP |
| `Test6_EventWorkflowService` | 2 | workflow-ul pe evenimente |
| `TestEntityManagerJPA` | 3 | maparea JPA prin `EntityManager` (salvare, citire, JPQL) |
| `TestRegistruCasaRepository` | 3 | Spring Data: CRUD, operațiile după nume, `@Query`, ștergerea în cascadă |
| `TestRegistruCasaEntityRepositoryJPA` | 3 | (Bonus) repository-ul cu `EntityManager` |
| `TestAgregateRepository` | 3 | celelalte agregate, câte un test pe context: salvare, citire, regulile principale, interogările |

Fiecare test își folosește propriile zile (un singur registru pe zi) și nu
modifică datele seed; testele de repository sunt `@Transactional` (rollback la final).

## Rulare

Testele (pe H2, fără instalări), din acest folder:

```bash
mvn test
```

Din IntelliJ: click dreapta pe `AllTests` → *Run*. `mvn test` rulează clasele individual.

Aplicația pe PostgreSQL: se creează utilizatorul `msd` (parola `msd`), ca în
`ScriptsDB.txt` din template, apoi se pornește `SpringBootData_ViviasApplication`.
Tabelele se creează automat la prima pornire (`ddl-auto=update`).

```sql
CREATE ROLE msd LOGIN PASSWORD 'msd';
CREATE SCHEMA msd;
ALTER SCHEMA msd OWNER TO msd;
```

## Tehnologii

Java 25, Spring Boot 4.1.1, Spring Data JPA (Hibernate), Jakarta Persistence,
Bean Validation, Spring Events, Spring AOP, PostgreSQL, H2 (teste), JUnit 5.

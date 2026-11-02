# Sprint 2 - Domain Model Implementation (VIVIAS INVEST S.R.L.)

Implementare pentru disciplina DAM, conform cerințelor din
`Sprint.2.Domain-Implementation.Requirements.pdf`: Domain Entities +
Business Rules (Validation / Computation / Workflow), urmând **structura
proiectului-model** `msd.t2_c4.spring_domain` (Spring, fără strat web,
agregat unic, servicii de domeniu si aplicație, teste JUnit 5).

## Corelație cu celelalte livrabile

- **Domeniul** este cel din proiectul de Analiză și Proiectare Orientată
  Obiect al echipei (`Vivias_Capitolele_1-5_complet.pdf`) și din backlog-ul
  `Sprint 1 dam.docx` (User Story 5 - "Închiderea zilei în registrul de casă").
- Din cele 3 subdomenii funcționale ale Context Map-ului (Prognoza
  trezoreriei / Casierie și încasări / Plăți și relația cu banca), acest
  Sprint implementează agregatul **`RegistruCasa`** din subdomeniul suport
  **"Casierie și încasări"** (APOO: Figura 6.b, CU5 `InchideZiuaDeCasa`).
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
| plafoanele din Legea 70/2015 în `VerificarePlafoaneService` | doar plafonul intern de casă (`PlafoaneNumerar`); restul țin de agregatele `AvansSpreDecontare` / `DepunereNumerar`, neimplementate în acest Sprint |
| `IdAngajat` pentru administrator | `Long` (`aprobatDe`) |

## Agregatul `RegistruCasa`

- **Root Entity**: `RegistruCasa` (dată, sold de deschidere, raport Z, sold
  faptic, notă explicativă, aprobare, stare).
- **Component Entity**: `OperatiuneCasa` (tip, sumă, document justificativ),
  cu ID local în namespace-ul agregatului și back-reference `@ManyToOne`
  spre rădăcină (`@OneToMany(mappedBy = "registru", cascade = ALL)`).
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

## Business Rules

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

Configurația Spring este în `org.vivias.spring.AppScanConfig`
(`@ComponentScan` + bean-ul proxy AOP auditat + `Validator` + parametrul
`PlafoaneNumerar`, cu plafonul intern de 2.000 lei).

## Rulare

```bash
mvn test
```

Teste JUnit 5 (`Test1`–`Test6`, plus suita `AllTests` pentru rulare din IDE),
acoperind toate cele trei categorii de servicii de business cerute (validare,
calcul, workflow facade și pe evenimente), plus fluxul integrat AOP de
auditare. Fiecare test își folosește propriile zile (un singur registru pe
zi) și nu modifică datele seed, deci clasele de test pot rula în orice ordine.

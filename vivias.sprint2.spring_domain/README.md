# Sprint 2 - Domain Model Implementation (VIVIAS INVEST S.R.L.)

Implementare pentru disciplina DAM, conform cerințelor din
`Sprint.2.Domain-Implementation.Requirements.pdf`: Domain Entities +
Business Rules (Validation / Computation / Workflow), urmând **structura
proiectului-model** `msd.t2_c4.spring_domain` (Spring, fără strat web,
agregat unic, servicii de domeniu si aplicație, teste JUnit 5).

## Corelație cu celelalte livrabile

- **Domeniul** este cel din `Vivias_Capitolul1_complet.pdf` (tema de
  Analiză și Proiectare Orientată Obiect a echipei, firma VIVIAS INVEST
  S.R.L.) și din backlog-ul `Sprint 1 dam.docx` (User Story 5 -
  "Închiderea zilei în registrul de casă").
- Din cele 3 subdomenii funcționale ale Context Map-ului (Cap. 1 -
  Prognoza trezoreriei / Casierie și încasări / Plăți și relația cu
  banca), acest Sprint implementează agregatul **`RegistruCasa`** din
  subdomeniul suport **"Casierie și încasări"**.
- Regula de calcul (`soldCalculat`, `diferența`) reproduce exact
  scenariul Gherkin din Sprint 1: sold inițial 1000 RON, încasări 500 RON,
  plăți 300 RON ⇒ sold calculat 1200 RON; sold faptic 1190 RON ⇒
  diferență `MINUS` 10 RON. Acest scenariu este și testul principal
  (`Test2_ComputingDomainServices`, `Test3_BusinessWorkflowService`).

## Agregatul `RegistruCasa`

- **Root Entity**: `RegistruCasa` (dată, sold inițial/faptic, stare).
- **Component Entities**: `Incasare`, `Plata`.
- **Value Objects**: `Suma` (valoare + `Moneda`, `@Embeddable`), enum-urile
  `StareRegistru`, `TipDiferenta`, `Incasare.TipIncasare`,
  `Plata.TipDestinatie`.
- **Entity Views**: `RegistruCasaSumarView`, `RegistruCasaAuditView`.

## Business Rules

| Tip regulă            | Clase |
|------------------------|-------|
| Validare (JSR-380 + domeniu) | `IValidatingRegistruCasaDomainService` / Impl |
| Calcul pe câmpuri entitate     | `ICalculatingRegistruCasaDomainService` / Impl |
| Calcul pentru entity view       | `IConsolidatingRegistruCasaSumarViewDomainService` / Impl |
| Workflow (use-case)   | `IPlanningRegistruCasaBusinessWorkflowService` / Impl |
| Auditare (Spring AOP)  | `IAuditingRegistruCasaBusinessWorkflowService` / Impl + `AuditingRegistruCasaApplicationServiceImpl` |

Configurația Spring este în `org.vivias.spring.AppScanConfig`
(`@ComponentScan` + bean-ul proxy AOP auditat + `Validator`).

## Rulare

```bash
mvn test
```

8 teste JUnit 5 (`src/test/java/org/vivias/domain/services`), acoperind
toate cele trei categorii de servicii de business cerute (validare,
calcul, workflow), plus fluxul integrat AOP de auditare.

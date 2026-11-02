# Ghid – Sprint 1 și Sprint 2 (DAM, VIVIAS INVEST S.R.L.)

Ghidul explică ce conține fiecare sprint, cum se leagă documentul din Sprint 1 de codul din Sprint 2 și ce s-a schimbat pe branch-ul `draft`.

---

## 1. Pe scurt

| | Sprint 1 – Business Domain Design | Sprint 2 – Domain Implementation |
|---|---|---|
| Ce se predă | documentul `docs/Sprint1_DAM_final.docx` | proiectul Maven `vivias.sprint2.spring_domain` |
| Ce cere profesorul | use cases, domain model (diagrama de clase, agregate, value objects), business rules, opțional diagrama SQL | entitățile de domeniu + regulile de business (validare, calcul, workflow), după proiectul-model `msd.t2_c4.spring_domain` |
| Ce acoperă | tot domeniul: casa zilnică, avansuri, plăți prin bancă | un singur agregat: **`RegistruCasa`** (US1, US2 parțial, US5) |
| Sursa domeniului | proiectul de APOO al echipei (`Vivias_Capitolul1_complet.docx`, Figura 6.b, CU5) | documentul din Sprint 1 |

---

## 2. Sprint 1 – documentul final

Fișier: `docs/Sprint1_DAM_final.docx`

### 2.1 Structura documentului

1. **Tema** – insula de cosmetice VIVIAS din Mall Moldova; evidența numerarului (registru zilnic, raport Z, plăți mici, depuneri, avansuri) și a plăților prin bancă.
2. **Echipa** – PO proxy Cojocaru Paula-Cristiana, Tech Lead Fărcal Mihai-Ștefan, Developer Iacob Dragoș, QA/DevOps Tomșa Cosmin-Mihai. *De completat: link-ul de task management (Trello/Jira).*
3. **Backlog** – 4 epics (E1 Casa zilnică, E2 Avansuri, E3 Plăți prin bancă, E4 Rapoarte și securitate) și 5 user stories cu Gherkin:

| User story | Ce face | Agregate implicate |
|---|---|---|
| US1 Importul raportului Z | numerarul din raportul Z intră în registru; cardul nu; un singur raport pe zi | RegistruCasa, OperatiuneCasa, RaportZ |
| US2 Plata unui furnizor în numerar | plată din casă, plafon 5.000 lei/zi pentru același furnizor | RegistruCasa, OperatiuneCasa, FacturaFurnizor |
| US3 Avansul spre decontare | avans din casă, decontare cu bonuri, un singur avans deschis | AvansSpreDecontare, RegistruCasa |
| US4 Plata unei facturi prin bancă | contabilul propune, doar administratorul aprobă sau respinge | Furnizor, FacturaFurnizor, Plata |
| US5 Închiderea zilei | sold scriptic, diferență, notă explicativă, regularizare, propunere de depunere | RegistruCasa, OperatiuneCasa, RaportZ |

4. **Diagrama cazurilor de utilizare** (Figura 1)
5. **ADR 001** – Java + Spring Boot + PostgreSQL + React; sumele se reprezintă prin value object-ul `Suma` (BigDecimal + monedă), niciodată prin `double`.
6. **C4 Context Diagram** (Figura 2)
7. **Modelul de domeniu** (Figura 3) – agregatele:

| Agregat | Componente | Value objects |
|---|---|---|
| **RegistruCasa** | OperatiuneCasa (1–N) | Suma, RaportZ |
| AvansSpreDecontare | – | Suma |
| Furnizor | – | CUI, IBAN |
| FacturaFurnizor | – | Suma |
| Plata | – | Suma |

   Între agregate există doar referințe prin id: `OperatiuneCasa → idFactura, idAvans`, `FacturaFurnizor → idFurnizor`, `Plata → idFactura`.

8. **Business rules** – coduri folosite și în cod:
   - **V1–V3**: validare câmpuri (sume > 0, IBAN/CUI valide, scadență ≥ emitere)
   - **A1–A7**: validare agregat (operațiuni doar în registru DESCHIS, un registru pe zi, închidere doar cu raport Z, plafon 5.000 lei, un avans deschis, plată = factură, doar administratorul aprobă)
   - **C1–C5**: calcul (sold scriptic, diferență, propunere de depunere, rest avans, view-ul sumar)
   - **W1–W2**: workflow (stările registrului și ale plății)
9. **Diagrama SQL** (Figura 4)

### 2.2 Ce s-a întâmplat cu documentul pe parcurs

- Varianta inițială (`Sprint 1 dam (1).docx`) avea o altă firmă și alt domeniu decât proiectul de APOO (clienți cu facturi, modul de tasking, avans de 48 h).
- Am făcut o variantă corectată (`docs/sprint1/Sprint1_DAM_corectat.docx`), dar și aceea avea agregate separate pentru încasări și plăți, ceea ce contrazice APOO (Figura 6.b).
- **Varianta finală, `Sprint1_DAM_final.docx`, este cea valabilă.** Ea urmează APOO: `RegistruCasa` conține operațiunile de casă. Folderul `docs/sprint1/` poate fi ignorat sau șters.

---

## 3. Sprint 2 – codul

Proiect: `vivias.sprint2.spring_domain` (Spring 5.3, JPA 2.2 doar ca adnotări, Bean Validation, JUnit 5, Java 11). Nu are strat web.

### 3.1 Modelul de domeniu – `org.vivias.domain.casierie`

| Clasă | Rol DDD | Ce ține minte |
|---|---|---|
| `RegistruCasa` | **aggregate root** | `idRegistru: Long`, `data` (unică), `soldDeschidere`, `operatii`, `raportZ`, `soldFaptic`, `notaExplicativa`, `aprobatDe: Long`, `stare`; `soldScriptic` e calculat (`@Transient`) |
| `OperatiuneCasa` | component entity | `operatiuneId` (local), `tip`, `suma`, `document`, `idFactura: Long [0..1]`, `idAvans: Long [0..1]` |
| `Suma` | value object (`@Embeddable`) | `valoare: BigDecimal` + `Moneda` (RON/EUR/USD); `aduna`, `scade` |
| `RaportZ` | value object (`@Embeddable`) | `numar`, `vanzariCard`, `vanzariNumerar` |
| `PlafoaneNumerar` | value object | plafonul intern de casă (2.000 lei); `sumaDeDepus(sold)` |
| `TipOperatiune` | enum | INCASARE_NUMERAR (+), PLATA_FURNIZOR (−), DEPUNERE (−), AVANS (−), RESTITUIRE (+) |
| `StareRegistru` | enum | DESCHIS, IN_VERIFICARE, CU_DIFERENTE, REGULARIZAT, INCHIS |
| `TipDiferenta` | enum | PLUS, MINUS, ZERO |
| `ZiCasaInchisaEvent` | domain event | publicat la închiderea zilei (vânzări, sold, propunere de depunere) |
| `RegistruCasaSumarView`, `RegistruCasaAuditView` | entity views | sumarul zilei și intrările de audit |

Metodele rădăcinii urmează pașii din US5 / CU5:

```
ataseazaRaportZ(raport)                 -> US1: numerarul devine operatiune INCASARE_NUMERAR
inregistreazaOperatiune(tip, suma, ...) -> US2: plati, depuneri, avansuri, restituiri
incepeInchiderea()                      -> fara raport Z: "Lipsește raportul Z"
introduceSoldFaptic(suma)               -> diferenta != 0 => CU_DIFERENTE
justificaDiferenta(nota)
aprobaRegularizare(idAdministrator)     -> REGULARIZAT
inchide(plafoane)                       -> INCHIS + ZiCasaInchisaEvent
```

Ciclul de viață (regula W1):

```
DESCHIS -> IN_VERIFICARE -> INCHIS                       (diferenta zero)
                         -> CU_DIFERENTE -> REGULARIZAT -> INCHIS
```

### 3.2 Serviciile – unde stă fiecare regulă

| Categorie (cerința profesorului) | Clasă | Reguli din Sprint 1 |
|---|---|---|
| Validare (JSR-380 + agregat) | `ValidatingRegistruCasaDomainServiceImpl` | V1, A1, A3 (raport Z, sold faptic, diferență justificată, sold ≥ 0) |
| Calcul pe entitate | `CalculatingRegistruCasaDomainServiceImpl` | C1 sold scriptic, C2 diferență, C3 propunere de depunere |
| Calcul pentru view | `ConsolidatingRegistruCasaSumarViewBusinessServiceImpl` | C5 `RegistruCasaSumarView` |
| Workflow – facade | `PlanningRegistruCasaBusinessWorkflowServiceImpl` | A2 (un registru pe zi, soldul de deschidere = soldul faptic de ieri), W1 |
| Workflow – pe evenimente | `PlanningRegistruCasaEventWorkflowServiceImpl` (+ `WorkflowAction`, `WorkflowCallEvent`, `WorkflowResponseEvent`) | același workflow, apelat prin Spring Events |
| Audit (Spring AOP) | `AuditingRegistruCasaApplicationServiceImpl` + `AuditingRegistruCasaBusinessWorkflowServiceImpl` | jurnalul operațiunilor din workflow |
| Repository / Factory | `RegistruCasaEntityRepositoryBase`, `RegistruCasaEntityFactoryBase` | stocare în memorie + date seed |
| Configurare | `org.vivias.spring.AppScanConfig` | component scan, proxy AOP, `Validator`, plafonul de 2.000 lei |

### 3.3 Testele (22, toate trec)

| Clasă | Ce verifică | Legătura cu Sprint 1 |
|---|---|---|
| `Test1_SimpleDomainService` | serviciul simplu de domeniu (numărul de operațiuni) | – |
| `Test2_ComputingDomainServices` | sold 1000 + 500 − 300 = 1200; faptic 1190 ⇒ MINUS 10; cardul nu intră în casă; propunere de depunere; monede diferite refuzate | US1, US5, C1–C3 |
| `Test3_BusinessWorkflowService` | închidere fără diferențe; diferență fără notă; sold peste plafon; lipsa raportului Z; registru închis; un registru pe zi; operațiuni refuzate | US5 (toate scenariile), A1–A3, W1 |
| `Test4_ValidatingDomainService` | JSR-380, value object invalid, reguli de agregat, validare pe evenimente | V1, A3 |
| `Test5_AuditedWorkflowService` | fluxul integrat cu audit AOP | – |
| `Test6_EventWorkflowService` | workflow-ul apelat prin evenimente, cap-coadă | US5 |
| `AllTests` | suita pentru rulare din IDE | – |

### 3.4 Cum se rulează

Din IntelliJ: click dreapta pe `AllTests` → Run. Sau, cu Maven instalat:

```bash
cd vivias.sprint2.spring_domain
mvn test
```

---

## 4. Ce s-a schimbat pe branch-ul `draft`

Commit `a8210c9` – „Aliniere Sprint 2 (RegistruCasa) la documentul Sprint 1 final” (doar local, fără push). Acesta:

- **include** și ce aliniase o altă sesiune la APOO: `OperatiuneCasa`, `RaportZ`, `PlafoaneNumerar`, `ZiCasaInchisaEvent`, workflow-ul pe evenimente, `Test6`, `AllTests`;
- **aliniază codul la diagrama din Sprint 1**:
  - `registruId: Integer` → `idRegistru: Long` (entitate, view-uri, eveniment, servicii, teste);
  - `aprobatDe` și `aprobaRegularizare(idAdministrator)`: `String` → `Long`;
  - `OperatiuneCasa.idFactura`: `String` → `Long` și câmpul nou `idAvans: Long`;
  - enumerările se salvează ca text: `@Enumerated(EnumType.STRING)`;
  - datele seed folosesc id-ul facturii `98L` în loc de `"F-098"`.

---

## 5. Ce rămâne de făcut

- **De șters:** `Incasare.java` și `Plata.java` din `domain/casierie` sunt clase vechi, nefolosite, care au rămas în afara commit-ului:
  ```bash
  git rm -f vivias.sprint2.spring_domain/src/main/java/org/vivias/domain/casierie/Incasare.java vivias.sprint2.spring_domain/src/main/java/org/vivias/domain/casierie/Plata.java
  ```
- **În documentul Sprint 1:** de completat link-ul de task management.
- **În afara Sprintului 2** (un singur agregat, conform modelului profesorului): `AvansSpreDecontare`, `Furnizor`, `FacturaFurnizor`, `Plata` prin bancă (US3, US4) și plafonul de 5.000 lei/furnizor (regula A4).
- **Diferențe mici păstrate intenționat:** diagrama arată `inchide(plafonIntern: Suma)`, codul primește `PlafoaneNumerar`, care conține același plafon, dar configurabil.
- **Git:** branch-ul `draft` nu este încă trimis pe GitHub (`git push -u origin draft`).

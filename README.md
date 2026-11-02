# DAM - VIVIAS INVEST S.R.L.

Proiect pentru disciplina **DAM** (Domain-driven Application Modeling), realizat
pentru firma fictivă **VIVIAS INVEST S.R.L.** Domeniul provine din proiectul de
Analiză și Proiectare Orientată Obiect al echipei (Capitolul 1: contextele
*Prognoza trezoreriei*, *Casierie și încasări*, *Plăți și relația cu banca*).

## Structura repository-ului

| Cale | Conținut |
|---|---|
| `docs/Sprint1_DAM_final.docx` | Sprint 1 - Business Domain Design (modelul de domeniu, regulile V/A/C/W) |
| `docs/GHID_Sprint1_Sprint2.docx` / `.md` | Ghid despre cum se leagă Sprint 1 de implementarea din Sprint 2 |
| `vivias.sprint2.spring_domain/` | Sprint 2 - implementarea modelului de domeniu (Java, Spring) |

## Sprint 2 pe scurt

Implementează agregatul **`RegistruCasa`** (registrul de casă zilnic, cu
`OperatiuneCasa` ca entitate componentă) din subdomeniul *Casierie și încasări*,
urmând structura proiectului-model `msd.t2_c4.spring_domain`:

- **Entități și Value Objects**: `RegistruCasa`, `OperatiuneCasa`, `Suma`, `RaportZ`,
  `PlafoaneNumerar`; eveniment de domeniu `ZiCasaInchisaEvent`; entity view-uri
  `RegistruCasaSumarView` și `RegistruCasaAuditView`.
- **Reguli de business**: validare (JSR-380 + invarianți de agregat), calcul
  (sold scriptic, diferență, propunere de depunere) și workflow (facade și pe
  evenimente), plus auditare prin Spring AOP.
- **Ciclul de viață al registrului**:

```
DESCHIS -> IN_VERIFICARE -> INCHIS
                        -> CU_DIFERENTE -> REGULARIZAT -> INCHIS
```

Detalii complete, corelația cu documentul APOO și deciziile de adaptare:
[vivias.sprint2.spring_domain/README.md](vivias.sprint2.spring_domain/README.md).

## Tehnologii

Java 11, Spring 5.3 (fără strat web), JPA 2.2 (adnotări), Bean Validation,
Spring Events, Spring AOP, JUnit 5.

## Rulare

Din folderul `vivias.sprint2.spring_domain`:

```bash
mvn test
```

Proiectul nu are interfață grafică: rezultatul se vede în cele 22 de teste
JUnit (`Test1`-`Test6`, suita `AllTests`).

## Echipa

- Tomsa Cosmin Mihai
- Mihai-Stefan Farcal

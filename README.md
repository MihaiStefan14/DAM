# DAM - VIVIAS INVEST S.R.L.

Proiect pentru disciplina **DAM** (Domain-driven Application Modeling), realizat
pentru firma fictivă **VIVIAS INVEST S.R.L.** Domeniul provine din proiectul de
Analiză și Proiectare Orientată Obiect al echipei (Capitolul 1: contextele
*Prognoza trezoreriei*, *Casierie și încasări*, *Plăți și relația cu banca*).

## Structura repository-ului

| Cale | Conținut |
|---|---|
| `docs/Sprint1_DAM_final.docx` | Sprint 1 - Business Domain Design (modelul de domeniu, regulile V/A/C/W) |
| `docs/DOCUMENTATIE_Sprint2.docx` | Documentația Sprint 2 |
| `docs/DOCUMENTATIE_Sprint3.docx` | Documentația Sprint 3 |
| `vivias.dam/` | Proiectul Java (Spring Boot): modelul de domeniu din Sprint 2, continuat cu persistența din Sprint 3 |

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

## Sprint 3 pe scurt

Continuă același proiect cu persistența, urmând proiectul-model
`msd.t2_c5.springboot_data`:

- **Toate agregatele din APOO** (Figurile 6.a - 6.c), pe cele 3 contexte (Casierie și încasări,
  Prognoza trezoreriei, Plăți și relația cu banca), cu regulile lor: 20 de tabele.
- **Model de persistență JPA**: ID-uri generate, `@OneToMany` cu `cascade = ALL`,
  `fetch = EAGER`, `orphanRemoval = true`, `@Embedded`, `@Enumerated`, `@Convert`,
  audit prin `@EntityListeners`.
- **Repository-uri**: câte un Spring Data `JpaRepository` pe agregat, cu operații după nume și `@Query`,
  plus (bonus) un repository complet cu `EntityManager`.
- **Serviciile din Sprint 2** citesc și salvează registrele în baza de date, iar jurnalul de
  audit e și el salvat.
- **Baze de date**: PostgreSQL pentru aplicație, H2 în memorie pentru teste.

Detalii complete, corelația cu documentul APOO și deciziile de adaptare:
[vivias.dam/README.md](vivias.dam/README.md).

## Tehnologii

Java 25, Spring Boot 4.1.1, Spring Data JPA (Hibernate), Bean Validation,
Spring Events, Spring AOP, PostgreSQL, H2 (teste), JUnit 5.

## Rulare

Din folderul `vivias.dam`:

```bash
mvn test
```

Proiectul nu are interfață grafică: rezultatul se vede în testele JUnit
(33 de teste, toate în `vivias.dam/src/test/java/org/vivias/tests`; suita `AllTests` pentru rulare din IDE).

## Echipa

- Tomsa Cosmin Mihai
- Mihai-Stefan Farcal
- Paula-Cristiana Cojocaru
- Dragos Iacob

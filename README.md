# Web Security Lab

Oktatási célú, szándékosan sérülékeny Spring Boot webalkalmazás. Minden modul két módban futtatható — **Vulnerable** és **Secure** — így szemmel látható a különbség a védelem nélküli és a megfelelően védett kód között.

> ⚠️ **FIGYELEM:** Ez az alkalmazás **szándékosan sérülékeny**. Soha ne telepítsd publikus szerverre, csak lokális tanulási környezetben futtasd.

---

## Mit demonstrál?

### SQL Injection modul
- **Basic SQLi** — klasszikus string-konkatenációs injekció (`' OR '1'='1`)
- **Blind SQLi (content-based)** — a válasz *tartalma* eltér találat esetén
- **Blind SQLi (time-based)** — a válasz *ideje* jelzi a feltétel igazságát (szimuláció H2-n)

### XSS modul
- **Stored XSS** — payload tárolódik a DB-ben, majd visszatöltve lefut
- **Reflected XSS** — a bemenet visszakerül a válaszba és ott renderelődik
- **DOM XSS** — tisztán kliensoldalon, `location.hash` → `innerHTML` sinkben

---

## Tech stack

- Java 17+
- Spring Boot 3.x (Web, Data JPA)
- H2 in-memory adatbázis
- Vanilla JavaScript + CSS (nincs frontend framework)
- Maven (wrapper mellékelve)

---

## Indítás

### 1) Klónozás

```bash
git clone https://github.com/<user>/web-security-lab.git
cd web-security-lab
```

### 2) Futtatás

```bash
./mvnw spring-boot:run
```

### 3) Megnyitás böngészőben

```
http://localhost:8080
```

## Modulok használata

### SQL Injection

| Mód | Példa bemenet | Eredmény |
|-----|---------------|----------|
| Vulnerable | `admin` | csak admin user jön vissza |
| Vulnerable | `' OR '1'='1` | **mind a 3 user visszajön** — SQLi lefutott |
| Secure | `' OR '1'='1` | nincs találat — paraméterezett query literálként kezeli |


*Készítette: Csaba Rescsik*

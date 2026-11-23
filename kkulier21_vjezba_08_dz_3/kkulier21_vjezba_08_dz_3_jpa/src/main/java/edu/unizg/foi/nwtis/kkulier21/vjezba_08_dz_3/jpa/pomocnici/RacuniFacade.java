package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;


@Stateless
public class RacuniFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = 1L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public Racun find(int id) {
    return getEntityManager().find(Racun.class, id);
  }

  public List<Racun> dajRacuneKorisnika(String korisnik) {
    CriteriaQuery<Racun> cq = cb.createQuery(Racun.class);
    Root<Racun> racun = cq.from(Racun.class);

    cq.select(racun);
    cq.where(cb.equal(racun.get("korisnici").get("korisnik"), korisnik));
    cq.orderBy(cb.desc(racun.get("vrijeme")));

    return getEntityManager().createQuery(cq).getResultList();
  }

  public boolean postojiOtvoreniRacun(String korisnik) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<Racun> racun = cq.from(Racun.class);

    cq.select(cb.count(racun));
    cq.where(cb.and(
        cb.equal(racun.get("korisnici").get("korisnik"), korisnik),
        cb.equal(racun.get("iznos"), 0.0)));

    Long broj = getEntityManager().createQuery(cq).getSingleResult();
    return broj != null && broj > 0;
  }

  public Racun otvoriRacun(String korisnik) {
    Korisnici korisnikEntitet = getEntityManager().find(Korisnici.class, korisnik);

    if (korisnikEntitet == null) {
      return null;
    }

    Racun racun = new Racun();
    racun.setKorisnici(korisnikEntitet);
    racun.setBrojstavki(0);
    racun.setIznos(0.0);
    racun.setVrijeme(Timestamp.from(Instant.now()));

    getEntityManager().persist(racun);
    getEntityManager().flush();

    return racun;
  }

  public int dajStvarniBrojStavki(int racunId) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<Racunstavka> stavka = cq.from(Racunstavka.class);

    cq.select(cb.count(stavka));
    cq.where(cb.equal(stavka.get("racun").get("id"), racunId));

    Long broj = getEntityManager().createQuery(cq).getSingleResult();
    return broj == null ? 0 : broj.intValue();
  }

  public void uskladiBrojStavki(int racunId) {
    Racun racun = find(racunId);

    if (racun == null || racun.getIznos() != 0.0) {
      return;
    }

    int stvarniBrojStavki = dajStvarniBrojStavki(racunId);
    racun.setBrojstavki(stvarniBrojStavki);

    getEntityManager().merge(racun);
  }
  public Racun zatvoriRacun(int racunId) {
    Racun racun = find(racunId);

    if (racun == null || racun.getIznos() != 0.0) {
      return null;
    }

    int stvarniBrojStavki = dajStvarniBrojStavki(racunId);

    if (stvarniBrojStavki == 0) {
      return null;
    }

    if (racun.getBrojstavki() != stvarniBrojStavki) {
      return null;
    }

    CriteriaQuery<Racunstavka> cq = cb.createQuery(Racunstavka.class);
    Root<Racunstavka> stavka = cq.from(Racunstavka.class);

    cq.select(stavka);
    cq.where(cb.equal(stavka.get("racun").get("id"), racunId));

    List<Racunstavka> stavke = getEntityManager().createQuery(cq).getResultList();

    double ukupno = 0.0;

    for (Racunstavka s : stavke) {
      ukupno += s.getIznos();
    }

    if (ukupno <= 0.0) {
      return null;
    }

    racun.setIznos(ukupno);
    racun.setBrojstavki(stvarniBrojStavki);

    Racun spremljeniRacun = getEntityManager().merge(racun);
    getEntityManager().flush();

    return spremljeniRacun;
  }
  
  public Racun dajOtvoreniRacun(String korisnik) {
    CriteriaQuery<Racun> cq = cb.createQuery(Racun.class);
    Root<Racun> racun = cq.from(Racun.class);

    cq.select(racun);
    cq.where(cb.and(
        cb.equal(racun.get("korisnici").get("korisnik"), korisnik),
        cb.equal(racun.get("iznos"), 0.0)));

    List<Racun> racuni = getEntityManager().createQuery(cq).getResultList();

    if (racuni.isEmpty()) {
      return null;
    }

    return racuni.getFirst();
  }

  public Racunstavka dodajStavkuUOtvoreniRacun(String korisnik, String rezervacija,
      String putnikIme, String putnikPrezime, double iznos) {

    Racun racun = dajOtvoreniRacun(korisnik);

    if (racun == null) {
      return null;
    }

    Racunstavka stavka = new Racunstavka();
    stavka.setRacun(racun);
    stavka.setRezervacija(rezervacija);
    stavka.setPutnikime(putnikIme);
    stavka.setPutnikiprezime(putnikPrezime);
    stavka.setIznos(iznos);

    getEntityManager().persist(stavka);

    racun.setBrojstavki(racun.getBrojstavki() + 1);
    getEntityManager().merge(racun);
    getEntityManager().flush();

    return stavka;
  }
  public List<Racun> dajZatvoreneRacune(String ime, String prezime) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Racun> cq = cb.createQuery(Racun.class);

    Root<Racun> racun = cq.from(Racun.class);

    Join<Racun, Korisnici> korisnik = racun.join("korisnici", JoinType.LEFT);
    Join<Racun, Racunstavka> stavka = racun.join("racunstavkas", JoinType.LEFT);

    List<Predicate> uvjeti = new ArrayList<>();

    uvjeti.add(cb.greaterThan(racun.get("iznos"), 0.0));

    if (ime != null && !ime.isBlank()) {
      String trazenoIme = "%" + ime.trim().toLowerCase() + "%";

      uvjeti.add(cb.or(
          cb.like(cb.lower(korisnik.get("ime")), trazenoIme),
          cb.like(cb.lower(stavka.get("putnikime")), trazenoIme)));
    }

    if (prezime != null && !prezime.isBlank()) {
      String trazenoPrezime = "%" + prezime.trim().toLowerCase() + "%";

      uvjeti.add(cb.or(
          cb.like(cb.lower(korisnik.get("prezime")), trazenoPrezime),
          cb.like(cb.lower(stavka.get("putnikiprezime")), trazenoPrezime)));
    }

    cq.select(racun)
        .distinct(true)
        .where(uvjeti.toArray(new Predicate[0]))
        .orderBy(cb.desc(racun.get("vrijeme")));

    return getEntityManager().createQuery(cq).getResultList();
  }
}
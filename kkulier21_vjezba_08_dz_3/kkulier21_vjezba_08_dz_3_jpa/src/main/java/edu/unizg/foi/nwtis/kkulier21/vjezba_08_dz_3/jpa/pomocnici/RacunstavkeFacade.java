package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.List;

import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Stateless
public class RacunstavkeFacade extends EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = 1L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public List<Racunstavka> dajStavkeRacuna(int racunId) {
    CriteriaQuery<Racunstavka> cq = cb.createQuery(Racunstavka.class);
    Root<Racunstavka> stavka = cq.from(Racunstavka.class);

    cq.select(stavka);
    cq.where(cb.equal(stavka.get("racun").get("id"), racunId));
    cq.orderBy(cb.asc(stavka.get("id")));

    return getEntityManager().createQuery(cq).getResultList();
  }
}
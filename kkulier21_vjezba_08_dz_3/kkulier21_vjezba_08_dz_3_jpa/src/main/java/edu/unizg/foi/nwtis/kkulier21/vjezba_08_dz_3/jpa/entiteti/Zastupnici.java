package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;

/**
 * The persistent class for the ZASTUPNICI database table.
 * 
 */
@Entity
@NamedQuery(name = "Zastupnici.findAll", query = "SELECT z FROM Zastupnici z")
public class Zastupnici implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String korisnik;

  // bi-directional many-to-one association to Aviotvrtke
  @ManyToOne
  @JoinColumn(name = "TVRTKA")
  private Aviotvrtke aviotvrtke;

  // bi-directional one-to-one association to Korisnici
  @OneToOne
  @JoinColumn(name = "KORISNIK", insertable = false, updatable = false)
  private Korisnici korisnici;

  public Zastupnici() {}

  public String getKorisnik() {
    return this.korisnik;
  }

  public void setKorisnik(String korisnik) {
    this.korisnik = korisnik;
  }

  public Aviotvrtke getAviotvrtke() {
    return this.aviotvrtke;
  }

  public void setAviotvrtke(Aviotvrtke aviotvrtke) {
    this.aviotvrtke = aviotvrtke;
  }

  public Korisnici getKorisnici() {
    return this.korisnici;
  }

  public void setKorisnici(Korisnici korisnici) {
    this.korisnici = korisnici;
  }
}
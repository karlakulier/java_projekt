package edu.unizg.foi.nwtis;

import java.io.Serializable;

public record Korisnik(String korisnik, String lozinka, String prezime, String ime, String email)
    implements Serializable {

  private static final long serialVersionUID = 1L;

  public Korisnik korisnikBezLozinke() {
    return new Korisnik(korisnik, "******", prezime, ime, email);
  }
}
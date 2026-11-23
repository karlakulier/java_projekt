package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms;

import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.Korisnik;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SpremnikJmsPoruka {

  private List<String> listaRedPoruka = new ArrayList<>();
  private List<Korisnik> listaPorukaTeme = new ArrayList<>();

  public void dodajRedPoruka(String poruka) {
    this.listaRedPoruka.add(poruka);
  }

  public List<String> dajRedPoruka() {
    return this.listaRedPoruka;
  }

  public void obrisiRedPoruka() {
    this.listaRedPoruka.clear();
  }

  public void dodajPorukuTeme(Korisnik korisnik) {
    this.listaPorukaTeme.add(korisnik);
  }

  public List<Korisnik> dajPorukeTeme() {
    return this.listaPorukaTeme;
  }

  public void obrisiPorukeTeme() {
    this.listaPorukaTeme.clear();
  }

  public void obrisiPorukuTeme(String korisnik) {
    this.listaPorukaTeme.removeIf(k -> korisnik.equals(k.korisnik()));
  }
}
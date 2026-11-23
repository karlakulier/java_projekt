package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.SessionScoped;

@SessionScoped
public class RadneRezervacije implements Serializable {

  private static final long serialVersionUID = 1L;

  private Integer posluzitelj;
  private String idUlaznice;

  private List<Map<String, String>> rezervacije = new ArrayList<>();

  private Map<String, String> putnik;

  public Integer getPosluzitelj() {
    return posluzitelj;
  }

  public void setPosluzitelj(Integer posluzitelj) {
    this.posluzitelj = posluzitelj;
  }

  public boolean jeOdabranPosluzitelj() {
    return this.posluzitelj != null;
  }

  public void obrisiOdabirPosluzitelja() {
    this.posluzitelj = null;
  }

  public List<Map<String, String>> getRezervacije() {
    return rezervacije;
  }

  public void setRezervacije(List<Map<String, String>> rezervacije) {
    if (rezervacije == null) {
      this.rezervacije = new ArrayList<>();
    } else {
      this.rezervacije = rezervacije;
    }
  }

  public void obrisiRezervacije() {
    this.rezervacije.clear();
  }

  public Map<String, String> dajRezervaciju(String idRezervacijeSjedala) {
    if (this.rezervacije == null || idRezervacijeSjedala == null) {
      return null;
    }

    for (var rezervacija : this.rezervacije) {
      var id = rezervacija.get("idRezervacijeSjedala");

      if (id != null && id.equals(idRezervacijeSjedala)) {
        return rezervacija;
      }
    }

    return null;
  }

  public boolean postojiRezervacijaZaLet(String idLeta) {
    if (this.rezervacije == null || idLeta == null) {
      return false;
    }

    for (var rezervacija : this.rezervacije) {
      var postojeciIdLeta = rezervacija.get("idLeta");

      if (postojeciIdLeta != null && postojeciIdLeta.equals(idLeta)) {
        return true;
      }
    }

    return false;
  }

  public boolean dodajPrivremenuRezervaciju(Map<String, String> rezervacija) {
    if (rezervacija == null) {
      return false;
    }

    var idLeta = rezervacija.get("idLeta");

    if (idLeta == null || idLeta.isBlank()) {
      return false;
    }

    if (postojiRezervacijaZaLet(idLeta)) {
      return false;
    }

    this.rezervacije.add(rezervacija);
    return true;
  }

  public void obrisiPrivremenuRezervaciju(String idLeta) {
    if (idLeta == null || this.rezervacije == null) {
      return;
    }

    this.rezervacije.removeIf(r -> idLeta.equals(r.get("idLeta")));
  }

  public void obrisiPrivremenuRezervacijuPoId(String idRezervacijeSjedala) {
    if (idRezervacijeSjedala == null || this.rezervacije == null) {
      return;
    }

    this.rezervacije.removeIf(r -> idRezervacijeSjedala.equals(r.get("idRezervacijeSjedala")));
  }

  public Map<String, String> getPutnik() {
    return putnik;
  }

  public void setPutnik(Map<String, String> putnik) {
    this.putnik = putnik;
  }

  public void obrisiPutnika() {
    this.putnik = null;
  }

  public String getIdUlaznice() {
    return idUlaznice;
  }

  public void setIdUlaznice(String idUlaznice) {
    this.idUlaznice = idUlaznice;
  }

  public boolean imaUlaznicu() {
    return this.idUlaznice != null && !this.idUlaznice.isBlank();
  }

  public void obrisiUlaznicu() {
    this.idUlaznice = null;
  }
}
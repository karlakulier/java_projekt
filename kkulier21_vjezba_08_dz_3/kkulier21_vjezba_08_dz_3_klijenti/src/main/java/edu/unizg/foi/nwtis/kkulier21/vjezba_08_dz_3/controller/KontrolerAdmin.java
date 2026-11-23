package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms.SpremnikJmsPoruka;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.KontrolaRestKlijent;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.RezervacijeRestKlijent;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.RacuniFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.RacunstavkeFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import jakarta.ws.rs.QueryParam;

@Controller
@Path("admin")
@RequestScoped
@RolesAllowed({"admin"})
public class KontrolerAdmin {

  @Inject
  private Models model;

  @Inject
  @RestClient
  private KontrolaRestKlijent kontrolaRestKlijent;

  @Inject
  @RestClient
  private RezervacijeRestKlijent rezervacijeRestKlijent;
  
  @Inject
  private SpremnikJmsPoruka spremnikJmsPoruka;
  @Inject
  private RacuniFacade racuniFacade;

  @Inject
  private RacunstavkeFacade racunstavkeFacade;

  @GET
  @Path("status")
  @View("admin/status.jsp")
  public void status() {
    pripremiStatus(null, false);
  }

  @GET
  @Path("kontrola/kraj")
  @View("admin/status.jsp")
  public void krajKontrolaAdmin() {
    try (Response odgovor = this.kontrolaRestKlijent.krajAdminDio()) {
      if (jeUspjesanStatus(odgovor.getStatus())) {
        pripremiStatus("Poslan je zahtjev za kraj rada administracijskog dijela poslužitelja Kontrola.",
            false);
      } else {
        pripremiStatus("Kraj rada administracijskog dijela Kontrola nije uspio. Status: "
            + odgovor.getStatus(), true);
      }
    } catch (Exception e) {
      pripremiStatus("Problem kod slanja zahtjeva za kraj rada Kontrola admin dijela: "
          + e.getClass().getName() + " - " + e.getMessage(), true);
      e.printStackTrace();
    }
  }

  @GET
  @Path("rezervacije/{id}/kraj")
  @View("admin/status.jsp")
  public void krajRezervacijeAdmin(@PathParam("id") int id) {
    try (Response odgovor = this.rezervacijeRestKlijent.krajAdminDioRezervacije(id)) {
      if (jeUspjesanStatus(odgovor.getStatus())) {
        pripremiStatus("Poslan je zahtjev za kraj rada administracijskog dijela poslužitelja Rezervacije "
            + id + ".", false);
      } else {
        pripremiStatus("Kraj rada administracijskog dijela Rezervacije " + id
            + " nije uspio. Status: " + odgovor.getStatus(), true);
      }
    } catch (Exception e) {
      pripremiStatus("Problem kod slanja zahtjeva za kraj rada Rezervacije admin dijela "
          + id + ": " + e.getClass().getName() + " - " + e.getMessage(), true);
      e.printStackTrace();
    }
  }

  private void pripremiStatus(String poruka, boolean pogreska) {
    model.put("kontrolaKorisnicki", provjeriKontrolaKorisnicki());
    model.put("kontrolaAdmin", provjeriKontrolaAdmin());
    model.put("rezervacije", provjeriSveRezervacije());

    if (poruka != null) {
      model.put("poruka", poruka);
      model.put("pogreska", pogreska);
    }
  }

  private Map<String, String> provjeriKontrolaKorisnicki() {
    try (Response odgovor = this.kontrolaRestKlijent.pingKorisnickiDio()) {
      return napraviStatusMapu("Kontrola - korisnički dio", odgovor.getStatus());
    } catch (Exception e) {
      return napraviStatusMapu("Kontrola - korisnički dio", -1);
    }
  }

  private Map<String, String> provjeriKontrolaAdmin() {
    try (Response odgovor = this.kontrolaRestKlijent.pingAdminDio()) {
      return napraviStatusMapu("Kontrola - administracijski dio", odgovor.getStatus());
    } catch (Exception e) {
      return napraviStatusMapu("Kontrola - administracijski dio", -1);
    }
  }

  private List<Map<String, String>> provjeriSveRezervacije() {
    List<Map<String, String>> rezultat = new ArrayList<>();

    for (int id : dajIdPosluziteljaRezervacija()) {
      rezultat.add(provjeriRezervacije(id));
    }

    return rezultat;
  }

  private Map<String, String> provjeriRezervacije(int id) {
    Map<String, String> mapa = new LinkedHashMap<>();

    mapa.put("id", Integer.toString(id));

    mapa.put("korisnickiStatus", provjeriStatusRezervacijeKorisnicki(id));
    mapa.put("avioTvrtkeStatus", provjeriStatusRezervacijeAvioTvrtke(id));
    mapa.put("adminStatus", provjeriStatusRezervacijeAdmin(id));

    return mapa;
  }

  private String provjeriStatusRezervacijeKorisnicki(int id) {
    try (Response odgovor = this.rezervacijeRestKlijent.pingKorisnickiDioRezervacije(id)) {
      return tekstStatusa(odgovor.getStatus());
    } catch (Exception e) {
      return "NE RADI";
    }
  }

  private String provjeriStatusRezervacijeAvioTvrtke(int id) {
    try (Response odgovor = this.rezervacijeRestKlijent.pingAvioTvrtkeDioRezervacije(id)) {
      return tekstStatusa(odgovor.getStatus());
    } catch (Exception e) {
      return "NE RADI";
    }
  }

  private String provjeriStatusRezervacijeAdmin(int id) {
    try (Response odgovor = this.rezervacijeRestKlijent.pingAdminDioRezervacije(id)) {
      return tekstStatusa(odgovor.getStatus());
    } catch (Exception e) {
      return "NE RADI";
    }
  }

  private Map<String, String> napraviStatusMapu(String naziv, int status) {
    Map<String, String> mapa = new LinkedHashMap<>();

    mapa.put("naziv", naziv);
    mapa.put("httpStatus", status == -1 ? "-" : Integer.toString(status));
    mapa.put("status", tekstStatusa(status));

    return mapa;
  }

  private String tekstStatusa(int status) {
    if (jeUspjesanStatus(status)) {
      return "RADI";
    }

    return "NE RADI";
  }

  private boolean jeUspjesanStatus(int status) {
    return status >= 200 && status < 300;
  }

  private int[] dajIdPosluziteljaRezervacija() {
    return new int[] {0, 1};
  }
  @GET
  @Path("nadzornaPloca")
  @View("admin/nadzornaPloca.jsp")
  public void nadzornaPloca() {
  }
  @GET
  @Path("korisnici")
  @View("admin/korisnici.jsp")
  public void korisnici() {
    model.put("korisnici", this.spremnikJmsPoruka.dajPorukeTeme());
  }

  @GET
  @Path("korisnici/obrisi/{korisnik}")
  @View("admin/korisnici.jsp")
  public void obrisiKorisnikaIzListe(@PathParam("korisnik") String korisnik) {
    this.spremnikJmsPoruka.obrisiPorukuTeme(korisnik);

    model.put("poruka", "Korisnik " + korisnik + " je obrisan iz liste.");
    model.put("pogreska", false);
    model.put("korisnici", this.spremnikJmsPoruka.dajPorukeTeme());
  }

  @GET
  @Path("korisnici/obrisiSve")
  @View("admin/korisnici.jsp")
  public void obrisiSveKorisnikeIzListe() {
    this.spremnikJmsPoruka.obrisiPorukeTeme();

    model.put("poruka", "Svi korisnici su obrisani iz liste.");
    model.put("pogreska", false);
    model.put("korisnici", this.spremnikJmsPoruka.dajPorukeTeme());
  }
  @GET
  @Path("racuni")
  @View("admin/racuni.jsp")
  public void racuni() {
    model.put("racuni", procitajRacuneIzJmsReda());
  }

  @GET
  @Path("racuni/stavke/{id}")
  @View("admin/stavkeRacuna.jsp")
  public void stavkeRacuna(@PathParam("id") String id) {
    Map<String, String> racun = null;
    List<Map<String, String>> stavke = new ArrayList<>();

    for (String poruka : this.spremnikJmsPoruka.dajRedPoruka()) {
      Map<String, String> r = procitajRacunIzJsona(poruka);

      if (id.equals(r.get("id"))) {
        racun = r;
        stavke = procitajStavkeRacunaIzJsona(poruka);
        break;
      }
    }

    if (racun == null) {
      model.put("poruka", "Račun nije pronađen u listi JMS poruka.");
      model.put("pogreska", true);
      model.put("racun", new LinkedHashMap<String, String>());
      model.put("stavke", new ArrayList<Map<String, String>>());
      return;
    }

    model.put("racun", racun);
    model.put("stavke", stavke);
  }

  @GET
  @Path("racuni/obrisi/{id}")
  @View("admin/racuni.jsp")
  public void obrisiRacunIzListe(@PathParam("id") String id) {
    this.spremnikJmsPoruka.dajRedPoruka().removeIf(poruka -> id.equals(procitajRacunIzJsona(poruka).get("id")));

    model.put("poruka", "Račun " + id + " je obrisan iz liste.");
    model.put("pogreska", false);
    model.put("racuni", procitajRacuneIzJmsReda());
  }

  @GET
  @Path("racuni/obrisiSve")
  @View("admin/racuni.jsp")
  public void obrisiSveRacuneIzListe() {
    this.spremnikJmsPoruka.obrisiRedPoruka();

    model.put("poruka", "Svi računi su obrisani iz liste.");
    model.put("pogreska", false);
    model.put("racuni", procitajRacuneIzJmsReda());
  }
  private List<Map<String, String>> procitajRacuneIzJmsReda() {
    List<Map<String, String>> racuni = new ArrayList<>();

    for (String poruka : this.spremnikJmsPoruka.dajRedPoruka()) {
      Map<String, String> racun = procitajRacunIzJsona(poruka);

      if (!racun.isEmpty()) {
        racuni.add(racun);
      }
    }

    return racuni;
  }

  private Map<String, String> procitajRacunIzJsona(String json) {
    Map<String, String> racun = new LinkedHashMap<>();

    if (json == null || json.isBlank()) {
      return racun;
    }

    try (var reader = Json.createReader(new StringReader(json))) {
      JsonValue vrijednost = reader.readValue();

      if (vrijednost.getValueType() != JsonValue.ValueType.OBJECT) {
        return racun;
      }

      JsonObject objekt = vrijednost.asJsonObject();

      racun.put("id", dohvatiVrijednostJson(objekt, "id"));
      racun.put("korisnik", dohvatiVrijednostJson(objekt, "korisnik"));
      racun.put("brojstavki", dohvatiVrijednostJson(objekt, "brojstavki"));
      racun.put("iznos", dohvatiVrijednostJson(objekt, "iznos"));
      racun.put("vrijeme", dohvatiVrijednostJson(objekt, "vrijeme"));

    } catch (Exception e) {
      e.printStackTrace();
    }

    return racun;
  }

  private List<Map<String, String>> procitajStavkeRacunaIzJsona(String json) {
    List<Map<String, String>> stavke = new ArrayList<>();

    if (json == null || json.isBlank()) {
      return stavke;
    }

    try (var reader = Json.createReader(new StringReader(json))) {
      JsonValue vrijednost = reader.readValue();

      if (vrijednost.getValueType() != JsonValue.ValueType.OBJECT) {
        return stavke;
      }

      JsonObject objekt = vrijednost.asJsonObject();
      JsonArray poljeStavki = objekt.getJsonArray("stavke");

      if (poljeStavki == null) {
        return stavke;
      }

      for (JsonValue v : poljeStavki) {
        if (v.getValueType() != JsonValue.ValueType.OBJECT) {
          continue;
        }

        JsonObject stavkaObjekt = v.asJsonObject();
        Map<String, String> stavka = new LinkedHashMap<>();

        stavka.put("id", dohvatiVrijednostJson(stavkaObjekt, "id"));
        stavka.put("iznos", dohvatiVrijednostJson(stavkaObjekt, "iznos"));
        stavka.put("putnikime", dohvatiVrijednostJson(stavkaObjekt, "putnikime"));
        stavka.put("putnikiprezime", dohvatiVrijednostJson(stavkaObjekt, "putnikiprezime"));
        stavka.put("rezervacija", dohvatiVrijednostJson(stavkaObjekt, "rezervacija"));

        stavke.add(stavka);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return stavke;
  }

  private String dohvatiVrijednostJson(JsonObject objekt, String kljuc) {
    if (objekt == null || kljuc == null || !objekt.containsKey(kljuc) || objekt.isNull(kljuc)) {
      return "";
    }

    try {
      return objekt.getString(kljuc);
    } catch (Exception e) {
      return objekt.get(kljuc).toString().replace("\"", "");
    }
  }
  @GET
  @Path("zatvoreniRacuni")
  @View("admin/zatvoreniRacuni.jsp")
  public void zatvoreniRacuni(@QueryParam("ime") String ime,
      @QueryParam("prezime") String prezime) {

    List<Racun> racuni = this.racuniFacade.dajZatvoreneRacune(ime, prezime);

    model.put("racuni", racuni);
    model.put("ime", ime == null ? "" : ime);
    model.put("prezime", prezime == null ? "" : prezime);
  }

  @GET
  @Path("zatvoreniRacuni/stavke/{id}")
  @View("admin/stavkeZatvorenogRacuna.jsp")
  public void stavkeZatvorenogRacuna(@PathParam("id") int id) {
    Racun racun = this.racuniFacade.find(id);

    if (racun == null) {
      model.put("poruka", "Račun nije pronađen.");
      model.put("pogreska", true);
      model.put("stavke", new ArrayList<Racunstavka>());
      return;
    }

    List<Racunstavka> stavke = this.racunstavkeFacade.dajStavkeRacuna(id);

    model.put("racun", racun);
    model.put("stavke", stavke);
  }
}
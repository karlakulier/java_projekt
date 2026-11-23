package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms.PosiljateljJmsRedPoruka;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.RacuniFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.RacunstavkeFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.model.RadneRezervacije;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Random;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.RezervacijeRestKlijent;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.ws.WebSocketRezervacije;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response.Status;
@Controller
@Path("rezervacije")
@RequestScoped
@RolesAllowed({"nwtis", "admin"})
public class KontrolerRezervacije {

  @Inject
  private Models model;

  @Inject
  private RadneRezervacije radneRezervacije;

  @Inject
  private SecurityContext securityContext;

  @Inject
  private RacuniFacade racuniFacade;

  @Inject
  private RacunstavkeFacade racunstavkeFacade;

  @Inject
  private PosiljateljJmsRedPoruka posiljateljJmsRedPoruka;
  
  @Inject
  @RestClient
  private RezervacijeRestKlijent rezervacijeRestKlijent;

  private final Random generator = new Random();
  
  @Inject
  private KorisniciFacade korisniciFacade;

  @GET
  @Path("pocetak")
  @View("rezervacije/index.jsp")
  public void pocetak() {
    model.put("posluzitelj", this.radneRezervacije.getPosluzitelj());
    model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
  }

  @POST
  @Path("odaberiPosluzitelja")
  @View("rezervacije/index.jsp")
  public void odaberiPosluzitelja(@FormParam("posluzitelj") Integer posluzitelj) {
    if (posluzitelj == null || posluzitelj < 0) {
      model.put("poruka", "Potrebno je odabrati poslužitelj rezervacija.");
      model.put("pogreska", true);
      model.put("odabranPosluzitelj", false);
      return;
    }

    this.radneRezervacije.setPosluzitelj(posluzitelj);

    model.put("poruka", "Odabran je poslužitelj rezervacija: " + posluzitelj);
    model.put("pogreska", false);
    model.put("posluzitelj", this.radneRezervacije.getPosluzitelj());
    model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
  }

  @GET
  @Path("ponistiOdabirPosluzitelja")
  @View("rezervacije/index.jsp")
  public void ponistiOdabirPosluzitelja() {
    this.radneRezervacije.obrisiOdabirPosluzitelja();

    model.put("poruka", "Odabir poslužitelja je poništen.");
    model.put("pogreska", false);
    model.put("odabranPosluzitelj", false);
  }

  @GET
  @Path("nadzornaKonzola")
  @View("rezervacije/nadzornaKonzola.jsp")
  public void nadzornaKonzola() {
    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      model.put("poruka", "Prvo je potrebno odabrati poslužitelj rezervacija.");
      model.put("pogreska", true);
      return;
    }

    model.put("posluzitelj", this.radneRezervacije.getPosluzitelj());
  }

  @GET
  @Path("racuni")
  @View("rezervacije/racuni.jsp")
  public void racuni() {
    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      model.put("poruka", "Prvo je potrebno odabrati poslužitelj rezervacija.");
      model.put("pogreska", true);
      model.put("racuni", new ArrayList<Racun>());
      model.put("stvarniBrojeviStavki", new HashMap<Integer, Integer>());
      model.put("sviRacuniZatvoreni", false);
      return;
    }

    pripremiRacune(null, false);
  }

  @GET
  @Path("otvoriRacun")
  @View("rezervacije/racuni.jsp")
  public void otvoriRacun() {
    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      model.put("poruka", "Prvo je potrebno odabrati poslužitelj rezervacija.");
      model.put("pogreska", true);
      model.put("racuni", new ArrayList<Racun>());
      model.put("stvarniBrojeviStavki", new HashMap<Integer, Integer>());
      model.put("sviRacuniZatvoreni", false);
      return;
    }

    String korisnik = dajPrijavljenogKorisnika();

    if (this.racuniFacade.postojiOtvoreniRacun(korisnik)) {
      pripremiRacune("Nije moguće otvoriti novi račun dok postoji otvoreni račun.", true);
      return;
    }

    Racun racun = this.racuniFacade.otvoriRacun(korisnik);

    if (racun == null) {
      pripremiRacune("Novi račun nije otvoren.", true);
      return;
    }

    pripremiRacune("Otvoren je novi račun: " + racun.getId(), false);
  }

  @GET
  @Path("stavkeRacuna/{id}")
  @View("rezervacije/stavkeRacuna.jsp")
  public void stavkeRacuna(@PathParam("id") int id) {
    Racun racun = this.racuniFacade.find(id);

    if (racun == null || !racunPripadaKorisniku(racun)) {
      model.put("poruka", "Račun nije pronađen.");
      model.put("pogreska", true);
      model.put("stavke", new ArrayList<Racunstavka>());
      return;
    }

    List<Racunstavka> stavke = this.racunstavkeFacade.dajStavkeRacuna(id);
    int stvarniBrojStavki = this.racuniFacade.dajStvarniBrojStavki(id);

    model.put("racun", racun);
    model.put("stavke", stavke);
    model.put("stvarniBrojStavki", stvarniBrojStavki);
    model.put("mozeUskladiti",
        racun.getIznos() == 0.0 && racun.getBrojstavki() != stvarniBrojStavki);
  }

  @GET
  @Path("uskladiStavke/{id}")
  @View("rezervacije/stavkeRacuna.jsp")
  public void uskladiStavke(@PathParam("id") int id) {
    Racun racun = this.racuniFacade.find(id);

    if (racun == null || !racunPripadaKorisniku(racun)) {
      model.put("poruka", "Račun nije pronađen.");
      model.put("pogreska", true);
      model.put("stavke", new ArrayList<Racunstavka>());
      return;
    }

    this.racuniFacade.uskladiBrojStavki(id);

    racun = this.racuniFacade.find(id);
    List<Racunstavka> stavke = this.racunstavkeFacade.dajStavkeRacuna(id);
    int stvarniBrojStavki = this.racuniFacade.dajStvarniBrojStavki(id);

    model.put("poruka", "Broj stavki računa je usklađen.");
    model.put("pogreska", false);
    model.put("racun", racun);
    model.put("stavke", stavke);
    model.put("stvarniBrojStavki", stvarniBrojStavki);
    model.put("mozeUskladiti",
        racun.getIznos() == 0.0 && racun.getBrojstavki() != stvarniBrojStavki);
  }

  @GET
  @Path("zatvoriRacun/{id}")
  @View("rezervacije/racuni.jsp")
  public void zatvoriRacun(@PathParam("id") int id) {
    Racun racun = this.racuniFacade.find(id);

    if (racun == null || !racunPripadaKorisniku(racun)) {
      pripremiRacune("Račun nije pronađen.", true);
      return;
    }

    int stvarniBrojStavki = this.racuniFacade.dajStvarniBrojStavki(id);

    if (racun.getIznos() != 0.0) {
      pripremiRacune("Račun je već zatvoren.", true);
      return;
    }

    if (racun.getBrojstavki() != stvarniBrojStavki) {
      pripremiRacune("Račun se ne može zatvoriti jer broj stavki nije usklađen.", true);
      return;
    }

    Racun zatvoreniRacun = this.racuniFacade.zatvoriRacun(id);

    if (zatvoreniRacun == null) {
      pripremiRacune("Račun nije zatvoren.", true);
      return;
    }

    List<Racunstavka> stavke = this.racunstavkeFacade.dajStavkeRacuna(id);
    String json = napraviJsonRacuna(zatvoreniRacun, stavke);
    boolean jmsPoslan = this.posiljateljJmsRedPoruka.novaPoruka(json);

    pripremiRacune("Račun je zatvoren. JMS poruka "
        + (jmsPoslan ? "je poslana." : "nije poslana."), !jmsPoslan);
  }

  private void pripremiRacune(String poruka, boolean pogreska) {
    String korisnik = dajPrijavljenogKorisnika();

    List<Racun> racuni = this.racuniFacade.dajRacuneKorisnika(korisnik);
    Map<Integer, Integer> stvarniBrojeviStavki = new HashMap<>();

    for (Racun r : racuni) {
      stvarniBrojeviStavki.put(r.getId(), this.racuniFacade.dajStvarniBrojStavki(r.getId()));
    }

    boolean sviRacuniZatvoreni = !this.racuniFacade.postojiOtvoreniRacun(korisnik);

    model.put("racuni", racuni);
    model.put("stvarniBrojeviStavki", stvarniBrojeviStavki);
    model.put("sviRacuniZatvoreni", sviRacuniZatvoreni);

    if (poruka != null) {
      model.put("poruka", poruka);
      model.put("pogreska", pogreska);
    }
  }

  private String dajPrijavljenogKorisnika() {
    return this.securityContext.getCallerPrincipal().getName();
  }

  private boolean racunPripadaKorisniku(Racun racun) {
    if (racun == null || racun.getKorisnici() == null) {
      return false;
    }

    return racun.getKorisnici().getKorisnik().equals(dajPrijavljenogKorisnika());
  }

  private String napraviJsonRacuna(Racun racun, List<Racunstavka> stavke) {
    StringBuilder json = new StringBuilder();

    json.append("{");
    json.append("\"id\":").append(racun.getId()).append(",");
    json.append("\"korisnik\":\"").append(escapeJson(racun.getKorisnici().getKorisnik())).append("\",");
    json.append("\"brojstavki\":").append(racun.getBrojstavki()).append(",");
    json.append("\"iznos\":").append(racun.getIznos()).append(",");
    json.append("\"vrijeme\":\"").append(racun.getVrijeme()).append("\",");
    json.append("\"stavke\":[");

    for (int i = 0; i < stavke.size(); i++) {
      Racunstavka s = stavke.get(i);

      json.append("{");
      json.append("\"id\":").append(s.getId()).append(",");
      json.append("\"iznos\":").append(s.getIznos()).append(",");
      json.append("\"putnikime\":\"").append(escapeJson(s.getPutnikime())).append("\",");
      json.append("\"putnikiprezime\":\"").append(escapeJson(s.getPutnikiprezime())).append("\",");
      json.append("\"rezervacija\":\"").append(escapeJson(s.getRezervacija())).append("\"");
      json.append("}");

      if (i < stavke.size() - 1) {
        json.append(",");
      }
    }

    json.append("]");
    json.append("}");

    return json.toString();
  }

  private String escapeJson(String tekst) {
    if (tekst == null) {
      return "";
    }

    return tekst.replace("\\", "\\\\").replace("\"", "\\\"");
  }
  @GET
  @Path("rezervacijePutnika")
  @View("rezervacije/rezervacijePutnika.jsp")
  public void rezervacijePutnika() {
    model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
    model.put("rezervacije", this.radneRezervacije.getRezervacije());
    model.put("imaOtvorenRacun", imaOtvorenRacun());
  }

  @POST
  @Path("rezervacijePutnika")
  @View("rezervacije/rezervacijePutnika.jsp")
  public void dohvatiRezervacijePutnika(@FormParam("idUlaznice") String idUlaznice,
      @FormParam("odDatuma") String odDatuma, @FormParam("doDatuma") String doDatuma) {

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneRezervacije("Prvo je potrebno odabrati poslužitelj rezervacija.", true);
      return;
    }

    var korisnik = this.securityContext.getCallerPrincipal().getName();
    var korisnikEntitet = this.korisniciFacade.find(korisnik);

    if (korisnikEntitet == null || korisnikEntitet.getEmail() == null) {
      postaviPrazneRezervacije("Nije pronađen email prijavljenog korisnika.", true);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajRezervacijePutnika(
        this.radneRezervacije.getPosluzitelj(), idUlaznice, korisnikEntitet.getEmail(),
        odDatuma, doDatuma)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        String tijelo = "";

        try {
          tijelo = odgovor.readEntity(String.class);
        } catch (Exception e) {
          tijelo = "";
        }

        if (odgovor.getStatus() == Status.NOT_FOUND.getStatusCode()) {
          postaviPrazneRezervacije("Nema pronađenih rezervacija za unesenu ulaznicu.", false);
        } else {
          postaviPrazneRezervacije("Rezervacije nisu dohvaćene. Status: " + odgovor.getStatus()
              + " Odgovor: " + tijelo, true);
        }

        return;
      }

      var json = odgovor.readEntity(String.class);
      var rezervacije = procitajRezervacije(json);

      this.radneRezervacije.setRezervacije(rezervacije);

      model.put("rezervacije", rezervacije);
      model.put("idUlaznice", idUlaznice);
      model.put("odDatuma", odDatuma);
      model.put("doDatuma", doDatuma);
      model.put("imaOtvorenRacun", imaOtvorenRacun());
      model.put("odabranPosluzitelj", true);
    } catch (Exception e) {
      postaviPrazneRezervacije("Problem kod dohvaćanja rezervacija.", true);
    }
  }

  @GET
  @Path("potvrdiRezervaciju/{idRezervacijeSjedala}")
  @View("rezervacije/rezervacijePutnika.jsp")
  public void potvrdiRezervaciju(@PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @QueryParam("idUlaznice") String idUlaznice) {

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneRezervacije("Prvo je potrebno odabrati poslužitelj rezervacija.", true);
      return;
    }

    if (!imaOtvorenRacun()) {
      postaviRezervacijeSPorukom("Ne postoji otvoreni račun korisnika.", true);
      return;
    }

    var korisnik = this.securityContext.getCallerPrincipal().getName();
    var korisnikEntitet = this.korisniciFacade.find(korisnik);

    if (korisnikEntitet == null || korisnikEntitet.getEmail() == null) {
      postaviRezervacijeSPorukom("Nije pronađen email prijavljenog korisnika.", true);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.potvrdiRezervaciju(
        this.radneRezervacije.getPosluzitelj(),
        idRezervacijeSjedala,
        idUlaznice,
        korisnikEntitet.getEmail(),
        "")) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        postaviRezervacijeSPorukom("Rezervacija nije potvrđena. Status: " + odgovor.getStatus(),
            true);
        return;
      }

      dodajStavkuIPosaljiWebSocket(idRezervacijeSjedala, korisnik, korisnikEntitet);
      postaviRezervacijeSPorukom("Rezervacija je potvrđena i dodana na otvoreni račun.", false);
    } catch (Exception e) {
      postaviRezervacijeSPorukom("Problem kod potvrđivanja rezervacije.", true);
    }
  }
  private boolean imaOtvorenRacun() {
    var korisnik = this.securityContext.getCallerPrincipal().getName();
    return this.racuniFacade.postojiOtvoreniRacun(korisnik);
  }

  private void postaviPrazneRezervacije(String poruka, boolean pogreska) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("rezervacije", new ArrayList<Map<String, String>>());
    model.put("imaOtvorenRacun", imaOtvorenRacun());
    model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
  }

  private void postaviRezervacijeSPorukom(String poruka, boolean pogreska) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("rezervacije", this.radneRezervacije.getRezervacije());
    model.put("imaOtvorenRacun", imaOtvorenRacun());
    model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
  }

  private List<Map<String, String>> procitajRezervacije(String json) {
    List<Map<String, String>> rezultat = new ArrayList<>();

    try (var reader = Json.createReader(new StringReader(json))) {
      JsonArray polje = reader.readArray();

      for (var vrijednost : polje) {
        JsonObject objekt = vrijednost.asJsonObject();
        Map<String, String> mapa = new LinkedHashMap<>();

        for (String kljuc : objekt.keySet()) {
          mapa.put(kljuc, objekt.get(kljuc).toString().replace("\"", ""));
        }

        normalizirajRezervaciju(mapa);
        rezultat.add(mapa);
      }
    }

    return rezultat;
  }

  private void normalizirajRezervaciju(Map<String, String> rezervacija) {
    postaviAkoNedostaje(rezervacija, "idRezervacijeSjedala", "id", "rezervacija",
        "idRezervacije");
    postaviAkoNedostaje(rezervacija, "idLeta", "let", "flightId");
    postaviAkoNedostaje(rezervacija, "oznakaLeta", "oznaka", "brojLeta");
    postaviAkoNedostaje(rezervacija, "polazniAerodrom", "polaziste", "polazni");
    postaviAkoNedostaje(rezervacija, "odredisniAerodrom", "odrediste", "odredisni");
    postaviAkoNedostaje(rezervacija, "vrijemePolijetanja", "vrijeme", "polijetanje");
  }

  private void postaviAkoNedostaje(Map<String, String> mapa, String cilj, String... moguci) {
    if (mapa.get(cilj) != null) {
      return;
    }

    for (String kljuc : moguci) {
      if (mapa.get(kljuc) != null) {
        mapa.put(cilj, mapa.get(kljuc));
        return;
      }
    }

    mapa.put(cilj, "");
  }
  private void dodajStavkuIPosaljiWebSocket(String idRezervacijeSjedala, String korisnik,
      Korisnici korisnikEntitet) {

    var rezervacija = this.radneRezervacije.dajRezervaciju(idRezervacijeSjedala);
    var ime = korisnikEntitet.getIme();
    var prezime = korisnikEntitet.getPrezime();
    var iznos = generirajIznos();

    this.racuniFacade.dodajStavkuUOtvoreniRacun(korisnik, idRezervacijeSjedala, ime, prezime,
        iznos);

    var oznakaLeta = vrijednost(rezervacija, "oznakaLeta");
    var idLeta = vrijednost(rezervacija, "idLeta");

    var poruka = idRezervacijeSjedala + "," + oznakaLeta + "," + idLeta + "," + ime + ","
        + prezime;

    WebSocketRezervacije.send(poruka);
  }

  private double generirajIznos() {
    double iznos = 10.0 + (300.0 - 10.0) * this.generator.nextDouble();

    return BigDecimal.valueOf(iznos).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  private String vrijednost(Map<String, String> mapa, String kljuc) {
    if (mapa == null || mapa.get(kljuc) == null) {
      return "";
    }

    return mapa.get(kljuc);
  }
  
}
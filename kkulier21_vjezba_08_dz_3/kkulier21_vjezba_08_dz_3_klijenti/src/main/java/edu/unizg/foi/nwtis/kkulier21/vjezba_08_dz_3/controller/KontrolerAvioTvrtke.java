package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.controller;

import java.io.StringReader;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.KontrolaRestKlijent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.RacuniFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.AvioTvrtkeRestKlijent;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.RezervacijeRestKlijent;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.model.RadneRezervacije;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racunstavka;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.RacunstavkeFacade;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms.PosiljateljJmsRedPoruka;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.KorisniciRestKlijent;
import jakarta.ws.rs.FormParam;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.ws.WebSocketRezervacije;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.ws.WebSocketAvioTvrtke;

@Controller
@Path("aviotvrtke")
@RequestScoped
@RolesAllowed({"nwtis", "admin"})
public class KontrolerAvioTvrtke {

  @Inject
  private Models model;

  @Inject
  private RadneRezervacije radneRezervacije;

  @Inject
  private SecurityContext securityContext;

  @Inject
  private RacuniFacade racuniFacade;

  @Inject
  private KorisniciFacade korisniciFacade;

  @Inject
  @RestClient
  private AvioTvrtkeRestKlijent avioTvrtkeRestKlijent;

  @Inject
  @RestClient
  private RezervacijeRestKlijent rezervacijeRestKlijent;
  @Inject
  private RacunstavkeFacade racunstavkeFacade;

  @Inject
  private PosiljateljJmsRedPoruka posiljateljJmsRedPoruka;
  
  @Inject
  @RestClient
  private KorisniciRestKlijent korisniciRestKlijent;
  @Inject
  @RestClient
  private KontrolaRestKlijent kontrolaRestKlijent;

  @GET
  @Path("pocetak")
  @View("aviotvrtke/index.jsp")
  public void pocetak(@QueryParam("idUlaznice") String idUlaznice) {
    idUlaznice = dajIdUlaznice(idUlaznice);

    try (var odgovor = this.avioTvrtkeRestKlijent.dajSveAvioTvrtke()) {
      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        postaviPrazneAvioTvrtke("Avio tvrtke nisu dohvaćene. Status: " + odgovor.getStatus(),
            true, idUlaznice);
        return;
      }

      String json = odgovor.readEntity(String.class);
      List<Map<String, String>> avioTvrtke = procitajPolje(json);

      for (Map<String, String> avioTvrtka : avioTvrtke) {
        normalizirajAvioTvrtku(avioTvrtka);
      }

      model.put("avioTvrtke", avioTvrtke);
      model.put("idUlaznice", idUlaznice);
      model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
    } catch (Exception e) {
      postaviPrazneAvioTvrtke("Problem kod dohvaćanja avio tvrtki.", true, idUlaznice);
    }
  }
  @GET
  @Path("let")
  @View("aviotvrtke/letNaDatum.jsp")
  public void letNaDatum(@QueryParam("oznakaLeta") String oznakaLeta,
      @QueryParam("datum") String datum,
      @QueryParam("idUlaznice") String idUlaznice) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazanLetNaDatum("Prvo je potrebno odabrati poslužitelj rezervacija.", true,
          oznakaLeta, datum, idUlaznice);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      postaviPrazanLetNaDatum("ID ulaznice nije kreiran.", true, oznakaLeta, datum, "");
      return;
    }

    if (oznakaLeta == null || oznakaLeta.isBlank()) {
      postaviPrazanLetNaDatum("Oznaka leta nije zadana.", true, oznakaLeta, datum, idUlaznice);
      return;
    }

    if (datum == null || datum.isBlank()) {
      postaviPrazanLetNaDatum("Datum stvarnog leta nije upisan.", true, oznakaLeta, datum,
          idUlaznice);
      return;
    }

    if (!ispravanDatum(datum)) {
      postaviPrazanLetNaDatum("Datum mora biti upisan u formatu dd.mm.gggg, npr. 22.06.2026.",
          true, oznakaLeta, datum, idUlaznice);
      return;
    }

    String datumZaMikroservis = pretvoriDatumZaMikroservis(datum);

    if (datumZaMikroservis == null) {
      postaviPrazanLetNaDatum("Datum nije moguće pretvoriti za mikroservis.", true,
          oznakaLeta, datum, idUlaznice);
      return;
    }

    try (var initOdgovor = this.rezervacijeRestKlijent.inicijalizirajLetAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        datumZaMikroservis,
        datumZaMikroservis,
        "")) {

      String initTijelo = procitajTijeloOdgovora(initOdgovor);

      System.out.println("INIT stvarni let status = " + initOdgovor.getStatus());
      System.out.println("INIT stvarni let tijelo = " + initTijelo);

      if (initOdgovor.getStatus() != Status.OK.getStatusCode()
          && initOdgovor.getStatus() != Status.CREATED.getStatusCode()
          && initOdgovor.getStatus() != Status.NO_CONTENT.getStatusCode()) {

        postaviPrazanLetNaDatum("Stvarni let nije inicijaliziran. Status: "
            + initOdgovor.getStatus() + ". Odgovor mikroservisa: "
            + prevediGresku(initTijelo), true, oznakaLeta, datum, idUlaznice);
        return;
      }

    } catch (Exception e) {
      postaviPrazanLetNaDatum("Problem kod inicijalizacije stvarnog leta: "
          + e.getClass().getName() + " - " + e.getMessage(), true,
          oznakaLeta, datum, idUlaznice);
      e.printStackTrace();
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajLetNaDatum(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        datumZaMikroservis)) {

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      System.out.println("DOHVAT let na datum status = " + odgovor.getStatus());
      System.out.println("DOHVAT let na datum tijelo = " + tijeloOdgovora);

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        postaviPrazanLetNaDatum("Let nije dohvaćen. Status: " + odgovor.getStatus()
            + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora), true,
            oznakaLeta, datum, idUlaznice);
        return;
      }

      List<Map<String, String>> letovi = new ArrayList<>();

      if (tijeloOdgovora != null && tijeloOdgovora.trim().startsWith("OK")) {
        Map<String, String> let = napraviLetIzOkOdgovora(tijeloOdgovora, oznakaLeta, datum);
        letovi.add(let);
      } else {
        letovi = procitajPolje(tijeloOdgovora);

        for (Map<String, String> let : letovi) {
          normalizirajLet(let);
        }
      }

      model.put("oznakaLeta", oznakaLeta);
      model.put("datum", datum);
      model.put("idUlaznice", idUlaznice);
      model.put("letovi", letovi);

    } catch (Exception e) {
      postaviPrazanLetNaDatum("Problem kod dohvaćanja leta na datum: "
          + e.getClass().getName() + " - " + e.getMessage(), true,
          oznakaLeta, datum, idUlaznice);
      e.printStackTrace();
    }
  }
  private boolean inicijalizirajLetZaUlaznicu(String oznakaLeta, String datumZaMikroservis,
      String idUlaznice) {

    try (var odgovor = this.rezervacijeRestKlijent.inicijalizirajLetAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        datumZaMikroservis,
        datumZaMikroservis,
        "")) {

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      System.out.println("Inicijalizacija leta odgovor status = " + odgovor.getStatus());
      System.out.println("Inicijalizacija leta odgovor tijelo = " + tijeloOdgovora);

      return odgovor.getStatus() == Status.OK.getStatusCode()
          || odgovor.getStatus() == Status.CREATED.getStatusCode()
          || odgovor.getStatus() == Status.NO_CONTENT.getStatusCode();

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  private void dohvatiLetNakonInicijalizacije(String oznakaLeta, String datum,
      String datumZaMikroservis, String idUlaznice) {

    try (var odgovor = this.rezervacijeRestKlijent.dajLetNaDatum(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        datumZaMikroservis)) {

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        postaviPrazanLetNaDatum("Let nije dohvaćen ni nakon inicijalizacije. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: "
            + prevediGresku(tijeloOdgovora), true, oznakaLeta, datum, idUlaznice);
        return;
      }

      List<Map<String, String>> letovi = new ArrayList<>();

      if (tijeloOdgovora != null && tijeloOdgovora.trim().startsWith("OK")) {
        Map<String, String> let = napraviLetIzOkOdgovora(tijeloOdgovora, oznakaLeta, datum);
        letovi.add(let);
      } else {
        letovi = procitajPolje(tijeloOdgovora);

        for (Map<String, String> let : letovi) {
          normalizirajLet(let);
        }
      }

      model.put("oznakaLeta", oznakaLeta);
      model.put("datum", datum);
      model.put("idUlaznice", idUlaznice);
      model.put("letovi", letovi);

    } catch (Exception e) {
      postaviPrazanLetNaDatum("Problem kod dohvaćanja leta nakon inicijalizacije: "
          + e.getClass().getName() + " - " + e.getMessage(), true, oznakaLeta, datum,
          idUlaznice);
      e.printStackTrace();
    }
  }
  private boolean ispravanDatum(String datum) {
    if (datum == null || datum.isBlank()) {
      return false;
    }

    try {
      DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
      LocalDate.parse(datum.trim(), format);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String pretvoriDatumZaMikroservis(String datum) {
    if (datum == null || datum.isBlank()) {
      return null;
    }

    try {
      DateTimeFormatter ulazniFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
      DateTimeFormatter izlazniFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

      LocalDate lokalniDatum = LocalDate.parse(datum.trim(), ulazniFormat);
      return lokalniDatum.format(izlazniFormat);
    } catch (Exception e) {
      return null;
    }
  }

  private Map<String, String> napraviLetIzOkOdgovora(String odgovor, String oznakaLeta,
      String datum) {

    Map<String, String> let = new LinkedHashMap<>();

    String ostatak = odgovor == null ? "" : odgovor.replaceFirst("^OK", "").trim();
    String idLeta = "";

    if (!ostatak.isBlank()) {
      String[] dijelovi = ostatak.split("\\s+");
      idLeta = dijelovi[0];
    }

    let.put("oznakaLeta", oznakaLeta);
    let.put("idLeta", idLeta);
    let.put("vrijemePolijetanja", datum);
    let.put("polazniAerodrom", "");
    let.put("odredisniAerodrom", "");

    return let;
  }
 

  @GET
  @Path("sjedala/{idLeta}")
  @View("aviotvrtke/sjedala.jsp")
  public void sjedalaLeta(@PathParam("idLeta") String idLeta,
      @QueryParam("idUlaznice") String idUlaznice) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPraznaSjedala("Prvo je potrebno odabrati poslužitelj rezervacija.", true, idLeta,
          idUlaznice);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajSjedalaLeta(
        this.radneRezervacije.getPosluzitelj(), idLeta, idUlaznice)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        postaviPraznaSjedala("Sjedala nisu dohvaćena. Status: " + odgovor.getStatus()
            + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora), true,
            idLeta, idUlaznice);
        return;
      }

      String json = odgovor.readEntity(String.class);
      List<Map<String, String>> sjedala = procitajSjedala(json);

      model.put("idLeta", idLeta);
      model.put("idUlaznice", idUlaznice);
      model.put("sjedala", sjedala);
      model.put("imaOtvorenRacun", imaOtvorenRacun());

    } catch (Exception e) {
      postaviPraznaSjedala("Problem kod dohvaćanja sjedala: "
          + e.getClass().getName() + " - " + e.getMessage(), true, idLeta, idUlaznice);
      e.printStackTrace();
    }
  }

  @POST
  @Path("rezerviraj/{idLeta}/{razred}")
  @View("aviotvrtke/sjedala.jsp")
  public void rezervirajSjedalo(@PathParam("idLeta") String idLeta,
      @PathParam("razred") String razred,
      @QueryParam("idUlaznice") String idUlaznice) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPraznaSjedala("Prvo je potrebno odabrati poslužitelj rezervacija.", true, idLeta,
          idUlaznice);
      return;
    }

    if (!imaOtvorenRacun()) {
      postaviPraznaSjedala("Ne postoji otvoreni račun zastupnika. Rezervacija sjedala nije moguća.",
          true, idLeta, idUlaznice);
      return;
    }

    Map<String, String> putnik = this.radneRezervacije.getPutnik();

    if (putnik == null) {
      postaviPraznaSjedala("Prvo je potrebno odabrati putnika.", true, idLeta, idUlaznice);
      return;
    }

    String putnikEmail = putnik.get("email");

    if (putnikEmail == null || putnikEmail.isBlank()) {
      postaviPraznaSjedala("Odabrani putnik nema email adresu.", true, idLeta, idUlaznice);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.rezervirajSjedalo(
        this.radneRezervacije.getPosluzitelj(), idUlaznice, idLeta, razred, putnikEmail,
        razred)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()
          && odgovor.getStatus() != Status.CREATED.getStatusCode()) {

        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        postaviPraznaSjedala("Sjedalo nije rezervirano. Status: " + odgovor.getStatus()
            + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora), true, idLeta,
            idUlaznice);
        return;
      }

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);
      String idRezervacijeSjedala = izvuciIdRezervacijeSjedala(tijeloOdgovora);

      if (idRezervacijeSjedala == null || idRezervacijeSjedala.isBlank()) {
        postaviPraznaSjedala("Rezervacija je napravljena, ali nije dohvaćen ID rezervacije.",
            true, idLeta, idUlaznice);
        return;
      }

      Map<String, String> rezervacija = new HashMap<>();
      rezervacija.put("idRezervacijeSjedala", idRezervacijeSjedala);
      rezervacija.put("idLeta", idLeta);
      rezervacija.put("razred", razred);
      rezervacija.put("putnikIme", putnik.get("ime"));
      rezervacija.put("putnikPrezime", putnik.get("prezime"));
      rezervacija.put("putnikEmail", putnikEmail);
      rezervacija.put("idUlaznice", idUlaznice);

      boolean dodana = this.radneRezervacije.dodajPrivremenuRezervaciju(rezervacija);

      if (!dodana) {
        postaviPraznaSjedala("Rezervacija je napravljena, ali nije dodana u privremenu listu. "
            + "Moguće je da već postoji rezervacija za taj let.", true, idLeta, idUlaznice);
        return;
      }
      
      WebSocketRezervacije.send("REZERVACIJA," + idLeta + "," + putnikEmail + ","
          + razred + "," + idRezervacijeSjedala);

      ucitajSjedalaNakonRezervacije(idLeta, idUlaznice,
          "Sjedalo u razredu " + razred + " je rezervirano za putnika "
              + putnik.get("ime") + " " + putnik.get("prezime")
              + " i dodano u privremene rezervacije.",
          false);

    } catch (Exception e) {
      postaviPraznaSjedala("Problem kod rezervacije sjedala: "
          + e.getClass().getName() + " - " + e.getMessage(), true, idLeta, idUlaznice);
      e.printStackTrace();
    }
  }
  private void ucitajSjedalaNakonRezervacije(String idLeta, String idUlaznice, String poruka,
      boolean pogreska) {

    try (var odgovor = this.rezervacijeRestKlijent.dajSjedalaLeta(
        this.radneRezervacije.getPosluzitelj(), idLeta, idUlaznice)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        postaviPraznaSjedala("Sjedala nisu dohvaćena nakon rezervacije. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora),
            true, idLeta, idUlaznice);
        return;
      }

      String json = odgovor.readEntity(String.class);
      List<Map<String, String>> sjedala = procitajSjedala(json);

      model.put("poruka", poruka);
      model.put("pogreska", pogreska);
      model.put("idLeta", idLeta);
      model.put("idUlaznice", idUlaznice);
      model.put("sjedala", sjedala);
      model.put("imaOtvorenRacun", imaOtvorenRacun());

    } catch (Exception e) {
      postaviPraznaSjedala("Problem kod ponovnog dohvaćanja sjedala: "
          + e.getClass().getName() + " - " + e.getMessage(), true, idLeta, idUlaznice);
      e.printStackTrace();
    }
  }
  private List<Map<String, String>> procitajSjedala(String json) {
    List<Map<String, String>> rezultat = new ArrayList<>();

    if (json == null || json.isBlank()) {
      return rezultat;
    }

    try (var reader = Json.createReader(new StringReader(json))) {
      JsonValue vrijednost = reader.readValue();

      if (vrijednost.getValueType() != JsonValue.ValueType.ARRAY) {
        return rezultat;
      }

      JsonArray polje = vrijednost.asJsonArray();

      for (JsonValue v : polje) {
        if (v.getValueType() != JsonValue.ValueType.OBJECT) {
          continue;
        }

        JsonObject objekt = v.asJsonObject();
        Map<String, String> sjedaloMapa = new LinkedHashMap<>();

        sjedaloMapa.put("id", dohvatiString(objekt, "id"));
        sjedaloMapa.put("status", dohvatiString(objekt, "statusRezervacije"));

        JsonObject sjedaloObjekt = objekt.getJsonObject("sjedalo");

        if (sjedaloObjekt != null) {
          sjedaloMapa.put("red", dohvatiIntKaoString(sjedaloObjekt, "red"));
          sjedaloMapa.put("sjedalo", dohvatiString(sjedaloObjekt, "oznakaSjedala"));
          sjedaloMapa.put("oznakaSjedala", dohvatiString(sjedaloObjekt, "oznakaSjedala"));
          sjedaloMapa.put("razred", dohvatiString(sjedaloObjekt, "razred"));
        } else {
          sjedaloMapa.put("red", "");
          sjedaloMapa.put("sjedalo", "");
          sjedaloMapa.put("oznakaSjedala", "");
          sjedaloMapa.put("razred", "");
        }

        if (sjedaloMapa.get("status") == null || sjedaloMapa.get("status").isBlank()) {
          sjedaloMapa.put("status", "SLOBODNA");
        }

        rezultat.add(sjedaloMapa);
      }
    }

    return rezultat;
  }
  private String dohvatiString(JsonObject objekt, String kljuc) {
    if (objekt == null || kljuc == null || !objekt.containsKey(kljuc) || objekt.isNull(kljuc)) {
      return "";
    }

    try {
      return objekt.getString(kljuc);
    } catch (Exception e) {
      return objekt.get(kljuc).toString().replace("\"", "");
    }
  }

  private String dohvatiIntKaoString(JsonObject objekt, String kljuc) {
    if (objekt == null || kljuc == null || !objekt.containsKey(kljuc) || objekt.isNull(kljuc)) {
      return "";
    }

    try {
      return Integer.toString(objekt.getInt(kljuc));
    } catch (Exception e) {
      return objekt.get(kljuc).toString().replace("\"", "");
    }
  }

  private boolean imaOtvorenRacun() {
    String korisnik = this.securityContext.getCallerPrincipal().getName();
    return this.racuniFacade.postojiOtvoreniRacun(korisnik);
  }

  private void postaviPrazneAvioTvrtke(String poruka, boolean pogreska, String idUlaznice) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("avioTvrtke", new ArrayList<Map<String, String>>());
    model.put("idUlaznice", idUlaznice);
    model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
  }

  private void postaviPrazneLetove(String poruka, boolean pogreska, String avioTvrtka,
      String idUlaznice) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("avioTvrtka", avioTvrtka);
    model.put("idUlaznice", idUlaznice);
    model.put("letovi", new ArrayList<Map<String, String>>());
  }

  private void postaviPrazanLetNaDatum(String poruka, boolean pogreska, String oznakaLeta,
      String datum, String idUlaznice) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("oznakaLeta", oznakaLeta);
    model.put("datum", datum);
    model.put("idUlaznice", idUlaznice);
    model.put("letovi", new ArrayList<Map<String, String>>());
  }

  private void postaviPraznaSjedala(String poruka, boolean pogreska, String idLeta,
      String idUlaznice) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("idLeta", idLeta);
    model.put("idUlaznice", idUlaznice);
    model.put("sjedala", new ArrayList<Map<String, String>>());
    model.put("imaOtvorenRacun", imaOtvorenRacun());
  }

  private List<Map<String, String>> procitajPolje(String json) {
    List<Map<String, String>> rezultat = new ArrayList<>();

    if (json == null || json.isBlank()) {
      return rezultat;
    }

    try (var reader = Json.createReader(new StringReader(json))) {
      JsonValue vrijednost = reader.readValue();

      if (vrijednost.getValueType() == JsonValue.ValueType.ARRAY) {
        JsonArray polje = vrijednost.asJsonArray();

        for (JsonValue v : polje) {
          if (v.getValueType() == JsonValue.ValueType.OBJECT) {
            rezultat.add(procitajObjekt(v.asJsonObject()));
          }
        }
      } else if (vrijednost.getValueType() == JsonValue.ValueType.OBJECT) {
        rezultat.add(procitajObjekt(vrijednost.asJsonObject()));
      }
    }

    return rezultat;
  }

  private Map<String, String> procitajObjekt(JsonObject objekt) {
    Map<String, String> mapa = new LinkedHashMap<>();

    for (String kljuc : objekt.keySet()) {
      JsonValue vrijednost = objekt.get(kljuc);

      if (vrijednost == null || vrijednost.getValueType() == JsonValue.ValueType.NULL) {
        mapa.put(kljuc, "");
      } else {
        mapa.put(kljuc, vrijednost.toString().replace("\"", ""));
      }
    }

    return mapa;
  }

  private void normalizirajAvioTvrtku(Map<String, String> avioTvrtka) {
    postaviAkoNedostaje(avioTvrtka, "atid", "id", "icao", "oznaka", "naziv");
    postaviAkoNedostaje(avioTvrtka, "naziv", "name", "ime", "opis");
  }

  private void normalizirajLet(Map<String, String> let) {
    postaviAkoNedostaje(let, "oznakaLeta", "oznaka", "brojLeta", "let", "flight");
    postaviAkoNedostaje(let, "idLeta", "id", "letId", "flightId");
    postaviAkoNedostaje(let, "vrijemePolijetanja", "vrijeme", "polijetanje",
        "datumVrijemePolijetanja", "departureTime");
    postaviAkoNedostaje(let, "polazniAerodrom", "polaziste", "polazni", "departureAirport");
    postaviAkoNedostaje(let, "odredisniAerodrom", "odrediste", "odredisni",
        "arrivalAirport");
  }

  private void normalizirajSjedalo(Map<String, String> sjedalo) {
    postaviAkoNedostaje(sjedalo, "sjedalo", "oznaka", "seat", "id", "broj");
    postaviAkoNedostaje(sjedalo, "red", "row", "redSjedala");
    postaviAkoNedostaje(sjedalo, "status", "stanje", "seatStatus");
  }

  private void postaviAkoNedostaje(Map<String, String> mapa, String cilj, String... moguci) {
    if (mapa.get(cilj) != null && !mapa.get(cilj).isBlank()) {
      return;
    }

    for (String kljuc : moguci) {
      if (mapa.get(kljuc) != null && !mapa.get(kljuc).isBlank()) {
        mapa.put(cilj, mapa.get(kljuc));
        return;
      }
    }

    mapa.put(cilj, "");
  }
  @GET
  @Path("zastupnik")
  @View("aviotvrtke/zastupnik.jsp")
  public void zastupnik(@QueryParam("idUlaznice") String idUlaznice) {

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      model.put("poruka",
          "Prvo je potrebno odabrati poslužitelj rezervacija. Dok poslužitelj nije odabran, aktivnosti zastupnika avio tvrtke nisu dostupne.");
      model.put("pogreska", true);
      model.put("odabranPosluzitelj", false);
      model.put("posluzitelj", this.radneRezervacije.getPosluzitelj());
      model.put("idUlaznice", this.radneRezervacije.getIdUlaznice());
      model.put("putnik", this.radneRezervacije.getPutnik());
      model.put("rezervacije", this.radneRezervacije.getRezervacije());
      return;
    }

    idUlaznice = dajIdUlaznice(idUlaznice);

    model.put("odabranPosluzitelj", true);
    model.put("posluzitelj", this.radneRezervacije.getPosluzitelj());
    model.put("idUlaznice", idUlaznice);
    model.put("putnik", this.radneRezervacije.getPutnik());
    model.put("rezervacije", this.radneRezervacije.getRezervacije());
  }
  @GET
  @Path("zastupnik/obrisiPrivremeneRezervacije")
  @View("aviotvrtke/zastupnik.jsp")
  public void obrisiPrivremeneRezervacije() {
    this.radneRezervacije.obrisiRezervacije();

    model.put("poruka", "Privremene rezervacije su obrisane.");
    model.put("pogreska", false);
    model.put("odabranPosluzitelj", this.radneRezervacije.jeOdabranPosluzitelj());
    model.put("posluzitelj", this.radneRezervacije.getPosluzitelj());
    model.put("idUlaznice", this.radneRezervacije.getIdUlaznice());
    model.put("putnik", this.radneRezervacije.getPutnik());
    model.put("rezervacije", this.radneRezervacije.getRezervacije());
  }
  
  @GET
  @Path("zastupnik/nepotvrdjeneRezervacije")
  @View("aviotvrtke/nepotvrdjeneRezervacije.jsp")
  public void nepotvrdjeneRezervacijeZastupnika() {
    model.put("rezervacije", this.radneRezervacije.getRezervacije());
    model.put("putnik", this.radneRezervacije.getPutnik());
    model.put("imaOtvorenRacun", imaOtvorenRacun());
  }
  @GET
  @Path("zastupnik/potvrdiRezervaciju/{idRezervacijeSjedala}")
  @View("aviotvrtke/nepotvrdjeneRezervacije.jsp")
  public void potvrdiRezervacijuZastupnika(
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @QueryParam("idUlaznice") String idUlaznice) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      pripremiNepotvrdjeneRezervacije("Prvo je potrebno odabrati poslužitelj rezervacija.", true);
      return;
    }

    if (!imaOtvorenRacun()) {
      pripremiNepotvrdjeneRezervacije("Ne postoji otvoreni račun zastupnika.", true);
      return;
    }

    Map<String, String> rezervacija = this.radneRezervacije.dajRezervaciju(idRezervacijeSjedala);

    if (rezervacija == null) {
      pripremiNepotvrdjeneRezervacije("Privremena rezervacija nije pronađena.", true);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      idUlaznice = rezervacija.get("idUlaznice");
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      pripremiNepotvrdjeneRezervacije("Nedostaje ID ulaznice za potvrđivanje rezervacije.", true);
      return;
    }

    String putnikEmail = rezervacija.get("putnikEmail");

    if (putnikEmail == null || putnikEmail.isBlank()) {
      pripremiNepotvrdjeneRezervacije("Privremena rezervacija nema email putnika.", true);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.potvrdiRezervaciju(
        this.radneRezervacije.getPosluzitelj(),
        idRezervacijeSjedala,
        idUlaznice,
        putnikEmail,
        "")) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()
          && odgovor.getStatus() != Status.NO_CONTENT.getStatusCode()) {

        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        pripremiNepotvrdjeneRezervacije("Rezervacija nije potvrđena. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora),
            true);
        return;
      }

      String zastupnik = this.securityContext.getCallerPrincipal().getName();
      String putnikIme = rezervacija.get("putnikIme");
      String putnikPrezime = rezervacija.get("putnikPrezime");
      double iznos = generirajIznosStavke();

      Racunstavka stavka = this.racuniFacade.dodajStavkuUOtvoreniRacun(
          zastupnik,
          idRezervacijeSjedala,
          putnikIme,
          putnikPrezime,
          iznos);

      if (stavka == null) {
        pripremiNepotvrdjeneRezervacije(
            "Rezervacija je potvrđena, ali stavka računa nije dodana.", true);
        return;
      }

      this.radneRezervacije.obrisiPrivremenuRezervaciju(rezervacija.get("idLeta"));
      
      WebSocketRezervacije.send("POTVRDA," + idRezervacijeSjedala + ","
          + putnikEmail + "," + rezervacija.get("idLeta"));

      pripremiNepotvrdjeneRezervacije(
          "Rezervacija je potvrđena i dodana kao stavka računa.", false);

    } catch (Exception e) {
      Throwable uzrok = e;

      while (uzrok.getCause() != null) {
        uzrok = uzrok.getCause();
      }

      pripremiNepotvrdjeneRezervacije(
          "Problem kod potvrđivanja rezervacije: "
              + e.getClass().getName() + " - " + e.getMessage()
              + ". Pravi uzrok: " + uzrok.getClass().getName() + " - " + uzrok.getMessage(),
          true);

      e.printStackTrace();
    }
  }
  private void pripremiNepotvrdjeneRezervacije(String poruka, boolean pogreska) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("rezervacije", this.radneRezervacije.getRezervacije());
    model.put("putnik", this.radneRezervacije.getPutnik());
    model.put("imaOtvorenRacun", imaOtvorenRacun());
  }
  private double generirajIznosStavke() {
    double iznos = 10.0 + Math.random() * 290.0;
    return Math.round(iznos * 100.0) / 100.0;
  }
  private String izvuciIdRezervacijeSjedala(String odgovor) {
    if (odgovor == null || odgovor.isBlank()) {
      return null;
    }

    String ocisceniOdgovor = odgovor.trim();

    if (ocisceniOdgovor.startsWith("OK")) {
      String ostatak = ocisceniOdgovor.replaceFirst("^OK", "").trim();

      if (!ostatak.isBlank()) {
        String[] dijelovi = ostatak.split("\\s+");
        return dijelovi[0];
      }
    }

    return ocisceniOdgovor;
  }
  
  @GET
  @Path("zastupnik/racuni")
  @View("aviotvrtke/racuniZastupnika.jsp")
  public void racuniZastupnika() {
    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      model.put("poruka", "Prvo je potrebno odabrati poslužitelj rezervacija.");
      model.put("pogreska", true);
      model.put("racuni", new ArrayList<Racun>());
      model.put("stvarniBrojeviStavki", new HashMap<Integer, Integer>());
      model.put("sviRacuniZatvoreni", false);
      return;
    }

    pripremiRacuneZastupnika(null, false);
  }

  @GET
  @Path("zastupnik/otvoriRacun")
  @View("aviotvrtke/racuniZastupnika.jsp")
  public void otvoriRacunZastupnika() {
    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      model.put("poruka", "Prvo je potrebno odabrati poslužitelj rezervacija.");
      model.put("pogreska", true);
      model.put("racuni", new ArrayList<Racun>());
      model.put("stvarniBrojeviStavki", new HashMap<Integer, Integer>());
      model.put("sviRacuniZatvoreni", false);
      return;
    }

    String korisnik = this.securityContext.getCallerPrincipal().getName();

    if (this.racuniFacade.postojiOtvoreniRacun(korisnik)) {
      pripremiRacuneZastupnika("Nije moguće otvoriti novi račun dok postoji otvoreni račun.",
          true);
      return;
    }

    Racun racun = this.racuniFacade.otvoriRacun(korisnik);

    if (racun == null) {
      pripremiRacuneZastupnika("Novi račun nije otvoren.", true);
      return;
    }

    pripremiRacuneZastupnika("Otvoren je novi račun zastupnika: " + racun.getId(), false);
  }

  @GET
  @Path("zastupnik/stavkeRacuna/{id}")
  @View("aviotvrtke/stavkeRacunaZastupnika.jsp")
  public void stavkeRacunaZastupnika(@PathParam("id") int id) {
    Racun racun = this.racuniFacade.find(id);

    if (racun == null || !racunPripadaPrijavljenomKorisniku(racun)) {
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
  @Path("zastupnik/uskladiStavke/{id}")
  @View("aviotvrtke/stavkeRacunaZastupnika.jsp")
  public void uskladiStavkeRacunaZastupnika(@PathParam("id") int id) {
    Racun racun = this.racuniFacade.find(id);

    if (racun == null || !racunPripadaPrijavljenomKorisniku(racun)) {
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
  @Path("zastupnik/zatvoriRacun/{id}")
  @View("aviotvrtke/racuniZastupnika.jsp")
  public void zatvoriRacunZastupnika(@PathParam("id") int id) {
    Racun racun = this.racuniFacade.find(id);

    if (racun == null || !racunPripadaPrijavljenomKorisniku(racun)) {
      pripremiRacuneZastupnika("Račun nije pronađen.", true);
      return;
    }

    int stvarniBrojStavki = this.racuniFacade.dajStvarniBrojStavki(id);

    if (stvarniBrojStavki == 0) {
      pripremiRacuneZastupnika("Račun se ne može zatvoriti jer nema nijednu stavku.", true);
      return;
    }

    if (racun.getIznos() != 0.0) {
      pripremiRacuneZastupnika("Račun je već zatvoren.", true);
      return;
    }

    if (racun.getBrojstavki() != stvarniBrojStavki) {
      pripremiRacuneZastupnika("Račun se ne može zatvoriti jer broj stavki nije usklađen.",
          true);
      return;
    }

    Racun zatvoreniRacun = this.racuniFacade.zatvoriRacun(id);

    if (zatvoreniRacun == null) {
      pripremiRacuneZastupnika("Račun nije zatvoren.", true);
      return;
    }

    this.radneRezervacije.obrisiRezervacije();

    List<Racunstavka> stavke = this.racunstavkeFacade.dajStavkeRacuna(id);
    String json = napraviJsonRacunaZastupnika(zatvoreniRacun, stavke);
    boolean jmsPoslan = this.posiljateljJmsRedPoruka.novaPoruka(json);

    pripremiRacuneZastupnika("Račun je zatvoren. Privremene rezervacije su obrisane. JMS poruka "
        + (jmsPoslan ? "je poslana." : "nije poslana."), !jmsPoslan);
  }

  private void pripremiRacuneZastupnika(String poruka, boolean pogreska) {
    String korisnik = this.securityContext.getCallerPrincipal().getName();

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

  private boolean racunPripadaPrijavljenomKorisniku(Racun racun) {
    if (racun == null || racun.getKorisnici() == null) {
      return false;
    }

    String korisnik = this.securityContext.getCallerPrincipal().getName();
    return racun.getKorisnici().getKorisnik().equals(korisnik);
  }

  private String napraviJsonRacunaZastupnika(Racun racun, List<Racunstavka> stavke) {
    StringBuilder json = new StringBuilder();

    json.append("{");
    json.append("\"id\":").append(racun.getId()).append(",");
    json.append("\"korisnik\":\"").append(escapeJsonZastupnik(racun.getKorisnici().getKorisnik()))
        .append("\",");
    json.append("\"brojstavki\":").append(racun.getBrojstavki()).append(",");
    json.append("\"iznos\":").append(racun.getIznos()).append(",");
    json.append("\"vrijeme\":\"").append(racun.getVrijeme()).append("\",");
    json.append("\"stavke\":[");

    for (int i = 0; i < stavke.size(); i++) {
      Racunstavka s = stavke.get(i);

      json.append("{");
      json.append("\"id\":").append(s.getId()).append(",");
      json.append("\"iznos\":").append(s.getIznos()).append(",");
      json.append("\"putnikime\":\"").append(escapeJsonZastupnik(s.getPutnikime())).append("\",");
      json.append("\"putnikiprezime\":\"").append(escapeJsonZastupnik(s.getPutnikiprezime()))
          .append("\",");
      json.append("\"rezervacija\":\"").append(escapeJsonZastupnik(s.getRezervacija()))
          .append("\"");
      json.append("}");

      if (i < stavke.size() - 1) {
        json.append(",");
      }
    }

    json.append("]");
    json.append("}");

    return json.toString();
  }

  private String escapeJsonZastupnik(String tekst) {
    if (tekst == null) {
      return "";
    }

    return tekst.replace("\\", "\\\\").replace("\"", "\\\"");
  }
  
  @GET
  @Path("zastupnik/putnici")
  @View("aviotvrtke/putnici.jsp")
  public void putnici(@QueryParam("odStranice") Integer odStranice) {
    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPraznePutnike("Prvo je potrebno odabrati poslužitelj rezervacija.", true, 1);
      return;
    }

    int stranica = odStranice == null || odStranice < 1 ? 1 : odStranice;
    dohvatiPutnike(stranica, null, false);
  }

  @GET
  @Path("zastupnik/odaberiPutnika/{id}")
  @View("aviotvrtke/putnici.jsp")
  public void odaberiPutnika(@PathParam("id") String id,
      @QueryParam("odStranice") Integer odStranice) {

    int stranica = odStranice == null || odStranice < 1 ? 1 : odStranice;
    List<Map<String, String>> putnici = dohvatiPutnikeIzRest(stranica);

    for (Map<String, String> putnik : putnici) {
      normalizirajPutnika(putnik);

      if (id.equals(putnik.get("korisnik"))) {
        this.radneRezervacije.setPutnik(putnik);
        dohvatiPutnike(stranica, "Odabran je putnik: " + putnik.get("korisnik"), false);
        return;
      }
    }

    dohvatiPutnike(stranica, "Putnik nije pronađen.", true);
  }
  @GET
  @Path("zastupnik/letoviPutnika/{id}")
  @View("aviotvrtke/letoviPutnika.jsp")
  public void letoviPutnika(@PathParam("id") String id,
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("odDatuma") String odDatuma,
      @QueryParam("doDatuma") String doDatuma) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneLetovePutnika("Prvo je potrebno odabrati poslužitelj rezervacija.", true,
          id, idUlaznice, odDatuma, doDatuma);
      return;
    }

    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (atid == null || atid.isBlank()) {
      postaviPrazneLetovePutnika("Prijavljeni korisnik nije zastupnik ni jedne avio tvrtke.",
          true, id, idUlaznice, odDatuma, doDatuma);
      return;
    }

    Map<String, String> putnik = this.radneRezervacije.getPutnik();

    if (putnik == null || !id.equals(putnik.get("korisnik"))) {
      postaviPrazneLetovePutnika("Prvo je potrebno odabrati ovog putnika.", true,
          id, idUlaznice, odDatuma, doDatuma);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()
        || odDatuma == null || odDatuma.isBlank()
        || doDatuma == null || doDatuma.isBlank()) {

      model.put("putnikId", id);
      model.put("idUlaznice", idUlaznice);
      model.put("odDatuma", odDatuma);
      model.put("doDatuma", doDatuma);
      model.put("putnik", putnik);
      model.put("letovi", new ArrayList<Map<String, String>>());
      model.put("atid", atid);
      return;
    }

    String odDatumaZaServis = pretvoriDatumZaMikroservis(odDatuma);
    String doDatumaZaServis = pretvoriDatumZaMikroservis(doDatuma);

    if (odDatumaZaServis == null || doDatumaZaServis == null) {
      postaviPrazneLetovePutnika("Datumi moraju biti u formatu dd.mm.gggg.",
          true, id, idUlaznice, odDatuma, doDatuma);
      return;
    }

    String putnikEmail = putnik.get("email");

    if (putnikEmail == null || putnikEmail.isBlank()) {
      postaviPrazneLetovePutnika("Odabrani putnik nema email adresu.", true,
          id, idUlaznice, odDatuma, doDatuma);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajLetovePutnikaZaAvioTvrtku(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        putnikEmail,
        atid,
        odDatumaZaServis,
        doDatumaZaServis)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        postaviPrazneLetovePutnika("Letovi putnika nisu dohvaćeni. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora),
            true, id, idUlaznice, odDatuma, doDatuma);
        return;
      }

      String json = odgovor.readEntity(String.class);
      List<Map<String, String>> letovi = procitajPolje(json);

      for (Map<String, String> let : letovi) {
        normalizirajLet(let);
      }

      model.put("putnikId", id);
      model.put("idUlaznice", idUlaznice);
      model.put("odDatuma", odDatuma);
      model.put("doDatuma", doDatuma);
      model.put("putnik", putnik);
      model.put("letovi", letovi);
      model.put("atid", atid);

    } catch (Exception e) {
      postaviPrazneLetovePutnika("Problem kod dohvaćanja letova putnika: "
          + e.getClass().getName() + " - " + e.getMessage(),
          true, id, idUlaznice, odDatuma, doDatuma);
      e.printStackTrace();
    }
  }

  @GET
  @Path("zastupnik/dodajZastupnika/{id}")
  @View("aviotvrtke/putnici.jsp")
  public void dodajZastupnika(@PathParam("id") String id,
      @QueryParam("odStranice") Integer odStranice) {

    int stranica = odStranice == null || odStranice < 1 ? 1 : odStranice;
    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (atid == null || atid.isBlank()) {
      dohvatiPutnike(stranica, "Prijavljeni korisnik nije zastupnik ni jedne avio tvrtke.", true);
      return;
    }

    try (var odgovor = this.avioTvrtkeRestKlijent.dodajZastupnika(atid, id)) {
      if (odgovor.getStatus() == Status.OK.getStatusCode()
          || odgovor.getStatus() == Status.CREATED.getStatusCode()
          || odgovor.getStatus() == Status.NO_CONTENT.getStatusCode()) {
        dohvatiPutnike(stranica, "Korisnik je dodan kao zastupnik avio tvrtke " + atid + ".",
            false);
      } else {
        dohvatiPutnike(stranica, "Korisnik nije dodan kao zastupnik. Status: "
            + odgovor.getStatus(), true);
      }
    } catch (Exception e) {
      dohvatiPutnike(stranica, "Problem kod dodavanja zastupnika.", true);
    }
  }

  @GET
  @Path("zastupnik/obrisiZastupnika/{id}")
  @View("aviotvrtke/putnici.jsp")
  public void obrisiZastupnika(@PathParam("id") String id,
      @QueryParam("odStranice") Integer odStranice) {

    int stranica = odStranice == null || odStranice < 1 ? 1 : odStranice;
    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (atid == null || atid.isBlank()) {
      dohvatiPutnike(stranica, "Prijavljeni korisnik nije zastupnik ni jedne avio tvrtke.", true);
      return;
    }

    try (var odgovor = this.avioTvrtkeRestKlijent.obrisiZastupnika(atid, id)) {
      if (odgovor.getStatus() == Status.OK.getStatusCode()
          || odgovor.getStatus() == Status.NO_CONTENT.getStatusCode()) {
        dohvatiPutnike(stranica, "Korisnik je obrisan kao zastupnik avio tvrtke " + atid + ".",
            false);
      } else {
        dohvatiPutnike(stranica, "Korisnik nije obrisan kao zastupnik. Status: "
            + odgovor.getStatus(), true);
      }
    } catch (Exception e) {
      dohvatiPutnike(stranica, "Problem kod brisanja zastupnika.", true);
    }
  }
  @GET
  @Path("letovi/{avioTvrtka}")
  @View("aviotvrtke/letovi.jsp")
  public void letoviAvioTvrtke(@PathParam("avioTvrtka") String avioTvrtka,
      @QueryParam("idUlaznice") String idUlaznice) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneLetove("Prvo je potrebno odabrati poslužitelj rezervacija.", true,
          avioTvrtka, idUlaznice);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      postaviPrazneLetove("ID ulaznice nije upisan.", true, avioTvrtka, idUlaznice);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajLetoveAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(), avioTvrtka, idUlaznice)) {

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      System.out.println("KORISNICKI LETOVI status = " + odgovor.getStatus());
      System.out.println("KORISNICKI LETOVI tijelo = " + tijeloOdgovora);

      if (odgovor.getStatus() == Status.OK.getStatusCode()) {
        List<Map<String, String>> letovi = procitajPolje(tijeloOdgovora);

        for (Map<String, String> let : letovi) {
          normalizirajLet(let);
        }

        model.put("avioTvrtka", avioTvrtka);
        model.put("idUlaznice", idUlaznice);
        model.put("letovi", letovi);
        return;
      }

      /*
       * Ako korisnički endpoint vrati ERROR 20 ili ERROR 21,
       * pokušavamo dohvatiti letove preko zastupničkog endpointa.
       */
      if (tijeloOdgovora != null
          && (tijeloOdgovora.startsWith("ERROR 20")
              || tijeloOdgovora.startsWith("ERROR 21"))) {

        dohvatiLetovePrekoZastupnika(avioTvrtka, idUlaznice,
            "Napomena: korisnički endpoint nije vratio letove pa su letovi dohvaćeni preko zastupničkog endpointa.");
        return;
      }

      postaviPrazneLetove("Letovi nisu dohvaćeni. Status: " + odgovor.getStatus()
          + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora), true,
          avioTvrtka, idUlaznice);

    } catch (Exception e) {
      System.out.println("OPĆA GREŠKA kod dohvaćanja letova:");
      e.printStackTrace();

      postaviPrazneLetove("Problem kod dohvaćanja letova avio tvrtke: "
          + e.getClass().getName() + " - " + e.getMessage(), true, avioTvrtka,
          idUlaznice);
    }
  }
  private void dohvatiLetovePrekoZastupnika(String avioTvrtka, String idUlaznice, String poruka) {
    try (var odgovor = this.rezervacijeRestKlijent.dajLetoveAvioTvrtkeZastupnika(
        this.radneRezervacije.getPosluzitelj(), idUlaznice)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        postaviPrazneLetove("Letovi nisu dohvaćeni ni preko zastupničkog endpointa. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora),
            true, avioTvrtka, idUlaznice);
        return;
      }

      String json = odgovor.readEntity(String.class);
      List<Map<String, String>> sviLetovi = procitajPolje(json);
      List<Map<String, String>> letovi = new ArrayList<>();

      for (Map<String, String> let : sviLetovi) {
        normalizirajLet(let);

        if (letPripadaAvioTvrtki(let, avioTvrtka)) {
          letovi.add(let);
        }
      }

      model.put("avioTvrtka", avioTvrtka);
      model.put("idUlaznice", idUlaznice);
      model.put("letovi", letovi);

      if (poruka != null) {
        model.put("poruka", poruka);
        model.put("pogreska", false);
      }

      if (letovi.isEmpty()) {
        model.put("poruka", "Nema letova za avio tvrtku " + avioTvrtka
            + ". Provjeri koristiš li ulaznicu zastupnika te avio tvrtke.");
        model.put("pogreska", true);
      }

    } catch (jakarta.ws.rs.WebApplicationException e) {
      int status = e.getResponse() != null ? e.getResponse().getStatus() : -1;
      String tijeloOdgovora = "";

      try {
        if (e.getResponse() != null) {
          tijeloOdgovora = e.getResponse().readEntity(String.class);
        }
      } catch (Exception ex) {
        tijeloOdgovora = "Nije moguće pročitati tijelo odgovora: "
            + ex.getClass().getSimpleName() + " - " + ex.getMessage();
      }

      System.out.println("GREŠKA kod zastupničkog dohvaćanja letova:");
      System.out.println("Status: " + status);
      System.out.println("Tijelo odgovora: " + tijeloOdgovora);
      e.printStackTrace();

      postaviPrazneLetove("Problem kod dohvaćanja letova preko zastupničkog endpointa. Status: "
          + status + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora),
          true, avioTvrtka, idUlaznice);

    } catch (Exception e) {
      System.out.println("OPĆA GREŠKA kod zastupničkog dohvaćanja letova:");
      e.printStackTrace();

      postaviPrazneLetove("Problem kod dohvaćanja letova preko zastupničkog endpointa: "
          + e.getClass().getName() + " - " + e.getMessage(), true, avioTvrtka, idUlaznice);
    }
  }
  private boolean letPripadaAvioTvrtki(Map<String, String> let, String avioTvrtka) {
    if (let == null || avioTvrtka == null || avioTvrtka.isBlank()) {
      return false;
    }

    String oznakaLeta = let.get("oznakaLeta");

    if (oznakaLeta != null && oznakaLeta.startsWith(avioTvrtka + " ")) {
      return true;
    }

    String avioTvrtkaJson = let.get("avioTvrtka");

    return avioTvrtkaJson != null && avioTvrtkaJson.contains("\"id\":\"" + avioTvrtka + "\"");
  }
  
  private void dohvatiPutnike(int stranica, String poruka, boolean pogreska) {
    List<Map<String, String>> putnici = dohvatiPutnikeIzRest(stranica);
    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    for (Map<String, String> putnik : putnici) {
      normalizirajPutnika(putnik);

      String id = putnik.get("korisnik");

      boolean zastupnikBiloKoje = postojiKaoZastupnikBiloKojeAvioTvrtke(id);
      boolean zastupnikIste = atid != null && jeZastupnikAvioTvrtke(atid, id);

      putnik.put("zastupnikBiloKoje", Boolean.toString(zastupnikBiloKoje));
      putnik.put("zastupnikIste", Boolean.toString(zastupnikIste));
    }

    model.put("putnici", putnici);
    model.put("odStranice", stranica);
    model.put("prethodnaStranica", stranica > 1 ? stranica - 1 : 1);
    model.put("sljedecaStranica", stranica + 1);
    model.put("brojRedaka", 7);
    model.put("putnik", this.radneRezervacije.getPutnik());
    model.put("atid", atid);

    if (poruka != null) {
      model.put("poruka", poruka);
      model.put("pogreska", pogreska);
    }
  }

  private List<Map<String, String>> dohvatiPutnikeIzRest(int stranica) {
    try (var odgovor = this.korisniciRestKlijent.dajKorisnikeStraniceno(stranica, 7)) {
      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        return new ArrayList<>();
      }

      String json = odgovor.readEntity(String.class);
      return procitajPolje(json);
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  private void postaviPraznePutnike(String poruka, boolean pogreska, int stranica) {
    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("putnici", new ArrayList<Map<String, String>>());
    model.put("odStranice", stranica);
    model.put("prethodnaStranica", 1);
    model.put("sljedecaStranica", 2);
    model.put("brojRedaka", 7);
    model.put("putnik", this.radneRezervacije.getPutnik());
  }

  private void postaviPrazneLetovePutnika(String poruka, boolean pogreska, String id,
      String idUlaznice, String odDatuma, String doDatuma) {

    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("putnikId", id);
    model.put("idUlaznice", idUlaznice);
    model.put("odDatuma", odDatuma);
    model.put("doDatuma", doDatuma);
    model.put("putnik", this.radneRezervacije.getPutnik());
    model.put("letovi", new ArrayList<Map<String, String>>());
    model.put("atid", dajAvioTvrtkuPrijavljenogZastupnika());
  }

  private void normalizirajPutnika(Map<String, String> putnik) {
    postaviAkoNedostaje(putnik, "korisnik", "id", "username", "korisnickoIme");
    postaviAkoNedostaje(putnik, "ime", "firstName");
    postaviAkoNedostaje(putnik, "prezime", "lastName");
    postaviAkoNedostaje(putnik, "email", "mail");
  }

  private String dajAvioTvrtkuPrijavljenogZastupnika() {
    String korisnik = this.securityContext.getCallerPrincipal().getName();

    try (var odgovor = this.avioTvrtkeRestKlijent.dajSveAvioTvrtke()) {
      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        return null;
      }

      List<Map<String, String>> avioTvrtke = procitajPolje(odgovor.readEntity(String.class));

      for (Map<String, String> avioTvrtka : avioTvrtke) {
        normalizirajAvioTvrtku(avioTvrtka);
        String atid = avioTvrtka.get("atid");

        if (atid != null && jeZastupnikAvioTvrtke(atid, korisnik)) {
          return atid;
        }
      }
    } catch (Exception e) {
      return null;
    }

    return null;
  }

  private boolean postojiKaoZastupnikBiloKojeAvioTvrtke(String korisnik) {
    try (var odgovor = this.avioTvrtkeRestKlijent.dajSveAvioTvrtke()) {
      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        return false;
      }

      List<Map<String, String>> avioTvrtke = procitajPolje(odgovor.readEntity(String.class));

      for (Map<String, String> avioTvrtka : avioTvrtke) {
        normalizirajAvioTvrtku(avioTvrtka);
        String atid = avioTvrtka.get("atid");

        if (atid != null && jeZastupnikAvioTvrtke(atid, korisnik)) {
          return true;
        }
      }
    } catch (Exception e) {
      return false;
    }

    return false;
  }

  private boolean jeZastupnikAvioTvrtke(String atid, String korisnik) {
    if (atid == null || korisnik == null) {
      return false;
    }

    try (var odgovor = this.avioTvrtkeRestKlijent.dajZastupnike(atid)) {
      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        return false;
      }

      List<Map<String, String>> zastupnici = procitajPolje(odgovor.readEntity(String.class));

      for (Map<String, String> zastupnik : zastupnici) {
        normalizirajPutnika(zastupnik);

        if (korisnik.equals(zastupnik.get("korisnik"))) {
          return true;
        }
      }
    } catch (Exception e) {
      return false;
    }

    return false;
  }
  private String procitajTijeloOdgovora(jakarta.ws.rs.core.Response odgovor) {
    try {
      return odgovor.readEntity(String.class);
    } catch (Exception e) {
      return "Nije moguće pročitati tijelo odgovora: "
          + e.getClass().getSimpleName() + " - " + e.getMessage();
    }
  }

  private String prevediGresku(String greska) {
    if (greska == null || greska.isBlank()) {
      return "Nema tijela odgovora.";
    }

    if (greska.startsWith("ERROR 20")) {
      return "ERROR 20 - letovi nisu pronađeni ili nisu inicijalizirani za ovu ulaznicu.";
    }

    if (greska.startsWith("ERROR 21")) {
      return "ERROR 21 - let nije pronađen.";
    }

    if (greska.startsWith("ERROR 22")) {
      return "ERROR 22 - sjedala ili rezervacije leta nisu pronađene.";
    }

    if (greska.startsWith("ERROR 23")) {
      return "ERROR 23 - konflikt kod rezervacije.";
    }

    if (greska.startsWith("ERROR 24")) {
      return "ERROR 24 - traženi podatak nije pronađen.";
    }

    if (greska.startsWith("ERROR 25")) {
      return "ERROR 25 - traženi zapis ne postoji.";
    }

    if (greska.startsWith("ERROR 26")) {
      return "ERROR 26 - konflikt stanja rezervacije.";
    }

    return greska;
  }
  @GET
  @Path("zastupnik/letovi")
  @View("aviotvrtke/letoviZastupnika.jsp")
  public void letoviZastupnika(@QueryParam("idUlaznice") String idUlaznice) {
    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneLetoveZastupnika("Prvo je potrebno odabrati poslužitelj rezervacija.", true,
          idUlaznice);
      return;
    }

    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (atid == null || atid.isBlank()) {
      postaviPrazneLetoveZastupnika("Prijavljeni korisnik nije zastupnik ni jedne avio tvrtke.",
          true, idUlaznice);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      model.put("idUlaznice", idUlaznice);
      model.put("atid", atid);
      model.put("letovi", new ArrayList<Map<String, String>>());
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajLetoveAvioTvrtkeZastupnika(
        this.radneRezervacije.getPosluzitelj(), idUlaznice)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        postaviPrazneLetoveZastupnika("Letovi avio tvrtke nisu dohvaćeni. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora),
            true, idUlaznice);
        return;
      }

      String json = odgovor.readEntity(String.class);
      List<Map<String, String>> letovi = procitajPolje(json);

      for (Map<String, String> let : letovi) {
        normalizirajLet(let);
      }

      model.put("idUlaznice", idUlaznice);
      model.put("atid", atid);
      model.put("letovi", letovi);

    } catch (Exception e) {
      postaviPrazneLetoveZastupnika("Problem kod dohvaćanja letova avio tvrtke: "
          + e.getClass().getName() + " - " + e.getMessage(), true, idUlaznice);
      e.printStackTrace();
    }
  }

  @GET
  @Path("zastupnik/letoviInterval")
  @View("aviotvrtke/letoviInterval.jsp")
  public void letoviAvioTvrtkeNaIntervalu(@QueryParam("oznakaLeta") String oznakaLeta,
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("odDatuma") String odDatuma,
      @QueryParam("doDatuma") String doDatuma) {

    idUlaznice = dajIdUlaznice(idUlaznice);
    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneLetoveIntervala("Prvo je potrebno odabrati poslužitelj rezervacija.",
          true, oznakaLeta, idUlaznice, odDatuma, doDatuma);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      postaviPrazneLetoveIntervala("ID ulaznice nije kreiran.",
          true, oznakaLeta, "", odDatuma, doDatuma);
      return;
    }

    if (atid == null || atid.isBlank()) {
      postaviPrazneLetoveIntervala("Prijavljeni korisnik nije zastupnik ni jedne avio tvrtke.",
          true, oznakaLeta, idUlaznice, odDatuma, doDatuma);
      return;
    }

    if (oznakaLeta == null || oznakaLeta.isBlank()) {
      postaviPrazneLetoveIntervala("Oznaka leta nije zadana.",
          true, oznakaLeta, idUlaznice, odDatuma, doDatuma);
      return;
    }

    if (odDatuma == null || odDatuma.isBlank()
        || doDatuma == null || doDatuma.isBlank()) {
      prikaziLetoveIntervala(oznakaLeta, idUlaznice, odDatuma, doDatuma, atid,
          new ArrayList<Map<String, String>>());
      return;
    }

    if (!ispravanDatum(odDatuma) || !ispravanDatum(doDatuma)) {
      postaviPrazneLetoveIntervala("Datumi moraju biti u formatu dd.mm.gggg.",
          true, oznakaLeta, idUlaznice, odDatuma, doDatuma);
      return;
    }

    String odDatumaZaMikroservis = pretvoriDatumZaMikroservis(odDatuma);
    String doDatumaZaMikroservis = pretvoriDatumZaMikroservis(doDatuma);

    if (odDatumaZaMikroservis == null || doDatumaZaMikroservis == null) {
      postaviPrazneLetoveIntervala("Datume nije moguće pretvoriti za mikroservis.",
          true, oznakaLeta, idUlaznice, odDatuma, doDatuma);
      return;
    }

    if (!inicijalizirajLetoveIntervala(oznakaLeta, idUlaznice,
        odDatumaZaMikroservis, doDatumaZaMikroservis)) {
      postaviPrazneLetoveIntervala("Stvarni letovi nisu inicijalizirani za zadani interval.",
          true, oznakaLeta, idUlaznice, odDatuma, doDatuma);
      return;
    }

    dohvatiIPrikaziLetoveIntervala(oznakaLeta, idUlaznice, odDatuma, doDatuma,
        odDatumaZaMikroservis, doDatumaZaMikroservis, atid);
  }
  private boolean inicijalizirajLetoveIntervala(String oznakaLeta, String idUlaznice,
      String odDatuma, String doDatuma) {

    try (var odgovor = this.rezervacijeRestKlijent.inicijalizirajLetAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        odDatuma,
        doDatuma,
        "")) {

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      System.out.println("INIT interval status = " + odgovor.getStatus());
      System.out.println("INIT interval tijelo = " + tijeloOdgovora);

      return odgovor.getStatus() == Status.OK.getStatusCode()
          || odgovor.getStatus() == Status.CREATED.getStatusCode()
          || odgovor.getStatus() == Status.NO_CONTENT.getStatusCode();

    } catch (Exception e) {
      System.out.println("Problem kod inicijalizacije intervala: "
          + e.getClass().getName() + " - " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
  private void dohvatiIPrikaziLetoveIntervala(String oznakaLeta, String idUlaznice,
      String odDatuma, String doDatuma, String odDatumaZaMikroservis,
      String doDatumaZaMikroservis, String atid) {

    try (var odgovor = this.rezervacijeRestKlijent.dajLetoveAvioTvrtkeNaIntervalu(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        odDatumaZaMikroservis,
        doDatumaZaMikroservis)) {

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      System.out.println("DOHVAT interval status = " + odgovor.getStatus());
      System.out.println("DOHVAT interval tijelo = " + tijeloOdgovora);

      if (odgovor.getStatus() == Status.OK.getStatusCode()) {
        List<Map<String, String>> letovi = procitajPolje(tijeloOdgovora);

        for (Map<String, String> let : letovi) {
          normalizirajLet(let);
        }

        prikaziLetoveIntervala(oznakaLeta, idUlaznice, odDatuma, doDatuma, atid, letovi);
        return;
      }

      if (tijeloOdgovora != null && tijeloOdgovora.startsWith("ERROR 20")) {
        LocalDate od = LocalDate.parse(odDatumaZaMikroservis);
        LocalDate doD = LocalDate.parse(doDatumaZaMikroservis);

        List<Map<String, String>> letovi = dohvatiStvarneLetoveDanPoDan(
            oznakaLeta, idUlaznice, od, doD);

        if (!letovi.isEmpty()) {
       //   model.put("poruka", "Stvarni letovi su dohvaćeni dan po dan jer intervalni dohvat nije vratio podatke.");
          model.put("pogreska", false);
          prikaziLetoveIntervala(oznakaLeta, idUlaznice, odDatuma, doDatuma, atid, letovi);
          return;
        }
      }

      postaviPrazneLetoveIntervala("Stvarni letovi nisu dohvaćeni. Status: "
          + odgovor.getStatus() + ". Odgovor mikroservisa: "
          + prevediGresku(tijeloOdgovora), true, oznakaLeta, idUlaznice, odDatuma,
          doDatuma);

    } catch (Exception e) {
      postaviPrazneLetoveIntervala("Problem kod dohvaćanja stvarnih letova: "
          + e.getClass().getName() + " - " + e.getMessage(), true, oznakaLeta,
          idUlaznice, odDatuma, doDatuma);
      e.printStackTrace();
    }
  }
  private void postaviPrazneLetoveZastupnika(String poruka, boolean pogreska,
      String idUlaznice) {

    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("idUlaznice", idUlaznice);
    model.put("atid", dajAvioTvrtkuPrijavljenogZastupnika());
    model.put("letovi", new ArrayList<Map<String, String>>());
  }
 
  private List<Map<String, String>> dohvatiStvarneLetoveDanPoDan(String oznakaLeta,
      String idUlaznice, LocalDate od, LocalDate doD) {

    List<Map<String, String>> letovi = new ArrayList<>();
    DateTimeFormatter prikazFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter mikroFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    LocalDate trenutni = od;

    while (!trenutni.isAfter(doD)) {
      String datumZaPrikaz = trenutni.format(prikazFormat);
      String datumZaMikroservis = trenutni.format(mikroFormat);

      try {
        this.inicijalizirajLetoveIntervala(
            oznakaLeta,
            idUlaznice,
            datumZaMikroservis,
            datumZaMikroservis);

        try (var odgovor = this.rezervacijeRestKlijent.dajLetNaDatum(
            this.radneRezervacije.getPosluzitelj(),
            idUlaznice,
            oznakaLeta,
            datumZaMikroservis)) {

          String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

          System.out.println("DAN PO DAN datum = " + datumZaMikroservis);
          System.out.println("DAN PO DAN status = " + odgovor.getStatus());
          System.out.println("DAN PO DAN tijelo = " + tijeloOdgovora);

          if (odgovor.getStatus() == Status.OK.getStatusCode()) {
            if (tijeloOdgovora != null && tijeloOdgovora.trim().startsWith("OK")) {
              Map<String, String> let = napraviLetIzOkOdgovora(
                  tijeloOdgovora, oznakaLeta, datumZaPrikaz);
              normalizirajLet(let);

              if (let.get("idLeta") != null && !let.get("idLeta").isBlank()) {
                letovi.add(let);
              }
            } else {
              List<Map<String, String>> procitaniLetovi = procitajPolje(tijeloOdgovora);

              for (Map<String, String> let : procitaniLetovi) {
                normalizirajLet(let);

                if (let.get("idLeta") != null && !let.get("idLeta").isBlank()) {
                  letovi.add(let);
                }
              }
            }
          }
        }

      } catch (Exception e) {
        System.out.println("Nema leta za datum " + datumZaMikroservis + ": "
            + e.getClass().getName() + " - " + e.getMessage());
      }

      trenutni = trenutni.plusDays(1);
    }

    return letovi;
  }
  private String dekodirajOznakuLeta(String oznakaLeta) {
    if (oznakaLeta == null) {
      return null;
    }

    return oznakaLeta.replace("+", " ").trim();
  }
  private void prikaziLetoveIntervala(String oznakaLeta, String idUlaznice,
      String odDatuma, String doDatuma, String atid, List<Map<String, String>> letovi) {

    model.put("oznakaLeta", oznakaLeta);
    model.put("idUlaznice", idUlaznice);
    model.put("odDatuma", odDatuma);
    model.put("doDatuma", doDatuma);
    model.put("atid", atid);
    model.put("letovi", letovi);
  }
  private void postaviPrazneLetoveIntervala(String poruka, boolean pogreska,
      String oznakaLeta, String idUlaznice, String odDatuma, String doDatuma) {

    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("oznakaLeta", oznakaLeta);
    model.put("idUlaznice", idUlaznice);
    model.put("odDatuma", odDatuma);
    model.put("doDatuma", doDatuma);
    model.put("atid", dajAvioTvrtkuPrijavljenogZastupnika());
    model.put("letovi", new ArrayList<Map<String, String>>());
  }
  @GET
  @Path("zastupnik/sjedala/{idLeta}")
  @View("aviotvrtke/sjedalaZastupnika.jsp")
  public void sjedalaLetaZastupnika(@PathParam("idLeta") String idLeta,
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("oznakaLeta") String oznakaLeta) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPraznaSjedalaZastupnika("Prvo je potrebno odabrati poslužitelj rezervacija.",
          true, idLeta, idUlaznice, oznakaLeta);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      postaviPraznaSjedalaZastupnika("ID ulaznice nije upisan.",
          true, idLeta, idUlaznice, oznakaLeta);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajSjedalaLetaAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(), idLeta, idUlaznice)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

        postaviPraznaSjedalaZastupnika("Sjedala nisu dohvaćena. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijeloOdgovora),
            true, idLeta, idUlaznice, oznakaLeta);
        return;
      }

      String json = odgovor.readEntity(String.class);
      List<Map<String, String>> sjedala = procitajSjedala(json);

      model.put("idLeta", idLeta);
      model.put("idUlaznice", idUlaznice);
      model.put("oznakaLeta", oznakaLeta);
      model.put("sjedala", sjedala);
      model.put("putnik", this.radneRezervacije.getPutnik());
      model.put("imaOtvorenRacun", imaOtvorenRacun());

    } catch (Exception e) {
      postaviPraznaSjedalaZastupnika("Problem kod dohvaćanja sjedala: "
          + e.getClass().getName() + " - " + e.getMessage(),
          true, idLeta, idUlaznice, oznakaLeta);
      e.printStackTrace();
    }
  }
  private void postaviPraznaSjedalaZastupnika(String poruka, boolean pogreska,
      String idLeta, String idUlaznice, String oznakaLeta) {

    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("idLeta", idLeta);
    model.put("idUlaznice", idUlaznice);
    model.put("oznakaLeta", oznakaLeta);
    model.put("sjedala", new ArrayList<Map<String, String>>());
    model.put("putnik", this.radneRezervacije.getPutnik());
    model.put("imaOtvorenRacun", imaOtvorenRacun());
  }
  private boolean kreirajUlaznicuAkoNePostoji() {
    if (this.radneRezervacije.imaUlaznicu()) {
      return true;
    }

    String korisnik = this.securityContext.getCallerPrincipal().getName();

    try (var odgovor = this.kontrolaRestKlijent.dodajUlaznicu(korisnik, "")) {
      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      if (odgovor.getStatus() != Status.CREATED.getStatusCode()
          && odgovor.getStatus() != Status.OK.getStatusCode()) {

        model.put("poruka", "Ulaznica nije kreirana. URL provjeri u KontrolaRestKlijent. "
            + "Status: " + odgovor.getStatus()
            + ". Odgovor kontrole: " + tijeloOdgovora
            + ". Korisnik: " + korisnik);
        model.put("pogreska", true);
        return false;
      }

      String idUlaznice = izvuciIdUlaznice(tijeloOdgovora);

      if (idUlaznice == null || idUlaznice.isBlank()) {
        model.put("poruka", "Ulaznica je kreirana, ali ID nije pročitan. Odgovor: "
            + tijeloOdgovora);
        model.put("pogreska", true);
        return false;
      }

      this.radneRezervacije.setIdUlaznice(idUlaznice);
      return true;

    } catch (Exception e) {
      model.put("poruka", "Problem kod kreiranja ulaznice: "
          + e.getClass().getName() + " - " + e.getMessage());
      model.put("pogreska", true);
      e.printStackTrace();
      return false;
    }
  }
  private String izvuciIdUlaznice(String odgovor) {
    if (odgovor == null || odgovor.isBlank()) {
      return null;
    }

    String ocisceniOdgovor = odgovor.trim();

    if (ocisceniOdgovor.startsWith("OK")) {
      String ostatak = ocisceniOdgovor.replaceFirst("^OK", "").trim();

      if (!ostatak.isBlank()) {
        String[] dijelovi = ostatak.split("\\s+");
        return dijelovi[0];
      }
    }

    return ocisceniOdgovor;
  }
  private String dajIdUlaznice() {
    if (!this.radneRezervacije.imaUlaznicu()) {
      kreirajUlaznicuAkoNePostoji();
    }

    return this.radneRezervacije.getIdUlaznice();
  }

  private String dajIdUlaznice(String idUlaznice) {
    if (this.radneRezervacije.imaUlaznicu()) {
      return this.radneRezervacije.getIdUlaznice();
    }

    if (idUlaznice != null && !idUlaznice.isBlank()) {
      this.radneRezervacije.setIdUlaznice(idUlaznice.trim());
      return idUlaznice.trim();
    }

    if (!kreirajUlaznicuAkoNePostoji()) {
      return "";
    }

    return this.radneRezervacije.getIdUlaznice();
  }
  @GET
  @Path("zastupnik/inicijalizirajLet")
  @View("aviotvrtke/letoviZastupnika.jsp")
  public void inicijalizirajLetZastupnika(@QueryParam("oznakaLeta") String oznakaLeta,
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("odDatuma") String odDatuma,
      @QueryParam("doDatuma") String doDatuma) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneLetoveZastupnika("Prvo je potrebno odabrati poslužitelj rezervacija.",
          true, idUlaznice);
      return;
    }

    if (oznakaLeta == null || oznakaLeta.isBlank()) {
      postaviPrazneLetoveZastupnika("Oznaka leta nije zadana.", true, idUlaznice);
      return;
    }

    if (odDatuma == null || odDatuma.isBlank()
        || doDatuma == null || doDatuma.isBlank()) {
      postaviPrazneLetoveZastupnika("Potrebno je upisati datume za inicijalizaciju.",
          true, idUlaznice);
      return;
    }

    if (!ispravanDatum(odDatuma) || !ispravanDatum(doDatuma)) {
      postaviPrazneLetoveZastupnika("Datumi moraju biti u formatu dd.mm.gggg.",
          true, idUlaznice);
      return;
    }

    String odDatumaZaMikroservis = pretvoriDatumZaMikroservis(odDatuma);
    String doDatumaZaMikroservis = pretvoriDatumZaMikroservis(doDatuma);

    try (var odgovor = this.rezervacijeRestKlijent.inicijalizirajLetAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        odDatumaZaMikroservis,
        doDatumaZaMikroservis,
        "")) {

      String tijelo = procitajTijeloOdgovora(odgovor);

      if (odgovor.getStatus() != Status.OK.getStatusCode()
          && odgovor.getStatus() != Status.CREATED.getStatusCode()
          && odgovor.getStatus() != Status.NO_CONTENT.getStatusCode()) {

        postaviPrazneLetoveZastupnika("Let nije inicijaliziran. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijelo),
            true, idUlaznice);
        return;
      }

      pripremiLetoveZastupnika("Let je inicijaliziran za zadani interval.", false, idUlaznice);

    } catch (Exception e) {
      postaviPrazneLetoveZastupnika("Problem kod inicijalizacije leta: "
          + e.getClass().getName() + " - " + e.getMessage(), true, idUlaznice);
      e.printStackTrace();
    }
  }
  @GET
  @Path("zastupnik/letNaDatum")
  @View("aviotvrtke/letZastupnikaNaDatum.jsp")
  public void letZastupnikaNaDatum(@QueryParam("oznakaLeta") String oznakaLeta,
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("datum") String datum) {

    idUlaznice = dajIdUlaznice(idUlaznice);
    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazanLetZastupnikaNaDatum("Prvo je potrebno odabrati poslužitelj rezervacija.",
          true, oznakaLeta, idUlaznice, datum, atid);
      return;
    }

    if (oznakaLeta == null || oznakaLeta.isBlank()) {
      postaviPrazanLetZastupnikaNaDatum("Oznaka leta nije zadana.",
          true, oznakaLeta, idUlaznice, datum, atid);
      return;
    }

    if (datum == null || datum.isBlank()) {
      prikaziLetZastupnikaNaDatum(oznakaLeta, idUlaznice, datum, atid,
          new ArrayList<Map<String, String>>());
      return;
    }

    if (!ispravanDatum(datum)) {
      postaviPrazanLetZastupnikaNaDatum("Datum mora biti u formatu dd.mm.gggg.",
          true, oznakaLeta, idUlaznice, datum, atid);
      return;
    }

    String datumZaMikroservis = pretvoriDatumZaMikroservis(datum);

    try (var odgovor = this.rezervacijeRestKlijent.dajLetAvioTvrtkeNaDatum(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        datumZaMikroservis)) {

      String tijelo = procitajTijeloOdgovora(odgovor);

      System.out.println("LET ZASTUPNIKA NA DATUM status = " + odgovor.getStatus());
      System.out.println("LET ZASTUPNIKA NA DATUM tijelo = " + tijelo);

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        postaviPrazanLetZastupnikaNaDatum("Let nije dohvaćen. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijelo),
            true, oznakaLeta, idUlaznice, datum, atid);
        return;
      }

      List<Map<String, String>> letovi = new ArrayList<>();

      if (tijelo != null && tijelo.trim().startsWith("OK")) {
        Map<String, String> let = napraviLetIzOkOdgovora(tijelo, oznakaLeta, datum);
        letovi.add(let);
      } else {
        letovi = procitajPolje(tijelo);

        for (Map<String, String> let : letovi) {
          normalizirajLet(let);
        }
      }

      for (Map<String, String> let : letovi) {
        String idLeta = let.get("idLeta");

        if (idLeta != null && !idLeta.isBlank()) {
          Map<String, String> stanje = dohvatiStanjeSjedala(idLeta, idUlaznice);

          let.put("slobodna", vrijednostStanja(stanje, "slobodna", "slobodno", "SLOBODNA"));
          let.put("rezervirana", vrijednostStanja(stanje, "rezervirana", "rezervirano", "REZERVIRANA"));
          let.put("potvrdjena", vrijednostStanja(stanje, "potvrdjena", "potvrđena", "POTVRDJENA", "POTVRĐENA"));
          let.put("zatvorena", vrijednostStanja(stanje, "zatvorena", "zatvoreno", "ZATVORENA"));
          let.put("nevazeca", vrijednostStanja(stanje, "nevazeca", "nevažeća", "NEVAZECA", "NEVAŽEĆA"));
        }
      }

      prikaziLetZastupnikaNaDatum(oznakaLeta, idUlaznice, datum, atid, letovi);

    } catch (Exception e) {
      postaviPrazanLetZastupnikaNaDatum("Problem kod dohvaćanja leta na datum: "
          + e.getClass().getName() + " - " + e.getMessage(),
          true, oznakaLeta, idUlaznice, datum, atid);
      e.printStackTrace();
    }
  }
  @GET
  @Path("zastupnik/zatvoriSjedala/{idLeta}")
  @View("aviotvrtke/letZastupnikaNaDatum.jsp")
  public void zatvoriSjedalaZastupnika(@PathParam("idLeta") String idLeta,
      @QueryParam("oznakaLeta") String oznakaLeta,
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("datum") String datum,
      @QueryParam("odReda") String odReda,
      @QueryParam("doReda") String doReda) {

    idUlaznice = dajIdUlaznice(idUlaznice);
    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazanLetZastupnikaNaDatum("Prvo je potrebno odabrati poslužitelj rezervacija.",
          true, oznakaLeta, idUlaznice, datum, atid);
      return;
    }

    if (idLeta == null || idLeta.isBlank()) {
      postaviPrazanLetZastupnikaNaDatum("ID leta nije zadan.",
          true, oznakaLeta, idUlaznice, datum, atid);
      return;
    }

    if (odReda == null || odReda.isBlank()
        || doReda == null || doReda.isBlank()) {
      postaviPrazanLetZastupnikaNaDatum("Potrebno je upisati interval redova.",
          true, oznakaLeta, idUlaznice, datum, atid);
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.zatvoriSjedalaAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(),
        idLeta,
        idUlaznice,
        odReda,
        doReda,
        "")) {

      String tijelo = procitajTijeloOdgovora(odgovor);

      if (odgovor.getStatus() != Status.OK.getStatusCode()
          && odgovor.getStatus() != Status.NO_CONTENT.getStatusCode()) {

        postaviPrazanLetZastupnikaNaDatum("Sjedala nisu zatvorena. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijelo),
            true, oznakaLeta, idUlaznice, datum, atid);
        return;
      }

      model.put("poruka", "Sjedala su zatvorena za redove " + odReda + " - " + doReda + ".");
      model.put("pogreska", false);

      letZastupnikaNaDatum(oznakaLeta, idUlaznice, datum);

    } catch (Exception e) {
      postaviPrazanLetZastupnikaNaDatum("Problem kod zatvaranja sjedala: "
          + e.getClass().getName() + " - " + e.getMessage(),
          true, oznakaLeta, idUlaznice, datum, atid);
      e.printStackTrace();
    }
  }
  private Map<String, String> dohvatiStanjeSjedala(String idLeta, String idUlaznice) {
    Map<String, String> stanje = new LinkedHashMap<>();

    stanje.put("slobodna", "0");
    stanje.put("rezervirana", "0");
    stanje.put("potvrdjena", "0");
    stanje.put("zatvorena", "0");
    stanje.put("nevazeca", "0");

    try (var odgovor = this.rezervacijeRestKlijent.dajStanjeSjedalaLetaAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(),
        idLeta,
        idUlaznice)) {

      String tijelo = procitajTijeloOdgovora(odgovor);

      System.out.println("STANJE SJEDALA status = " + odgovor.getStatus());
      System.out.println("STANJE SJEDALA tijelo = " + tijelo);

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        return stanje;
      }

      if (tijelo == null || tijelo.isBlank()) {
        return stanje;
      }

      String ocisceno = tijelo.trim();

      if (ocisceno.startsWith("OK")) {
        ocisceno = ocisceno.replaceFirst("^OK", "").trim();
      }

      String[] dijelovi = ocisceno.split("\\s+");

      if (dijelovi.length >= 5) {
        stanje.put("slobodna", dijelovi[0]);
        stanje.put("rezervirana", dijelovi[1]);
        stanje.put("potvrdjena", dijelovi[2]);
        stanje.put("zatvorena", dijelovi[3]);
        stanje.put("nevazeca", dijelovi[4]);
      }

    } catch (Exception e) {
      System.out.println("Problem kod dohvaćanja stanja sjedala.");
      e.printStackTrace();
    }

    return stanje;
  }
  private Map<String, String> procitajJsonObjektKaoMapu(String json) {
    Map<String, String> mapa = new LinkedHashMap<>();

    if (json == null || json.isBlank()) {
      return mapa;
    }

    try (var reader = Json.createReader(new StringReader(json))) {
      JsonObject objekt = reader.readObject();

      for (String kljuc : objekt.keySet()) {
        mapa.put(kljuc, objekt.get(kljuc).toString().replace("\"", ""));
      }
    } catch (Exception e) {
      System.out.println("Ne mogu pročitati JSON objekt: " + json);
    }

    return mapa;
  }
  private void pripremiLetoveZastupnika(String poruka, boolean pogreska, String idUlaznice) {
    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    model.put("idUlaznice", idUlaznice);
    model.put("atid", atid);

    if (poruka != null) {
      model.put("poruka", poruka);
      model.put("pogreska", pogreska);
    }

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      model.put("letovi", new ArrayList<Map<String, String>>());
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      model.put("letovi", new ArrayList<Map<String, String>>());
      return;
    }

    try (var odgovor = this.rezervacijeRestKlijent.dajLetoveAvioTvrtkeZastupnika(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice)) {

      String tijeloOdgovora = procitajTijeloOdgovora(odgovor);

      System.out.println("PRIPREMI LETOVE ZASTUPNIKA status = " + odgovor.getStatus());
      System.out.println("PRIPREMI LETOVE ZASTUPNIKA tijelo = " + tijeloOdgovora);

      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        model.put("poruka", "Letovi avio tvrtke nisu dohvaćeni. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: "
            + prevediGresku(tijeloOdgovora));
        model.put("pogreska", true);
        model.put("letovi", new ArrayList<Map<String, String>>());
        return;
      }

      List<Map<String, String>> letovi = procitajPolje(tijeloOdgovora);

      for (Map<String, String> let : letovi) {
        normalizirajLet(let);
      }

      model.put("letovi", letovi);

    } catch (Exception e) {
      model.put("poruka", "Problem kod pripreme letova zastupnika: "
          + e.getClass().getName() + " - " + e.getMessage());
      model.put("pogreska", true);
      model.put("letovi", new ArrayList<Map<String, String>>());
      e.printStackTrace();
    }
  }
  private String vrijednostStanja(Map<String, String> stanje, String... kljucevi) {
    if (stanje == null || kljucevi == null) {
      return "0";
    }

    for (String kljuc : kljucevi) {
      if (stanje.get(kljuc) != null) {
        return stanje.get(kljuc);
      }
    }

    return "0";
  }
  private void prikaziLetZastupnikaNaDatum(String oznakaLeta, String idUlaznice,
      String datum, String atid, List<Map<String, String>> letovi) {

    model.put("oznakaLeta", oznakaLeta);
    model.put("idUlaznice", idUlaznice);
    model.put("datum", datum);
    model.put("atid", atid);
    model.put("letovi", letovi);
  }
  private void postaviPrazanLetZastupnikaNaDatum(String poruka, boolean pogreska,
      String oznakaLeta, String idUlaznice, String datum, String atid) {

    model.put("poruka", poruka);
    model.put("pogreska", pogreska);
    model.put("oznakaLeta", oznakaLeta);
    model.put("idUlaznice", idUlaznice);
    model.put("datum", datum);
    model.put("atid", atid);
    model.put("letovi", new ArrayList<Map<String, String>>());
  }
  @GET
  @Path("zastupnik/zatvoriSjedalaInterval")
  @View("aviotvrtke/letoviZastupnika.jsp")
  public void zatvoriSjedalaZastupnikaUIntervalu(
      @QueryParam("oznakaLeta") String oznakaLeta,
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("odDatuma") String odDatuma,
      @QueryParam("doDatuma") String doDatuma,
      @QueryParam("odReda") String odReda,
      @QueryParam("doReda") String doReda) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneLetoveZastupnika("Prvo je potrebno odabrati poslužitelj rezervacija.",
          true, idUlaznice);
      return;
    }

    if (oznakaLeta == null || oznakaLeta.isBlank()) {
      postaviPrazneLetoveZastupnika("Oznaka leta nije zadana.", true, idUlaznice);
      return;
    }

    if (odDatuma == null || odDatuma.isBlank()
        || doDatuma == null || doDatuma.isBlank()) {
      postaviPrazneLetoveZastupnika("Potrebno je upisati interval datuma.",
          true, idUlaznice);
      return;
    }

    if (odReda == null || odReda.isBlank()
        || doReda == null || doReda.isBlank()) {
      postaviPrazneLetoveZastupnika("Potrebno je upisati interval redova.",
          true, idUlaznice);
      return;
    }

    if (!ispravanDatum(odDatuma) || !ispravanDatum(doDatuma)) {
      postaviPrazneLetoveZastupnika("Datumi moraju biti u formatu dd.mm.gggg.",
          true, idUlaznice);
      return;
    }

    String odDatumaZaMikroservis = pretvoriDatumZaMikroservis(odDatuma);
    String doDatumaZaMikroservis = pretvoriDatumZaMikroservis(doDatuma);

    try (var odgovor = this.rezervacijeRestKlijent.zatvoriSjedalaAvioTvrtkeUIntervalu(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        oznakaLeta,
        odDatumaZaMikroservis,
        doDatumaZaMikroservis,
        odReda,
        doReda,
        "")) {

      String tijelo = procitajTijeloOdgovora(odgovor);

      System.out.println("ZATVARANJE SJEDALA INTERVAL status = " + odgovor.getStatus());
      System.out.println("ZATVARANJE SJEDALA INTERVAL tijelo = " + tijelo);

      if (odgovor.getStatus() != Status.OK.getStatusCode()
          && odgovor.getStatus() != Status.NO_CONTENT.getStatusCode()) {

        postaviPrazneLetoveZastupnika("Sjedala nisu zatvorena. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijelo),
            true, idUlaznice);
        return;
      }
      WebSocketAvioTvrtke.send("ZATVARANJE," + oznakaLeta + "," + odDatuma + ","
          + doDatuma + "," + odReda + "-" + doReda);

      pripremiLetoveZastupnika("Sjedala su zatvorena za let " + oznakaLeta
          + " u intervalu datuma " + odDatuma + " - " + doDatuma
          + " i redova " + odReda + " - " + doReda + ".", false, idUlaznice);

    } catch (Exception e) {
      postaviPrazneLetoveZastupnika("Problem kod zatvaranja sjedala u intervalu: "
          + e.getClass().getName() + " - " + e.getMessage(), true, idUlaznice);
      e.printStackTrace();
    }
  }
  @GET
  @Path("zastupnik/inicijalizirajLetove")
  @View("aviotvrtke/letoviZastupnika.jsp")
  public void inicijalizirajLetoveZastupnika(
      @QueryParam("idUlaznice") String idUlaznice,
      @QueryParam("odDatuma") String odDatuma,
      @QueryParam("doDatuma") String doDatuma) {

    idUlaznice = dajIdUlaznice(idUlaznice);

    if (!this.radneRezervacije.jeOdabranPosluzitelj()) {
      postaviPrazneLetoveZastupnika("Prvo je potrebno odabrati poslužitelj rezervacija.",
          true, idUlaznice);
      return;
    }

    String atid = dajAvioTvrtkuPrijavljenogZastupnika();

    if (atid == null || atid.isBlank()) {
      postaviPrazneLetoveZastupnika("Prijavljeni korisnik nije zastupnik ni jedne avio tvrtke.",
          true, idUlaznice);
      return;
    }

    if (idUlaznice == null || idUlaznice.isBlank()) {
      postaviPrazneLetoveZastupnika("ID ulaznice nije kreiran.", true, idUlaznice);
      return;
    }

    if (odDatuma == null || odDatuma.isBlank()
        || doDatuma == null || doDatuma.isBlank()) {
      postaviPrazneLetoveZastupnika("Potrebno je upisati interval datuma.",
          true, idUlaznice);
      return;
    }

    if (!ispravanDatum(odDatuma) || !ispravanDatum(doDatuma)) {
      postaviPrazneLetoveZastupnika("Datumi moraju biti u formatu dd.mm.gggg.",
          true, idUlaznice);
      return;
    }

    String odDatumaZaMikroservis = pretvoriDatumZaMikroservis(odDatuma);
    String doDatumaZaMikroservis = pretvoriDatumZaMikroservis(doDatuma);

    try (var odgovor = this.rezervacijeRestKlijent.inicijalizirajLetoveAvioTvrtke(
        this.radneRezervacije.getPosluzitelj(),
        idUlaznice,
        odDatumaZaMikroservis,
        doDatumaZaMikroservis,
        "")) {

      String tijelo = procitajTijeloOdgovora(odgovor);

      System.out.println("INICIJALIZACIJA SVIH LETOVA status = " + odgovor.getStatus());
      System.out.println("INICIJALIZACIJA SVIH LETOVA tijelo = " + tijelo);

      if (odgovor.getStatus() != Status.OK.getStatusCode()
          && odgovor.getStatus() != Status.CREATED.getStatusCode()
          && odgovor.getStatus() != Status.NO_CONTENT.getStatusCode()) {

        postaviPrazneLetoveZastupnika("Letovi nisu inicijalizirani. Status: "
            + odgovor.getStatus() + ". Odgovor mikroservisa: " + prevediGresku(tijelo),
            true, idUlaznice);
        return;
      }
      WebSocketAvioTvrtke.send("INICIJALIZACIJA," + atid + "," + odDatuma + ","
          + doDatuma);

      pripremiLetoveZastupnika("Letovi avio tvrtke " + atid
          + " su inicijalizirani za interval " + odDatuma + " - " + doDatuma + ".",
          false, idUlaznice);

    } catch (Exception e) {
      postaviPrazneLetoveZastupnika("Problem kod inicijalizacije letova avio tvrtke: "
          + e.getClass().getName() + " - " + e.getMessage(), true, idUlaznice);
      e.printStackTrace();
    }
  }

}
package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.resource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.KorisniciRestKlijent;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.pomocnici.KlijentMrezneUticnice;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("rezervacije")
public class RezervacijeResource {

  private static final String KONTROLA_REST_URL =
      "http://localhost:8080/api/v2/kontrola/ulaznica/korisnik/";

  private final List<String> adresa = new ArrayList<>();
  private final List<Integer> mreznaVrata = new ArrayList<>();
  private final List<Integer> mreznaVrataAvioKompanije = new ArrayList<>();
  private final List<Integer> mreznaVrataAdministracija = new ArrayList<>();
  private final List<String> kodZaKraj = new ArrayList<>();

  @Inject
  KlijentMrezneUticnice klijentMrezneUticnice;

  @Inject
  KorisniciRestKlijent korisniciRestKlijent;

  @PostConstruct
  private void ucitajConfig() {
    Config config = ConfigProvider.getConfig();

    for (int i = 0;; i++) {
      try {
        this.adresa.add(config.getValue("rezervacije." + i + ".adresa", String.class));
        this.mreznaVrata.add(config.getValue("rezervacije." + i + ".mreznaVrata", Integer.class));
        this.mreznaVrataAvioKompanije
            .add(config.getValue("rezervacije." + i + ".mreznaVrataAvioKompanije", Integer.class));
        this.mreznaVrataAdministracija
            .add(config.getValue("rezervacije." + i + ".mreznaVrataAdministracija", Integer.class));
        this.kodZaKraj.add(config.getValue("rezervacije." + i + ".kodZaKraj", String.class));
      } catch (Exception e) {
        break;
      }
    }
  }

  @Path("{id}/ping")
  @HEAD
  @Operation(summary = "Provjera rada poslužitelja rezervacije slanjem PING komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Poslužitelj radi"),
      @APIResponse(responseCode = "404", description = "Poslužitelj ne vraća OK"),
      @APIResponse(responseCode = "503", description = "Poslužitelj nije dostupan")})
  @Counted(name = "rezervacijeKorisnickiPingCount")
  @Timed(name = "rezervacijeKorisnickiPingTime")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  public Response ping(@PathParam("id") int id) {
    String odgovor = this.posaljiKorisnicku(id, "PING");
    return this.odgovorHead(odgovor, true);
  }

  @Path("{id}/pauza/{milisek}")
  @HEAD
  @Operation(summary = "Pokreće pauzu poslužitelja rezervacije slanjem PAUZA komande")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Pauza pokrenuta"),
      @APIResponse(responseCode = "404", description = "Pauza nije pokrenuta"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "rezervacijePauzaCount")
  @Timed(name = "rezervacijePauzaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response pauza(@PathParam("id") int id, @PathParam("milisek") long milisek) {
    String odgovor = this.posaljiKorisnicku(id, "PAUZA " + milisek);
    return this.odgovorHead(odgovor, false);
  }

  @Path("{id}/{idRezervacijeSjedala}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvaća rezervaciju sjedala prema id rezervacije")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Rezervacija dohvaćena"),
      @APIResponse(responseCode = "404", description = "Rezervacija nije pronađena"),
      @APIResponse(responseCode = "409", description = "Konflikt"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "rezervacijeDohvatiRezervacijuCount")
  @Timed(name = "rezervacijeDohvatiRezervacijuTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response dohvatiRezervaciju(@PathParam("id") int id,
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @HeaderParam("idUlaznice") String idUlaznice) {

    String komanda = "ISPIS " + idUlaznice + " " + idRezervacijeSjedala;
    String odgovor = this.posaljiKorisnicku(id, komanda);

    return this.odgovorOkJsonIliGreska(odgovor);
  }
  @Path("{id}/ispis/letovi/{avioTvrtka}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvaća letove odabrane avio tvrtke")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Letovi dohvaćeni"),
      @APIResponse(responseCode = "404", description = "Letovi nisu pronađeni"),
      @APIResponse(responseCode = "409", description = "Konflikt"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "rezervacijeIspisLetoviCount")
  @Timed(name = "rezervacijeIspisLetoviTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response ispisLetovi(@PathParam("id") int id,
      @PathParam("avioTvrtka") String avioTvrtka,
      @HeaderParam("idUlaznice") String idUlaznice) {

    String komanda = "ISPIS LETOVI " + idUlaznice + " " + avioTvrtka;

    String odgovor = this.posaljiKorisnicku(id, komanda);

    if (odgovor != null && odgovor.startsWith("ERROR 20")) {
      odgovor = this.posaljiAvio(id, komanda);
    }

    return this.odgovorOkJsonIliGreska(odgovor);
  }

  @Path("{id}/ispis/{idLeta}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvaća rezervacije sjedala zadanog leta")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Rezervacije sjedala dohvaćene"),
      @APIResponse(responseCode = "404", description = "Rezervacije nisu pronađene"),
      @APIResponse(responseCode = "409", description = "Konflikt"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "rezervacijeIspisLetaCount")
  @Timed(name = "rezervacijeIspisLetaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response ispisLeta(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice) {

    String komanda = "ISPIS " + idUlaznice + " " + idLeta;

    String odgovor = this.posaljiKorisnicku(id, komanda);

    if (odgovor != null
        && (odgovor.startsWith("ERROR 20")
            || odgovor.startsWith("ERROR 21")
            || odgovor.startsWith("ERROR 22")
            || odgovor.startsWith("ERROR 24")
            || odgovor.startsWith("ERROR 25"))) {
      odgovor = this.posaljiAvio(id, komanda);
    }

    return this.odgovorOkJsonIliGreska(odgovor);
  }

  @Path("{id}/ispis/putnik")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvaća rezervacije putnika u zadanom razdoblju")
  @Counted(name = "rezervacijeIspisPutnikCount")
  @Timed(name = "rezervacijeIspisPutnikTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response ispisPutnik(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {

    String komanda = "ISPIS " + idUlaznice + " " + putnik + " " + odDatuma + " " + doDatuma;
    String odgovor = this.posaljiKorisnicku(id, komanda);

    return this.odgovorOkJsonIliGreska(odgovor);
  }

  @Path("{id}/ispis/let")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Dohvaća id leta prema oznaci leta i datumu")
  @Counted(name = "rezervacijeIspisLetCount")
  @Timed(name = "rezervacijeIspisLetTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response ispisLet(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("datum") String datum) {

    String komanda = "ISPIS " + idUlaznice + " \"" + oznakaLeta + "\" " + datum;
    String odgovor = this.posaljiKorisnicku(id, komanda);

    return this.odgovorOkTextIliGreska(odgovor);
  }
  @Path("{id}/rezerviraj")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Rezervira sjedalo za putnika")
  @Counted(name = "rezervacijeRezervirajCount")
  @Timed(name = "rezervacijeRezervirajTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response rezerviraj(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("idLeta") String idLeta,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("razred") String razred) {

    String komanda = "REZERVIRAJ " + idUlaznice + " " + idLeta + " " + putnik + " " + razred;

    String odgovor = this.posaljiKorisnicku(id, komanda);

    if (odgovor != null
        && (odgovor.startsWith("ERROR 20")
            || odgovor.startsWith("ERROR 21")
            || odgovor.startsWith("ERROR 22")
            || odgovor.startsWith("ERROR 24")
            || odgovor.startsWith("ERROR 25")
            || odgovor.startsWith("ERROR 27"))) {
      odgovor = this.posaljiAvio(id, komanda);
    }

    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.status(Status.CREATED).entity(odgovor).build();
    }

    return this.odgovorGreska(odgovor);
  }
  @Path("{id}/{idRezervacijeSjedala}")
  @PUT
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Potvrđuje rezervaciju sjedala")
  @Counted(name = "rezervacijePotvrdiCount")
  @Timed(name = "rezervacijePotvrdiTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response potvrdi(@PathParam("id") int id,
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik) {

    String komanda = "POTVRDI " + idUlaznice + " " + idRezervacijeSjedala + " " + putnik;
    String odgovor = this.posaljiKorisnicku(id, komanda);

    return this.odgovorOkIliGreska(odgovor);
  }

  @Path("{id}/rezerviraj/grupa")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Rezervira sjedala za grupu putnika")
  @Counted(name = "rezervacijeRezervirajGrupaCount")
  @Timed(name = "rezervacijeRezervirajGrupaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response rezervirajGrupa(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("idLeta") String idLeta,
      @HeaderParam("putnici") String putnici,
      @HeaderParam("razred") String razred) {

    String komanda = "REZERVIRAJ GRUPA " + idUlaznice + " " + idLeta
        + " [" + putnici.trim() + "] " + razred;
    String odgovor = this.posaljiKorisnicku(id, komanda);

    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.status(Status.CREATED).entity(odgovor).build();
    }

    return this.odgovorGreska(odgovor);
  }

  @Path("{id}/potvrdi/grupa")
  @PUT
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Potvrđuje rezervacije sjedala za grupu putnika")
  @Counted(name = "rezervacijePotvrdiGrupaCount")
  @Timed(name = "rezervacijePotvrdiGrupaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response potvrdiGrupa(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("rezervacijeSjedala") String rezervacijeSjedala,
      @HeaderParam("putnici") String putnici) {

    String[] rezervacije = rezervacijeSjedala.trim().split("\\s+");
    String[] emailovi = putnici.trim().split("\\s+");

    if (rezervacije.length != emailovi.length) {
      return Response.status(Status.CONFLICT).build();
    }

    for (int i = 0; i < rezervacije.length; i++) {
      String komanda = "POTVRDI " + idUlaznice + " " + rezervacije[i] + " " + emailovi[i];
      String odgovor = this.posaljiKorisnicku(id, komanda);

      if (odgovor == null || !odgovor.startsWith("OK")) {
        return this.odgovorGreska(odgovor);
      }
    }

    return Response.ok().build();
  }

  @Path("aviotvrtke/{id}/ping")
  @HEAD
  @Operation(summary = "Provjera rada avio dijela poslužitelja rezervacije")
  @Counted(name = "rezervacijeAvioPingCount")
  @Timed(name = "rezervacijeAvioPingTime")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  public Response avioPing(@PathParam("id") int id) {
    String odgovor = this.posaljiAvio(id, "PING");
    return this.odgovorHead(odgovor, true);
  }

  @Path("aviotvrtke/{id}/ispis/{idLeta}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvaća rezervacije leta prema id leta za zastupnika avio tvrtke")
  @Counted(name = "rezervacijeAvioIspisLetaCount")
  @Timed(name = "rezervacijeAvioIspisLetaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioIspisLeta(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda = "ISPIS " + idUlaznice + " " + idLeta;
    String odgovor = this.posaljiAvio(id, komanda);

    return this.odgovorOkJsonIliGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/ispis/let")
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  @Operation(summary = "Dohvaća stvarne letove prema oznaci leta i datumu ili intervalu za zastupnika avio tvrtke")
  @Counted(name = "rezervacijeAvioIspisLetCount")
  @Timed(name = "rezervacijeAvioIspisLetTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioIspisLet(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("datum") String datum,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda;

    if (odDatuma != null && !odDatuma.isBlank()
        && doDatuma != null && !doDatuma.isBlank()) {

      komanda = "ISPIS " + idUlaznice + " " + avioTvrtka
          + " \"" + oznakaLeta + "\" " + odDatuma + " " + doDatuma;

      String odgovor = this.posaljiAvio(id, komanda);
      return this.odgovorOkJsonIliGreska(odgovor);
    }

    komanda = "ISPIS " + idUlaznice + " \"" + oznakaLeta + "\" " + datum;
    String odgovor = this.posaljiAvio(id, komanda);

    return this.odgovorOkTextIliGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/ispis/putnik")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvaća rezervacije putnika za avio tvrtku zastupnika")
  @Counted(name = "rezervacijeAvioIspisPutnikCount")
  @Timed(name = "rezervacijeAvioIspisPutnikTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioIspisPutnik(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda = "ISPIS " + idUlaznice + " " + avioTvrtka + " "
        + putnik + " " + odDatuma + " " + doDatuma;
    String odgovor = this.posaljiAvio(id, komanda);

    return this.odgovorOkJsonIliGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/ispis/letovi")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dohvaća letove avio tvrtke zastupnika")
  @Counted(name = "rezervacijeAvioIspisLetoviCount")
  @Timed(name = "rezervacijeAvioIspisLetoviTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioIspisLetovi(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda = "ISPIS LETOVI " + idUlaznice + " " + avioTvrtka;
    String odgovor = this.posaljiAvio(id, komanda);

    return this.odgovorOkJsonIliGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/stanje/{idLeta}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Dohvaća stanje sjedala na letu")
  @Counted(name = "rezervacijeAvioStanjeCount")
  @Timed(name = "rezervacijeAvioStanjeTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioStanje(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice) {

    String komanda = "STANJE " + idUlaznice + " " + idLeta;
    String odgovor = this.posaljiAvio(id, komanda);

    return this.odgovorOkTextIliGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/zatvori/{idLeta}")
  @PUT
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Zatvara redove sjedala na letu")
  @Counted(name = "rezervacijeAvioZatvoriLetCount")
  @Timed(name = "rezervacijeAvioZatvoriLetTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioZatvoriLet(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odReda") String odReda,
      @HeaderParam("doReda") String doReda) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda = "ZATVORI " + idUlaznice + " " + avioTvrtka + " "
        + idLeta + " " + odReda + " " + doReda;
    String odgovor = this.posaljiAvio(id, komanda);

    return this.odgovorOkIliGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/zatvori")
  @PUT
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Zatvara letove u zadanom razdoblju")
  @Counted(name = "rezervacijeAvioZatvoriRazdobljeCount")
  @Timed(name = "rezervacijeAvioZatvoriRazdobljeTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioZatvoriRazdoblje(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda = "ZATVORI " + idUlaznice + " " + avioTvrtka
        + " " + odDatuma + " " + doDatuma;
    String odgovor = this.posaljiAvio(id, komanda);

    return this.odgovorOkIliGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/inicijaliziraj/let")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Inicijalizira letove za oznaku leta")
  @Counted(name = "rezervacijeAvioInicijalizirajLetCount")
  @Timed(name = "rezervacijeAvioInicijalizirajLetTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioInicijalizirajLet(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda = "INICIJALIZIRAJ " + idUlaznice + " " + avioTvrtka
        + " \"" + oznakaLeta + "\" " + odDatuma + " " + doDatuma;
    String odgovor = this.posaljiAvio(id, komanda);

    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.status(Status.CREATED).entity(odgovor).build();
    }

    return this.odgovorGreska(odgovor);
  }

  @Path("aviotvrtke/{id}/inicijaliziraj")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Inicijalizira letove avio tvrtke")
  @Counted(name = "rezervacijeAvioInicijalizirajTvrtkuCount")
  @Timed(name = "rezervacijeAvioInicijalizirajTvrtkuTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response avioInicijalizirajTvrtku(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma) {

    String avioTvrtka = this.dohvatiAvioTvrtkuIzUlaznice(idUlaznice);

    if (avioTvrtka == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    String komanda = "INICIJALIZIRAJ " + idUlaznice + " " + avioTvrtka
        + " " + odDatuma + " " + doDatuma;
    String odgovor = this.posaljiAvio(id, komanda);

    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.status(Status.CREATED).entity(odgovor).build();
    }

    return this.odgovorGreska(odgovor);
  }

  @Path("{id}/admin/ping")
  @HEAD
  @Operation(summary = "Provjera rada administracijskog dijela poslužitelja rezervacije")
  @Counted(name = "rezervacijeAdminPingCount")
  @Timed(name = "rezervacijeAdminPingTime")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  public Response rezervacijeAdminPing(@PathParam("id") int id) {
    String odgovor = this.posaljiAdmin(id, "PING");
    return this.odgovorHead(odgovor, true);
  }

  @Path("{id}/admin/kraj")
  @HEAD
  @Operation(summary = "Zaustavlja poslužitelj rezervacije")
  @Counted(name = "rezervacijeAdminKrajCount")
  @Timed(name = "rezervacijeAdminKrajTime")
  @Timeout(3000)
  @Retry(maxRetries = 1)
  public Response rezervacijeAdminKraj(@PathParam("id") int id) {
    String odgovor = this.posaljiAdmin(id, "KRAJ " + this.kodZaKraj.get(id));
    return this.odgovorHead(odgovor, true);
  }

  @Path("{id}/admin/rezervacije")
  @DELETE
  @Operation(summary = "Poništava sve rezervacije na poslužitelju rezervacije")
  @Counted(name = "rezervacijeAdminPonistiCount")
  @Timed(name = "rezervacijeAdminPonistiTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  public Response rezervacijeAdminPonisti(@PathParam("id") int id) {
    String odgovor = this.posaljiAdmin(id, "REZERVACIJE PONIŠTI");

    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }

    if (odgovor == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.NOT_FOUND).build();
  }

  private String posaljiKorisnicku(int id, String komanda) {
    if (!this.postojiPosluzitelj(id)) {
      return null;
    }

    return this.klijentMrezneUticnice.posaljiKomandu(
        komanda, this.adresa.get(id), this.mreznaVrata.get(id));
  }

  private String posaljiAvio(int id, String komanda) {
    if (!this.postojiPosluzitelj(id)) {
      return null;
    }

    return this.klijentMrezneUticnice.posaljiKomandu(
        komanda, this.adresa.get(id), this.mreznaVrataAvioKompanije.get(id));
  }

  private String posaljiAdmin(int id, String komanda) {
    if (!this.postojiPosluzitelj(id)) {
      return null;
    }

    return this.klijentMrezneUticnice.posaljiKomandu(
        komanda, this.adresa.get(id), this.mreznaVrataAdministracija.get(id));
  }

  private boolean postojiPosluzitelj(int id) {
    return id >= 0 && id < this.adresa.size();
  }

  private String dohvatiAvioTvrtkuIzUlaznice(String idUlaznice) {
    String email = this.dohvatiEmailIzUlaznice(idUlaznice);

    if (email == null || email.isBlank()) {
      return null;
    }

    try (Response odgovor = this.korisniciRestKlijent.dajAvioTvrtkuPoEmailu(email)) {
      if (odgovor.getStatus() != Status.OK.getStatusCode() || !odgovor.hasEntity()) {
        return null;
      }

      AvioTvrtka avioTvrtka = odgovor.readEntity(AvioTvrtka.class);

      if (avioTvrtka == null) {
        return null;
      }

      return avioTvrtka.id();
    } catch (Exception e) {
      return null;
    }
  }

  private String dohvatiEmailIzUlaznice(String idUlaznice) {
    if (idUlaznice == null || idUlaznice.isBlank()) {
      return null;
    }

    try (var client = ClientBuilder.newClient()) {
      Response odgovor = client
          .target(KONTROLA_REST_URL + idUlaznice)
          .request(MediaType.TEXT_PLAIN)
          .get();

      try (odgovor) {
        if (odgovor.getStatus() != Status.OK.getStatusCode() || !odgovor.hasEntity()) {
          return null;
        }

        String email = odgovor.readEntity(String.class);
        return this.ocistiEmail(email);
      }
    } catch (Exception e) {
      return null;
    }
  }

  private String ocistiEmail(String email) {
    if (email == null) {
      return null;
    }

    String ocisceniEmail = email.trim();

    if (ocisceniEmail.startsWith("OK ")) {
      ocisceniEmail = ocisceniEmail.substring(3).trim();
    }

    if (ocisceniEmail.startsWith("\"") && ocisceniEmail.endsWith("\"")) {
      ocisceniEmail = ocisceniEmail.substring(1, ocisceniEmail.length() - 1);
    }

    if (ocisceniEmail.isBlank()) {
      return null;
    }

    return ocisceniEmail;
  }

  private Response odgovorHead(String odgovor, boolean serviceUnavailableZaNull) {
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }

    if (odgovor == null && serviceUnavailableZaNull) {
      return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }

    return Response.status(Status.NOT_FOUND).build();
  }

  private Response odgovorOkJsonIliGreska(String odgovor) {
    if (odgovor != null && odgovor.startsWith("OK ")) {
      return Response.ok(odgovor.substring(3), MediaType.APPLICATION_JSON).build();
    }

    return this.odgovorGreska(odgovor);
  }

  private Response odgovorOkTextIliGreska(String odgovor) {
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok(odgovor, MediaType.TEXT_PLAIN).build();
    }

    return this.odgovorGreska(odgovor);
  }

  private Response odgovorOkIliGreska(String odgovor) {
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }

    return this.odgovorGreska(odgovor);
  }

  private Response odgovorGreska(String odgovor) {
    if (odgovor == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    if (odgovor.startsWith("ERROR 20")
        || odgovor.startsWith("ERROR 21")
        || odgovor.startsWith("ERROR 22")
        || odgovor.startsWith("ERROR 24")
        || odgovor.startsWith("ERROR 25")) {
      return Response.status(Status.NOT_FOUND).entity(odgovor).build();
    }

    if (odgovor.startsWith("ERROR 23")
        || odgovor.startsWith("ERROR 26")) {
      return Response.status(Status.CONFLICT).entity(odgovor).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(odgovor).build();
  }
}
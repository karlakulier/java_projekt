package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.KorisniciRestKlijent;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.pomocnici.KlijentMrezneUticnice;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("kontrola")
public class KontrolaResource {

  @Inject
  @ConfigProperty(name = "kontrola.adresa")
  private String adresa;

  @Inject
  @ConfigProperty(name = "kontrola.mreznaVrata")
  private int mreznaVrata;

  @Inject
  @ConfigProperty(name = "kontrola.mreznaVrataAdministracija")
  private int mreznaVrataAdministracija;

  @Inject
  @ConfigProperty(name = "kontrola.kodZaKraj")
  private String kodZaKraj;

  @Inject
  KlijentMrezneUticnice klijentMrezneUticnice;

  @Inject
  KorisniciRestKlijent korisniciRestKlijent;

  @Path("ping")
  @HEAD
  @Operation(summary = "Provjera rada korisničkog dijela poslužitelja kontrola")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Poslužitelj radi"),
      @APIResponse(responseCode = "404", description = "Poslužitelj ne vraća OK"),
      @APIResponse(responseCode = "503", description = "Poslužitelj nije dostupan")})
  @Counted(name = "kontrolaKorisnickiPingCount")
  @Timed(name = "kontrolaKorisnickiPingTime")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "greskaPing")
  public Response ping() {
    String odgovor = this.posaljiKorisnickuKomandu("PING");

    if (odgovor == null) {
      return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }

    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }

    return Response.status(Status.NOT_FOUND).build();
  }

  @Path("pauza/{milisek}")
  @HEAD
  @Operation(summary = "Pokretanje pauze korisničkog dijela poslužitelja kontrola")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Pauza izvršena"),
      @APIResponse(responseCode = "404", description = "Pauza nije izvršena"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaPauzaCount")
  @Timed(name = "kontrolaPauzaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaPauza")
  public Response pauza(@PathParam("milisek") long milisek) {
    String odgovor = this.posaljiKorisnickuKomandu("PAUZA " + milisek);
    return this.odgovorOkIliNotFound(odgovor);
  }

  @Path("korisnik/dodaj/{korisnik}")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dodaje korisnika na PosluziteljKontrola")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Korisnik dodan"),
      @APIResponse(responseCode = "404", description = "Korisnik ne postoji u REST servisu"),
      @APIResponse(responseCode = "409", description = "Korisnik već postoji"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaKorisnikDodajCount")
  @Timed(name = "kontrolaKorisnikDodajTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaKorisnikDodaj")
  public Response korisnikDodaj(@PathParam("korisnik") String korisnik) {
    Korisnik pronadeniKorisnik = this.dohvatiKorisnika(korisnik);

    if (pronadeniKorisnik == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    String komanda = String.format("KORISNIK DODAJ \"%s\" \"%s\" %s %s",
        pronadeniKorisnik.ime(), pronadeniKorisnik.prezime(),
        pronadeniKorisnik.lozinka(), pronadeniKorisnik.email());
   

    String odgovor = this.posaljiKorisnickuKomandu(komanda);

    if ("OK".equals(odgovor)) {
      return Response.status(Status.CREATED).build();
    }

    if (odgovor != null && odgovor.startsWith("ERROR 11")) {
      return Response.status(Status.CONFLICT).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("korisnik/provjeri/{korisnik}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Provjerava korisnika na PosluziteljKontrola")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Korisnik postoji"),
      @APIResponse(responseCode = "404", description = "Korisnik ne postoji u REST servisu"),
      @APIResponse(responseCode = "409", description = "Korisnik nije registriran na kontroli"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaKorisnikProvjeriCount")
  @Timed(name = "kontrolaKorisnikProvjeriTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaKorisnikProvjeri")
  public Response korisnikProvjeri(@PathParam("korisnik") String korisnik) {
    Korisnik pronadeniKorisnik = this.dohvatiKorisnika(korisnik);

    if (pronadeniKorisnik == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    String odgovor =
        this.posaljiKorisnickuKomandu("KORISNIK PROVJERI " + pronadeniKorisnik.email());

    if ("OK".equals(odgovor)) {
      return Response.ok().build();
    }

    if (odgovor != null && odgovor.startsWith("ERROR 12")) {
      return Response.status(Status.CONFLICT).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }
  @Path("ulaznica/dodaj/{korisnik}")
  @POST
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Dodaje ulaznicu za korisnika")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Ulaznica kreirana"),
      @APIResponse(responseCode = "404", description = "Korisnik ne postoji"),
      @APIResponse(responseCode = "409", description = "Ulaznica već postoji ili lozinka nije ispravna"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaUlaznicaDodajCount")
  @Timed(name = "kontrolaUlaznicaDodajTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaUlaznicaDodaj")
  public Response ulaznicaDodaj(@PathParam("korisnik") String korisnik) {
    Korisnik pronadeniKorisnik = this.dohvatiKorisnikaPrivatno(korisnik);

    if (pronadeniKorisnik == null) {
      return Response.status(Status.NOT_FOUND)
          .entity("ERROR 10 - korisnik ne postoji u REST servisu")
          .build();
    }

    Response odgovorRegistracije = registrirajKorisnikaAkoTreba(pronadeniKorisnik);

    if (odgovorRegistracije != null) {
      return odgovorRegistracije;
    }

    String komanda = "ULAZNICA DODAJ " + pronadeniKorisnik.email()
        + " " + pronadeniKorisnik.lozinka();

    String odgovor = this.posaljiKorisnickuKomandu(komanda);

    System.out.println("Komanda ulaznice = " + komanda);
    System.out.println("Odgovor ulaznice = " + odgovor);

    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.status(Status.CREATED).entity(odgovor).build();
    }

    if (odgovor != null && odgovor.startsWith("ERROR 12")) {
      return Response.status(Status.CONFLICT).entity(odgovor).build();
    }

    if (odgovor != null && odgovor.startsWith("ERROR 13")) {
      return Response.status(Status.CONFLICT).entity(odgovor).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR)
        .entity(odgovor == null ? "Nema odgovora od PosluziteljKontrola" : odgovor)
        .build();
  }
  private Response registrirajKorisnikaAkoTreba(Korisnik korisnik) {
    String provjera = this.posaljiKorisnickuKomandu(
        "KORISNIK PROVJERI " + korisnik.email());

    System.out.println("Provjera korisnika = " + provjera);

    if (provjera != null && provjera.startsWith("OK")) {
      return null;
    }

    String komanda = String.format("KORISNIK DODAJ \"%s\" \"%s\" %s %s",
        korisnik.ime(), korisnik.prezime(), korisnik.lozinka(), korisnik.email());

    String odgovor = this.posaljiKorisnickuKomandu(komanda);

    System.out.println("Komanda korisnika = " + komanda);
    System.out.println("Odgovor korisnika = " + odgovor);

    if (odgovor != null && odgovor.startsWith("OK")) {
      return null;
    }

    if (odgovor != null && odgovor.startsWith("ERROR 11")) {
      return null;
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR)
        .entity(odgovor == null ? "Korisnik nije registriran na kontroli" : odgovor)
        .build();
  }
  @Path("ulaznica/provjeri/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Provjerava ulaznicu")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Ulaznica je ispravna"),
      @APIResponse(responseCode = "404", description = "Ulaznica nije ispravna"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaUlaznicaProvjeriCount")
  @Timed(name = "kontrolaUlaznicaProvjeriTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaUlaznicaProvjeri")
  public Response ulaznicaProvjeri(@PathParam("id") String id) {
    String odgovor = this.posaljiKorisnickuKomandu("ULAZNICA PROVJERI " + id);
    return this.odgovorOkIliNotFound(odgovor);
  }

  @Path("ulaznica/korisnik/{id}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Dohvaća email adresu korisnika ulaznice")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Email dohvaćen"),
      @APIResponse(responseCode = "404", description = "Ulaznica nije ispravna"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaUlaznicaKorisnikCount")
  @Timed(name = "kontrolaUlaznicaKorisnikTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaUlaznicaKorisnik")
  public Response ulaznicaKorisnik(@PathParam("id") String id) {
    String odgovor = this.posaljiKorisnickuKomandu("ULAZNICA KORISNIK " + id);

    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok(odgovor).build();
    }

    return Response.status(Status.NOT_FOUND).build();
  }

  @Path("ulaznica/ponisti/{id}")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Poništava ulaznicu")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Ulaznica poništena"),
      @APIResponse(responseCode = "404", description = "Ulaznica ne postoji"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaUlaznicaPonistiCount")
  @Timed(name = "kontrolaUlaznicaPonistiTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaUlaznicaPonisti")
  public Response ulaznicaPonisti(@PathParam("id") String id) {
    String odgovor = this.posaljiKorisnickuKomandu("ULAZNICA PONIŠTI " + id);
    return this.odgovorOkIliNotFound(odgovor);
  }

  @Path("admin/ping")
  @HEAD
  @Operation(summary = "Provjera rada administracijskog dijela poslužitelja kontrola")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Poslužitelj radi"),
      @APIResponse(responseCode = "404", description = "Poslužitelj ne vraća OK"),
      @APIResponse(responseCode = "503", description = "Poslužitelj nije dostupan")})
  @Counted(name = "kontrolaAdminPingCount")
  @Timed(name = "kontrolaAdminPingTime")
  @Timeout(3000)
  @Retry(maxRetries = 3)
  @Fallback(fallbackMethod = "greskaPing")
  public Response adminPing() {
    String odgovor = this.posaljiAdministracijskuKomandu("PING");

    if (odgovor == null) {
      return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }

    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }

    return Response.status(Status.NOT_FOUND).build();
  }

  @Path("admin/kraj")
  @HEAD
  @Operation(summary = "Kraj rada poslužitelja kontrola")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Poslužitelj zaustavljen"),
      @APIResponse(responseCode = "404", description = "Pogreška pri zaustavljanju"),
      @APIResponse(responseCode = "503", description = "Poslužitelj nije dostupan")})
  @Counted(name = "kontrolaAdminKrajCount")
  @Timed(name = "kontrolaAdminKrajTime")
  @Timeout(3000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaPing")
  public Response adminKraj() {
    String odgovor = this.posaljiAdministracijskuKomandu("KRAJ " + this.kodZaKraj);
    return this.odgovorOkIliNotFound(odgovor);
  }

  @Path("admin/ulaznica")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Poništava sve ulaznice")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Ulaznice poništene"),
      @APIResponse(responseCode = "404", description = "Poništavanje nije uspjelo"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaAdminUlaznicaPonistiCount")
  @Timed(name = "kontrolaAdminUlaznicaPonistiTime")
  @Timeout(3000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaUlaznicaPonisti")
  public Response adminUlaznicaPonisti() {
    String odgovor = this.posaljiAdministracijskuKomandu("ULAZNICA PONIŠTI");
    return this.odgovorOkIliNotFound(odgovor);
  }

  @Path("admin/registriraj")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Registrira poslužitelj")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Poslužitelj registriran"),
      @APIResponse(responseCode = "409", description = "Poslužitelj već registriran"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaAdminRegistrirajCount")
  @Timed(name = "kontrolaAdminRegistrirajTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaAdminRegistriraj")
  public Response adminRegistriraj(
      @HeaderParam("adresa") String adresaPosluzitelja,
      @HeaderParam("mreznaVrata") String vrata,
      @HeaderParam("mreznaVrataAdministracija") String vrataAdmin,
      @HeaderParam("kodZaKraj") String kod) {

    String komanda = "REGISTRIRAJ " + adresaPosluzitelja + " " + vrata
        + " " + vrataAdmin + " " + kod;

    String odgovor = this.posaljiAdministracijskuKomandu(komanda);

    if ("OK".equals(odgovor)) {
      return Response.status(Status.CREATED).build();
    }

    if (odgovor != null && odgovor.startsWith("ERROR 16")) {
      return Response.status(Status.CONFLICT).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("admin/zabrana")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dodaje zabranu adrese")
  @APIResponses(value = {
      @APIResponse(responseCode = "201", description = "Adresa zabranjena"),
      @APIResponse(responseCode = "409", description = "Adresa je već zabranjena"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaAdminZabranaCount")
  @Timed(name = "kontrolaAdminZabranaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaAdminZabrana")
  public Response adminZabrana(@HeaderParam("adresa") String adresaZabrane) {
    String odgovor =
        this.posaljiAdministracijskuKomandu("ZABRANA " + adresaZabrane);

    if ("OK".equals(odgovor)) {
      return Response.status(Status.CREATED).build();
    }

    if (odgovor != null && odgovor.startsWith("ERROR 16")) {
      return Response.status(Status.CONFLICT).build();
    }

    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  @Path("admin/dozvola")
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Briše zabranu adrese")
  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Adresa dozvoljena"),
      @APIResponse(responseCode = "404", description = "Adresa nije pronađena"),
      @APIResponse(responseCode = "500", description = "Pogreška")})
  @Counted(name = "kontrolaAdminDozvolaCount")
  @Timed(name = "kontrolaAdminDozvolaTime")
  @Timeout(5000)
  @Retry(maxRetries = 1)
  @Fallback(fallbackMethod = "greskaAdminDozvola")
  public Response adminDozvola(@HeaderParam("adresa") String adresaDozvole) {
    String odgovor =
        this.posaljiAdministracijskuKomandu("DOZVOLA " + adresaDozvole);

    if ("OK".equals(odgovor)) {
      return Response.ok().build();
    }

    return Response.status(Status.NOT_FOUND).build();
  }

  private Korisnik dohvatiKorisnika(String korisnik) {
    try (Response odgovor = this.korisniciRestKlijent.dajKorisnika(korisnik)) {
      if (odgovor.getStatus() != Status.OK.getStatusCode()
          || !odgovor.hasEntity()) {
        return null;
      }
      return odgovor.readEntity(Korisnik.class);
    }
  }

  private Korisnik dohvatiKorisnikaPrivatno(String korisnik) {
    try (Response odgovor =
        this.korisniciRestKlijent.dajKorisnika(korisnik)) {

      if (odgovor.getStatus() != Status.OK.getStatusCode()
          || !odgovor.hasEntity()) {
        return null;
      }

      return odgovor.readEntity(Korisnik.class);
    }
  }

  private Response odgovorOkIliNotFound(String odgovor) {
    if (odgovor != null && odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Status.NOT_FOUND).build();
  }

  private String posaljiKorisnickuKomandu(String komanda) {
    return this.klijentMrezneUticnice.posaljiKomandu(
        komanda, this.adresa, this.mreznaVrata);
  }

  private String posaljiAdministracijskuKomandu(String komanda) {
    return this.klijentMrezneUticnice.posaljiKomandu(
        komanda, this.adresa, this.mreznaVrataAdministracija);
  }

  public Response greskaPing() {
    return Response.status(Status.SERVICE_UNAVAILABLE).build();
  }

  public Response greskaPauza(long milisek) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaKorisnikDodaj(String korisnik) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaKorisnikProvjeri(String korisnik) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaUlaznicaDodaj(String korisnik) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaUlaznicaProvjeri(String id) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaUlaznicaKorisnik(String id) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaUlaznicaPonisti(String id) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaUlaznicaPonisti() {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaAdminRegistriraj(String adresaPosluzitelja,
      String vrata, String vrataAdmin, String kod) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaAdminZabrana(String adresaZabrane) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  public Response greskaAdminDozvola(String adresaDozvole) {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }
}
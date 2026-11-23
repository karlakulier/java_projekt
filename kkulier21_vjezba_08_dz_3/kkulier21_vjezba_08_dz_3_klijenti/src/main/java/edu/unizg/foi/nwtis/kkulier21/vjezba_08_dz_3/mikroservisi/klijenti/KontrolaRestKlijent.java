package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "mikroservisi.kontrola")
public interface KontrolaRestKlijent {
  
  @HEAD
  @Path("ping")
  Response pingKorisnickiDio();

  @HEAD
  @Path("admin/ping")
  Response pingAdminDio();

  @HEAD
  @Path("admin/kraj")
  Response krajAdminDio();

  @Path("ulaznica/dodaj/{korisnik}")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response dodajUlaznicu(@PathParam("korisnik") String korisnik, String tijelo);

  @Path("ulaznica/provjeri/{idUlaznice}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  Response provjeriUlaznicu(@PathParam("idUlaznice") String idUlaznice);

  @Path("ulaznica/korisnik/{idUlaznice}")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  Response dajKorisnikaUlaznice(@PathParam("idUlaznice") String idUlaznice);

  @Path("ulaznica/ponisti/{idUlaznice}")
  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response ponistiUlaznicu(@PathParam("idUlaznice") String idUlaznice, String tijelo);
}
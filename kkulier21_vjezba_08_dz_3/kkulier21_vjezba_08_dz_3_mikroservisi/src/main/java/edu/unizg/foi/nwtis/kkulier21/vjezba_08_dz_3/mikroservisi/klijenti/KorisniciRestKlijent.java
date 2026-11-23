package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.Korisnik;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// @RegisterRestClient(baseUri = "http://20.24.5.5:8080/api/v1/korisnici")
@RegisterRestClient(configKey = "servisi.korisnici")
public interface KorisniciRestKlijent {

  @Path("test")
  @GET
  @Consumes({MediaType.TEXT_PLAIN})
  @Produces({MediaType.TEXT_PLAIN})
  public Response test();

  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajSveKorisnike();

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response kreirajKorisnika(Korisnik korisnik);

  @Path("{id}")
  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajKorisnika(@PathParam("id") String id);

  @Path("{id}/{lozinka}")
  @GET
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajPrivatnoKorisnika(@PathParam("id") String id,
      @PathParam("lozinka") String lozinka);


  @Path("{id}")
  @PUT
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response azurirajKorisnika(@PathParam("id") Korisnik korisnik);

  @Path("{id}")
  @DELETE
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Response obrisiKorisnika(@PathParam("id") String id);
  
  @Path("email/{email}/aviotvrtka")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response dajAvioTvrtkuPoEmailu(@PathParam("email") String email);

}

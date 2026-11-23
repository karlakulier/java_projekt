package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "mikroservisi.rezervacije")
public interface RezervacijeRestKlijent {

  @Path("{id}/ispis/putnik")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response dajRezervacijePutnika(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma);

  @Path("{id}/{idRezervacijeSjedala}")
  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response potvrdiRezervaciju(@PathParam("id") int id,
      @PathParam("idRezervacijeSjedala") String idRezervacijeSjedala,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      String tijelo);

  @Path("{id}/ispis/letovi/{avioTvrtka}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response dajLetoveAvioTvrtke(@PathParam("id") int id,
      @PathParam("avioTvrtka") String avioTvrtka,
      @HeaderParam("idUlaznice") String idUlaznice);

  @Path("{id}/ispis/let")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  Response dajLetNaDatum(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("datum") String datum);

  @Path("{id}/ispis/{idLeta}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response dajSjedalaLeta(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice);

  @Path("{id}/rezerviraj")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response rezervirajSjedalo(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("idLeta") String idLeta,
      @HeaderParam("razred") String razred,
      @HeaderParam("putnik") String putnik,
      String tijelo);

  @Path("aviotvrtke/{id}/ispis/putnik")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response dajLetovePutnikaZaAvioTvrtku(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("putnik") String putnik,
      @HeaderParam("avioTvrtka") String avioTvrtka,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma);

  @Path("aviotvrtke/{id}/ispis/letovi")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response dajLetoveAvioTvrtkeZastupnika(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice);

  @Path("aviotvrtke/{id}/ispis/let")
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  Response dajLetoveAvioTvrtkeNaIntervalu(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma);

  @Path("aviotvrtke/{id}/ispis/{idLeta}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response dajSjedalaLetaAvioTvrtke(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice);

  @Path("aviotvrtke/{id}/inicijaliziraj/let")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response inicijalizirajLetAvioTvrtke(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma,
      String tijelo);
  
  @Path("aviotvrtke/{id}/stanje/{idLeta}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response dajStanjeSjedalaLetaAvioTvrtke(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice);
  
  @Path("aviotvrtke/{id}/zatvori/{idLeta}")
  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response zatvoriSjedalaAvioTvrtke(@PathParam("id") int id,
      @PathParam("idLeta") String idLeta,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odReda") String odReda,
      @HeaderParam("doReda") String doReda,
      String tijelo);
  @Path("aviotvrtke/{id}/ispis/let")
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  Response dajLetAvioTvrtkeNaDatum(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("datum") String datum);
  
  @Path("aviotvrtke/{id}/zatvori")
  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response zatvoriSjedalaAvioTvrtkeUIntervalu(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("oznakaLeta") String oznakaLeta,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma,
      @HeaderParam("odReda") String odReda,
      @HeaderParam("doReda") String doReda,
      String tijelo);
  
  @Path("aviotvrtke/{id}/inicijaliziraj")
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response inicijalizirajLetoveAvioTvrtke(@PathParam("id") int id,
      @HeaderParam("idUlaznice") String idUlaznice,
      @HeaderParam("odDatuma") String odDatuma,
      @HeaderParam("doDatuma") String doDatuma,
      String tijelo);
  
  @HEAD
  @Path("{id}/ping")
  Response pingKorisnickiDioRezervacije(@PathParam("id") int id);

  @HEAD
  @Path("aviotvrtke/{id}/ping")
  Response pingAvioTvrtkeDioRezervacije(@PathParam("id") int id);

  @HEAD
  @Path("{id}/admin/ping")
  Response pingAdminDioRezervacije(@PathParam("id") int id);

  @HEAD
  @Path("{id}/admin/kraj")
  Response krajAdminDioRezervacije(@PathParam("id") int id);
}
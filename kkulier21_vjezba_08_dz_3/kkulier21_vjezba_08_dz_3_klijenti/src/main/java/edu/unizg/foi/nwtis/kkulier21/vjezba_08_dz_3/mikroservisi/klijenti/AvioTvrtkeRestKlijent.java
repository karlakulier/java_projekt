package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;

@RegisterRestClient(configKey = "servisi.aviotvrtke")
public interface AvioTvrtkeRestKlijent {

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Response dajSveAvioTvrtke();

  @Path("{atid}/zastupnici")
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Response dajZastupnike(@PathParam("atid") String atid);
  @Path("{atid}/zastupnik/{id}")
  
  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response dodajZastupnika(@PathParam("atid") String atid, @PathParam("id") String id);

  @Path("{atid}/zastupnik/{id}")
  @DELETE
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  Response obrisiZastupnika(@PathParam("atid") String atid, @PathParam("id") String id);
}
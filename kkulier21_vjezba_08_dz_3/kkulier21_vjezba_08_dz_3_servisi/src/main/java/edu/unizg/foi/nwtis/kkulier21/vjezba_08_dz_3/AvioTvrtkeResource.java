package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.dao.AvioTvrtkaDAO;
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

@Path("api/v1/aviotvrtke")
public class AvioTvrtkeResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajSveAvioTvrtke() {
    try (var vezaBP = Main.dajVezu()) {
      var tvrtke = new AvioTvrtkaDAO(vezaBP).dohvatiSve();
      if (tvrtke == null || tvrtke.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(tvrtke).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response kreirajAvioTvrtku(AvioTvrtka tvrtka) {
    try (var vezaBP = Main.dajVezu()) {
      if (tvrtka == null || !new AvioTvrtkaDAO(vezaBP).dodaj(tvrtka)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajAvioTvrtku(@PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      var tvrtka = new AvioTvrtkaDAO(vezaBP).dohvati(atid);
      if (tvrtka == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(tvrtka).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response azurirajAvioTvrtku(@PathParam("atid") String atid, AvioTvrtka tvrtka) {
    try (var vezaBP = Main.dajVezu()) {
      if (tvrtka == null || !atid.equals(tvrtka.id())) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      if (!new AvioTvrtkaDAO(vezaBP).azuriraj(tvrtka)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}")
  @DELETE
  public Response obrisiAvioTvrtku(@PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new AvioTvrtkaDAO(vezaBP);
      if (dao.tvrtkaImaZastupnike(atid)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      if (!dao.obrisi(atid)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}/zastupnici")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajZastupnike(@PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      var zastupnici = new AvioTvrtkaDAO(vezaBP).dohvatiZastupnike(atid);
      if (zastupnici == null || zastupnici.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(zastupnici).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}/zastupnik/{id}")
  @POST
  public Response dodajZastupnika(@PathParam("atid") String atid, @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      if (!new AvioTvrtkaDAO(vezaBP).dodajZastupnika(atid, id)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{atid}/zastupnik/{id}")
  @DELETE
  public Response obrisiZastupnika(@PathParam("atid") String atid, @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      if (!new AvioTvrtkaDAO(vezaBP).obrisiZastupnika(atid, id)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}

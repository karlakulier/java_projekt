package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import edu.unizg.foi.nwtis.Grupa;
import edu.unizg.foi.nwtis.dao.GrupaDAO;
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

@Path("api/v1/grupe")
public class GrupeResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajSveGrupe() {
    try (var vezaBP = Main.dajVezu()) {
      var grupe = new GrupaDAO(vezaBP).dohvatiSve();
      if (grupe == null || grupe.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(grupe).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response kreirajGrupu(Grupa grupa) {
    try (var vezaBP = Main.dajVezu()) {
      if (grupa == null || !new GrupaDAO(vezaBP).dodaj(grupa)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajGrupu(@PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      var grupa = new GrupaDAO(vezaBP).dohvati(gid);
      if (grupa == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(grupa).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response azurirajGrupu(@PathParam("gid") String gid, Grupa grupa) {
    try (var vezaBP = Main.dajVezu()) {
      if (grupa == null || !gid.equals(grupa.grupa())) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      if (!new GrupaDAO(vezaBP).azuriraj(grupa)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}")
  @DELETE
  public Response obrisiGrupu(@PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new GrupaDAO(vezaBP);
      if (dao.grupaImaKorisnike(gid)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      if (!dao.obrisi(gid)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}/korisnici")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajKorisnikeGrupe(@PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnici = new GrupaDAO(vezaBP).dohvatiKorisnikeGrupe(gid);
      if (korisnici == null || korisnici.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(korisnici).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}/korisnik/{id}")
  @POST
  public Response dodajKorisnikaUGrupu(@PathParam("gid") String gid, @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      if (!new GrupaDAO(vezaBP).dodajKorisnika(gid, id)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{gid}/korisnik/{id}")
  @DELETE
  public Response obrisiKorisnikaIzGrupe(@PathParam("gid") String gid, @PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      if (!new GrupaDAO(vezaBP).obrisiKorisnika(gid, id)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}

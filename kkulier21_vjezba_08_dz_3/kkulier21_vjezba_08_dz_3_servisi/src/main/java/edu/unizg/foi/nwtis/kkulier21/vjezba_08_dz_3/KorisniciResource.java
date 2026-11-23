package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import edu.unizg.foi.nwtis.Korisnik;

import edu.unizg.foi.nwtis.dao.KorisnikDAO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/v1/korisnici")
public class KorisniciResource {

  @Path("test")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response test() {
    return Response.ok("OK").build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajKorisnike(@QueryParam("ime") String ime, @QueryParam("prezime") String prezime,
      @DefaultValue("-1") @QueryParam("odStranice") int odStranice,
      @DefaultValue("-1") @QueryParam("stranica") int stranica) {

    try (var vezaBP = Main.dajVezu()) {
      var dao = new KorisnikDAO(vezaBP);
      var korisnici = (ime != null && prezime != null && odStranice >= 0 && stranica > 0)
          ? dao.dohvatiImePrezime(ime, prezime, odStranice, stranica)
          : (odStranice >= 0 && stranica > 0) ? dao.dohvatiSve(odStranice, stranica)
              : dao.dohvatiSve();

      if (korisnici == null || korisnici.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(korisnici).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response kreirajKorisnika(Korisnik korisnik) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new KorisnikDAO(vezaBP);
      if (korisnik == null || !dao.dodaj(korisnik)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajKorisnika(@PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnik = new KorisnikDAO(vezaBP).dohvati(id, null, false);
      if (korisnik == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(korisnik).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/{lozinka}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajKorisnikaSLozinkom(@PathParam("id") String id,
      @PathParam("lozinka") String lozinka) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnik = new KorisnikDAO(vezaBP).dohvati(id, lozinka);
      if (korisnik == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(korisnik).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response azurirajKorisnika(@PathParam("id") String id, Korisnik korisnik) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new KorisnikDAO(vezaBP);
      if (korisnik == null || !id.equals(korisnik.korisnik()) || !dao.azuriraj(korisnik)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}")
  @DELETE
  public Response obrisiKorisnika(@PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new KorisnikDAO(vezaBP);
      if (dao.korisnikImaVeze(id)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      if (!dao.obrisi(id)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("email/{email}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajKorisnikaPoEmailu(@PathParam("email") String email) {
    try (var vezaBP = Main.dajVezu()) {
      var korisnik = new KorisnikDAO(vezaBP).dohvatiEmail(email);
      if (korisnik == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(korisnik).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/grupa")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajGrupeKorisnika(@PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var grupe = new KorisnikDAO(vezaBP).dohvatiGrupeKorisnika(id);
      if (grupe == null || grupe.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(grupe).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/grupa/{gid}")
  @POST
  public Response dodajKorisnikaUGrupu(@PathParam("id") String id, @PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      var dao = new KorisnikDAO(vezaBP);
      if (!dao.dodajKorisnikaUGrupu(id, gid)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/grupa/{gid}")
  @DELETE
  public Response obrisiKorisnikaIzGrupe(@PathParam("id") String id, @PathParam("gid") String gid) {
    try (var vezaBP = Main.dajVezu()) {
      if (!new KorisnikDAO(vezaBP).obrisiKorisnikaIzGrupe(id, gid)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/aviotvrtka")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajAvioTvrtkuKorisnika(@PathParam("id") String id) {
    try (var vezaBP = Main.dajVezu()) {
      var avioTvrtka = new KorisnikDAO(vezaBP).dohvatiAvioTvrtkuKorisnika(id);
      if (avioTvrtka == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(avioTvrtka).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/aviotvrtka/{atid}")
  @POST
  public Response dodajZastupnika(@PathParam("id") String id, @PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      if (!new KorisnikDAO(vezaBP).dodajZastupnika(id, atid)) {
        return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("{id}/aviotvrtka/{atid}")
  @DELETE
  public Response obrisiZastupnika(@PathParam("id") String id, @PathParam("atid") String atid) {
    try (var vezaBP = Main.dajVezu()) {
      if (!new KorisnikDAO(vezaBP).obrisiZastupnika(id, atid)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("email/{email}/aviotvrtka")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response dajAvioTvrtkuPoEmailu(@PathParam("email") String email) {
    try (var vezaBP = Main.dajVezu()) {
      var avioTvrtka = new KorisnikDAO(vezaBP).dohvatiAvioTvrtkuPoEmail(email);
      if (avioTvrtka == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok(avioTvrtka).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}

package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.controller;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.Korisnik;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms.PosiljateljJmsTema;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jms.SpremnikJmsPoruka;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.AvioTvrtkeRestKlijent;
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.mikroservisi.klijenti.KorisniciRestKlijent;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.mvc.binding.MvcBinding;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response.Status;

@Controller
@Path("korisnici")
@RequestScoped
@PermitAll
public class Kontroler {

  @Inject
  private Models model;

  @Inject
  private BindingResult bindingResult;

  @Inject
  private SecurityContext securityContext;

  @Inject
  SpremnikJmsPoruka spremnikJmsPoruka;

  @Inject
  PosiljateljJmsTema posiljateljJmsTema;

  @Inject
  @RestClient
  KorisniciRestKlijent korisniciRestKlijent;

  @Inject
  @RestClient
  AvioTvrtkeRestKlijent avioTvrtkeRestKlijent;

  @GET
  @Path("pocetak")
  @View("index.jsp")
  @PermitAll
  public void pocetak() {
    if (this.securityContext.getCallerPrincipal() != null) {
      var ime = this.securityContext.getCallerPrincipal().getName();
      model.put("korisnik", ime);
    }
  }

  @GET
  @Path("noviKorisnik")
  @View("noviKorisnik.jsp")
  @PermitAll
  public void noviKorisnik() {}

  @GET
  @Path("ispisKorisnika")
  @View("korisnici.jsp")
  @PermitAll
  public void ispisKorisnika() {
    try (var odgovor = korisniciRestKlijent.dajSveKorisnike()) {
      if (odgovor.getStatus() == Status.OK.getStatusCode()) {
        var korisnici = odgovor.readEntity(new GenericType<List<Korisnik>>() {});
        model.put("korisnici", korisnici);
      } else {
        model.put("korisnici", new ArrayList<Korisnik>());
        model.put("poruka", "Korisnici nisu dohvaćeni. Status: " + odgovor.getStatus());
      }
    } catch (Exception e) {
      model.put("korisnici", new ArrayList<Korisnik>());
      model.put("poruka", "Problem kod dohvaćanja korisnika.");
    }
  }

  @GET
  @Path("pregledRedaPoruka")
  @View("pregledRedaPoruka.jsp")
  @PermitAll
  public void pregledRedaPoruka() {
    var listaPoruka = this.spremnikJmsPoruka.dajRedPoruka();
    model.put("poruke", listaPoruka);
  }

  @GET
  @Path("obrisiRedPoruka")
  @View("pregledRedaPoruka.jsp")
  @PermitAll
  public void obrisiRedPoruka() {
    this.spremnikJmsPoruka.obrisiRedPoruka();
    var listaPoruka = this.spremnikJmsPoruka.dajRedPoruka();
    model.put("poruke", listaPoruka);
  }

  @GET
  @Path("pregledPorukaTema")
  @View("pregledPorukaTema.jsp")
  @PermitAll
  public void pregledPorukaTema() {
    var listaPoruka = this.spremnikJmsPoruka.dajPorukeTeme();
    model.put("poruke", listaPoruka);
  }

  @GET
  @Path("obrisiPorukeTema")
  @View("pregledPorukaTema.jsp")
  @PermitAll
  public void obrisiPorukeTema() {
    this.spremnikJmsPoruka.obrisiPorukeTeme();
    var listaPoruka = this.spremnikJmsPoruka.dajPorukeTeme();
    model.put("poruke", listaPoruka);
  }

  @POST
  @Path("pretrazivanjeKorisnika")
  @View("korisnici.jsp")
  @PermitAll
  public void pretrazivanjeKorisnika(@FormParam("prezime") String prezime,
      @FormParam("ime") String ime) {

    try (var odgovor = korisniciRestKlijent.dajSveKorisnike()) {
      if (odgovor.getStatus() != Status.OK.getStatusCode()) {
        model.put("korisnici", new ArrayList<Korisnik>());
        model.put("poruka", "Korisnici nisu dohvaćeni. Status: " + odgovor.getStatus());
        return;
      }

      var sviKorisnici = odgovor.readEntity(new GenericType<List<Korisnik>>() {});
      List<Korisnik> korisnici = new ArrayList<>();

      var trazenoIme = ime == null ? "" : ime.trim().toLowerCase();
      var trazenoPrezime = prezime == null ? "" : prezime.trim().toLowerCase();

      if (trazenoIme.length() == 0 && trazenoPrezime.length() == 0) {
        model.put("korisnici", korisnici);
        return;
      }

      for (var korisnik : sviKorisnici) {
        var korisnikIme = korisnik.ime() == null ? "" : korisnik.ime().toLowerCase();
        var korisnikPrezime =
            korisnik.prezime() == null ? "" : korisnik.prezime().toLowerCase();

        var odgovaraIme = trazenoIme.length() == 0 || korisnikIme.contains(trazenoIme);
        var odgovaraPrezime =
            trazenoPrezime.length() == 0 || korisnikPrezime.contains(trazenoPrezime);

        if (odgovaraIme && odgovaraPrezime) {
          korisnici.add(korisnik);
        }
      }

      model.put("korisnici", korisnici);

    } catch (Exception e) {
      model.put("korisnici", new ArrayList<Korisnik>());
      model.put("poruka", "Problem kod pretraživanja korisnika.");
    }
  }

  @POST
  @Path("dodajKorisnika")
  @PermitAll
  public String dodajKorisnika(@MvcBinding @FormParam("korisnik") String korId,
      @FormParam("lozinka") String lozinka, @FormParam("prezime") String prezime,
      @FormParam("ime") String ime, @FormParam("email") String email) {

    if (bindingResult.isFailed() || korId == null || korId.trim().length() == 0
        || lozinka == null || lozinka.trim().length() == 0
        || prezime == null || prezime.trim().length() == 0
        || ime == null || ime.trim().length() == 0
        || email == null || email.trim().length() == 0) {

      model.put("poruka", "Nisu upisani potrebni podaci.");
      model.put("pogreska", true);
      model.put("korId", korId == null ? "" : korId);
      model.put("lozinka", "");
      model.put("prezime", prezime == null ? "" : prezime);
      model.put("ime", ime == null ? "" : ime);
      model.put("email", email == null ? "" : email);

      return "noviKorisnik.jsp";
    }

    var korisnik = new Korisnik(korId, lozinka, prezime, ime, email);

    try (var odgovor = korisniciRestKlijent.kreirajKorisnika(korisnik)) {
      if (odgovor.getStatus() == Status.CREATED.getStatusCode()
          || odgovor.getStatus() == Status.OK.getStatusCode()) {

        boolean jmsPoslan = this.posiljateljJmsTema.novaPoruka(korisnik);

        model.put("poruka", "Uspješno dodan korisnik: " + korisnik.ime() + " "
            + korisnik.prezime()
            + (jmsPoslan ? " JMS poruka je poslana." : " JMS poruka nije poslana."));
        model.put("pogreska", !jmsPoslan);

      } else {
        String tijelo = "";

        try {
          tijelo = odgovor.readEntity(String.class);
        } catch (Exception e) {
          tijelo = "";
        }

        model.put("poruka", "Korisnik nije dodan. Status: " + odgovor.getStatus()
            + " Odgovor: " + tijelo);
        model.put("pogreska", true);
      }

    } catch (Exception e) {
      model.put("poruka", "Problem kod dodavanja korisnika: " + e.getClass().getName()
          + " - " + e.getMessage());
      model.put("pogreska", true);
      e.printStackTrace();
    
    }

    return "noviKorisnik.jsp";
  }

  @GET
  @Path("ispisAvioTvrtki")
  @View("avioTvrtke.jsp")
  @PermitAll
  public void ispisAvioTvrtki() {
    try (var odgovor = avioTvrtkeRestKlijent.dajSveAvioTvrtke()) {
      if (odgovor.getStatus() == Status.OK.getStatusCode()) {
        var avioTvrtke = odgovor.readEntity(new GenericType<List<AvioTvrtka>>() {});
        model.put("avioTvrtke", avioTvrtke);
      } else {
        model.put("avioTvrtke", new ArrayList<AvioTvrtka>());
        model.put("poruka", "Avio tvrtke nisu dohvaćene. Status: " + odgovor.getStatus());
      }
    } catch (Exception e) {
      model.put("avioTvrtke", new ArrayList<AvioTvrtka>());
      model.put("poruka", "Problem kod dohvaćanja avio tvrtki.");
    }
  }

  @GET
  @Path("ispisZastupnika/{atid}")
  @View("zastupnici.jsp")
  @PermitAll
  public void ispisZastupnika(@PathParam("atid") String atid) {
    try (var odgovor = avioTvrtkeRestKlijent.dajZastupnike(atid)) {
      if (odgovor.getStatus() == Status.OK.getStatusCode()) {
        var zastupnici = odgovor.readEntity(new GenericType<List<Korisnik>>() {});
        model.put("zastupnici", zastupnici);
      } else {
        model.put("zastupnici", new ArrayList<Korisnik>());
        model.put("poruka", "Zastupnici nisu dohvaćeni. Status: " + odgovor.getStatus());
      }
    } catch (Exception e) {
      model.put("zastupnici", new ArrayList<Korisnik>());
      model.put("poruka", "Problem kod dohvaćanja zastupnika.");
    }

    model.put("atid", atid);
  }
}
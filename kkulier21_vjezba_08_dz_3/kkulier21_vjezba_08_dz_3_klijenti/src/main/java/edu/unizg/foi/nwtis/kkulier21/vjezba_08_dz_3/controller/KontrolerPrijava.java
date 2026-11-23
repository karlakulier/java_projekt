/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.controller;

import java.io.Serializable;


import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.binding.BindingResult;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author Karla Kulier
 */
@Controller
@Path("prijava")
@PermitAll
public class KontrolerPrijava implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 3488625477601542324L;

  @Inject
  private Models model;

  @Inject
  private BindingResult bindingResult;

//  @Inject
 // KorisniciFacade korisniciFacade;

  @Inject
  private SecurityContext securityContext;

  @GET
  @Path("prijavaKorisnika")
  public String prijavaKorisnika() {
    return "prijavaKorisnika.jsp";
  }

  @GET
  @Path("prijavaKorisnikaPogreska")
  public String prijavaKorisnikaPogreska() {
    return "prijavaKorisnikaPogreska.jsp";
  }

  @GET
  @Path("odjavaKorisnika")
  public Response odjavaKorisnika(@Context HttpServletRequest request) throws Exception {
    request.logout();
    request.getSession().invalidate();

    return Response
        .seeOther(java.net.URI.create(request.getContextPath() + "/mvc/korisnici/pocetak")).build();
  }

}

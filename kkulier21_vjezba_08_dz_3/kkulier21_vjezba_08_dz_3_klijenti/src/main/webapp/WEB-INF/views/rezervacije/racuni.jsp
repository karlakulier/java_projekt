<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Pregled mojih računa</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled mojih računa</h1>
        <p class="page-subtitle">Pregled računa za kupovinu avio karata i njihovih stavki.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">
                Početna rezervacije
            </a>
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");
            Object oPogreska = request.getAttribute("pogreska");

            if (poruka != null && poruka.length() > 0) {
                boolean pogreska = oPogreska != null && (Boolean) oPogreska;
                String klasa = pogreska ? "alert alert-error" : "alert alert-success";
        %>
            <div class="<%= klasa %>"><%= poruka %></div>
        <%
            }

            Boolean sviRacuniZatvoreni = (Boolean) request.getAttribute("sviRacuniZatvoreni");

            if (sviRacuniZatvoreni != null && sviRacuniZatvoreni) {
        %>
            <div class="actions">
                <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/otvoriRacun">
                    Otvori novi račun za kupovinu avio karata
                </a>
            </div>
        <%
            } else {
        %>
            <div class="alert alert-error">
                Novi račun se može otvoriti tek kada su svi računi zatvoreni.
            </div>
        <%
            }
        %>

        <%
            List<Racun> racuni = (List<Racun>) request.getAttribute("racuni");
            Map<Integer, Integer> stvarniBrojeviStavki =
                (Map<Integer, Integer>) request.getAttribute("stvarniBrojeviStavki");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID</th>
                    <th>Vrijeme</th>
                    <th>Broj stavki u računu</th>
                    <th>Stvarni broj stavki</th>
                    <th>Iznos</th>
                    <th>Status</th>
                    <th>Stavke</th>
                    <th>Zatvaranje</th>
                </tr>

                <%
                    int i = 0;

                    if (racuni != null && !racuni.isEmpty()) {
                        for (Racun r : racuni) {
                            i++;
                            int stvarniBroj = 0;

                            if (stvarniBrojeviStavki != null && stvarniBrojeviStavki.get(r.getId()) != null) {
                                stvarniBroj = stvarniBrojeviStavki.get(r.getId());
                            }

                            boolean otvoren = r.getIznos() == 0.0;
                            boolean imaStavke = stvarniBroj > 0;
                            boolean brojStavkiUskladen = r.getBrojstavki() == stvarniBroj;
                            boolean mozeZatvoriti = otvoren && imaStavke && brojStavkiUskladen;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= r.getId() %></td>
                    <td><%= r.getVrijeme() %></td>
                    <td><%= r.getBrojstavki() %></td>
                    <td><%= stvarniBroj %></td>
                    <td><%= r.getIznos() %></td>
                    <td>
                        <span class="badge"><%= otvoren ? "Otvoren" : "Zatvoren" %></span>
                    </td>
                    <td>
                        <a class="link" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/stavkeRacuna/<%= r.getId() %>">
                            Pregled stavki
                        </a>
                    </td>
                    <td>
                        <%
                            if (mozeZatvoriti) {
                        %>
                            <a class="link" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/zatvoriRacun/<%= r.getId() %>">
                                Zatvori račun
                            </a>
                        <%
                            } else {
                        %>
                            -
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="9">Nema računa za prikaz.</td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>
    </section>

</main>
</body>
</html>
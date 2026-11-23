<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="edu.unizg.foi.nwtis.Korisnik" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Nadzorna ploča za korisnike</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Nadzorna ploča za korisnike</h1>
        <p class="page-subtitle">
            Pregled korisnika pristiglih putem JMS teme i upravljanje listom korisnika.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                Administracijski status
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPloca">
                Nadzorna ploča za rezervacije
            </a>

            <a class="btn btn-danger"
               href="${pageContext.servletContext.contextPath}/mvc/admin/korisnici/obrisiSve">
                Obriši sve korisnike iz liste
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

            List<Korisnik> korisnici =
                (List<Korisnik>) request.getAttribute("korisnici");

            int ukupnoKorisnika = korisnici == null ? 0 : korisnici.size();
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Ukupno korisnika u listi</div>
                <div class="info-value"><%= ukupnoKorisnika %></div>
            </div>
        </div>

        <h2 class="card-title">Popis korisnika</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Korisnik</th>
                    <th>Ime</th>
                    <th>Prezime</th>
                    <th>Email</th>
                    <th>Akcija</th>
                </tr>

                <%
                    if (korisnici != null && !korisnici.isEmpty()) {
                        int i = 0;

                        for (Korisnik k : korisnici) {
                            i++;

                            String korisnik = k.korisnik();
                            String ime = k.ime();
                            String prezime = k.prezime();
                            String email = k.email();

                            if (korisnik == null) {
                                korisnik = "";
                            }

                            if (ime == null) {
                                ime = "";
                            }

                            if (prezime == null) {
                                prezime = "";
                            }

                            if (email == null) {
                                email = "";
                            }

                            String korisnikZaUrl = URLEncoder.encode(korisnik, "UTF-8")
                                .replace("+", "%20");
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= korisnik %></td>
                    <td><%= ime %></td>
                    <td><%= prezime %></td>
                    <td><%= email %></td>
                    <td>
                        <a class="link"
                           href="${pageContext.servletContext.contextPath}/mvc/admin/korisnici/obrisi/<%= korisnikZaUrl %>">
                            Obriši
                        </a>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="6">Nema korisnika u listi.</td>
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
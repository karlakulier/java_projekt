<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun" %>
<%@ page import="edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racunstavka" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Stavke zatvorenog računa</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Stavke zatvorenog računa</h1>
        <p class="page-subtitle">
            Pregled podataka zatvorenog računa i svih pripadajućih stavki.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/zatvoreniRacuni">
                Povratak na zatvorene račune
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                Administracijski status
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");

            if (poruka != null && poruka.length() > 0) {
        %>
            <div class="alert alert-error"><%= poruka %></div>
        <%
            }

            Racun racun = (Racun) request.getAttribute("racun");
            List<Racunstavka> stavke =
                (List<Racunstavka>) request.getAttribute("stavke");
        %>

        <h2 class="card-title">Račun</h2>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">ID računa</div>
                <div class="info-value">
                    <%= racun != null ? racun.getId() : "-" %>
                </div>
            </div>

            <div class="info-item">
                <div class="info-label">Korisnik</div>
                <div class="info-value">
                    <%= racun != null && racun.getKorisnici() != null ? racun.getKorisnici().getKorisnik() : "-" %>
                </div>
            </div>

            <div class="info-item">
                <div class="info-label">Broj stavki</div>
                <div class="info-value">
                    <%= racun != null ? racun.getBrojstavki() : "-" %>
                </div>
            </div>

            <div class="info-item">
                <div class="info-label">Iznos</div>
                <div class="info-value">
                    <%= racun != null ? racun.getIznos() : "-" %>
                </div>
            </div>
        </div>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Vrijeme</div>
                <div class="info-value">
                    <%= racun != null ? racun.getVrijeme() : "-" %>
                </div>
            </div>

            <div class="info-item">
                <div class="info-label">Status</div>
                <div class="info-value">
                    <span class="badge">Zatvoren</span>
                </div>
            </div>
        </div>

        <h2 class="card-title">Stavke računa</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID stavke</th>
                    <th>Iznos</th>
                    <th>Ime putnika</th>
                    <th>Prezime putnika</th>
                    <th>Rezervacija</th>
                </tr>

                <%
                    if (stavke != null && !stavke.isEmpty()) {
                        int i = 0;

                        for (Racunstavka s : stavke) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= s.getId() %></td>
                    <td><%= s.getIznos() %></td>
                    <td><%= s.getPutnikime() == null ? "" : s.getPutnikime() %></td>
                    <td><%= s.getPutnikiprezime() == null ? "" : s.getPutnikiprezime() %></td>
                    <td><%= s.getRezervacija() == null ? "" : s.getRezervacija() %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="6">Nema stavki za prikaz.</td>
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
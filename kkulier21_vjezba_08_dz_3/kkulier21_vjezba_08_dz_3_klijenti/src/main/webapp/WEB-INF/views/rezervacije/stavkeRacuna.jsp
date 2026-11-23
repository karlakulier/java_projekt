<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun" %>
<%@ page import="edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racunstavka" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Stavke računa</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Stavke računa</h1>
        <p class="page-subtitle">Pregled stavki odabranog računa i mogućnost usklađivanja broja stavki.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/racuni">
                Pregled računa
            </a>
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">
                Početna rezervacije
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

            Racun racun = (Racun) request.getAttribute("racun");

            if (racun != null) {
        %>

        <h2 class="card-title">Podaci računa</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>ID računa</th>
                    <th>Broj stavki u računu</th>
                    <th>Stvarni broj stavki</th>
                    <th>Iznos</th>
                </tr>
                <tr>
                    <td><%= racun.getId() %></td>
                    <td><%= racun.getBrojstavki() %></td>
                    <td>${stvarniBrojStavki}</td>
                    <td><%= racun.getIznos() %></td>
                </tr>
            </table>
        </div>

        <%
            Boolean mozeUskladiti = (Boolean) request.getAttribute("mozeUskladiti");

            if (mozeUskladiti != null && mozeUskladiti) {
        %>
            <div class="actions" style="margin-top: 16px;">
                <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/uskladiStavke/<%= racun.getId() %>">
                    Uskladi broj stavki računa
                </a>
            </div>
        <%
            }
        %>

        <%
            }
        %>

        <h2 class="card-title">Stavke računa</h2>

        <%
            List<Racunstavka> stavke =
                (List<Racunstavka>) request.getAttribute("stavke");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID</th>
                    <th>Putnik ime</th>
                    <th>Putnik prezime</th>
                    <th>Rezervacija</th>
                    <th>Iznos</th>
                </tr>

                <%
                    int i = 0;

                    if (stavke != null && !stavke.isEmpty()) {
                        for (Racunstavka s : stavke) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= s.getId() %></td>
                    <td><%= s.getPutnikime() %></td>
                    <td><%= s.getPutnikiprezime() %></td>
                    <td><%= s.getRezervacija() %></td>
                    <td><%= s.getIznos() %></td>
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
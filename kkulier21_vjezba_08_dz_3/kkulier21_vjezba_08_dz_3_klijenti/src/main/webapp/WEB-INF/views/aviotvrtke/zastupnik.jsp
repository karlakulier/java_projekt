<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Privatni dio avio tvrtke</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Privatni dio za avio tvrtke</h1>
        <p class="page-subtitle">Aktivnosti zastupnika avio tvrtke, rezervacije, računi, putnici i letovi.</p>
    </div>

    <section class="card">
        <%
            String idUlaznice = (String) request.getAttribute("idUlaznice");

            if (idUlaznice == null) {
                idUlaznice = "";
            }
        %>

        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>

            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">
                Odabir poslužitelja rezervacija
            </a>

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak?idUlaznice=<%= idUlaznice %>">
                Pregled avio tvrtki i letova
            </a>

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letovi?idUlaznice=<%= idUlaznice %>">
                Pregled letova zastupnika
            </a>

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/putnici?idUlaznice=<%= idUlaznice %>">
                Pregled putnika
            </a>

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/nepotvrdjeneRezervacije?idUlaznice=<%= idUlaznice %>">
                Nepotvrđene rezervacije
            </a>

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/racuni?idUlaznice=<%= idUlaznice %>">
                Pregled računa zastupnika
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

            Boolean odabranPosluzitelj = (Boolean) request.getAttribute("odabranPosluzitelj");

            if (odabranPosluzitelj == null || !odabranPosluzitelj) {
        %>
            <div class="alert alert-error">
                Prvo je potrebno odabrati poslužitelj rezervacija. Dok poslužitelj nije odabran,
                aktivnosti zastupnika avio tvrtke nisu dostupne.
            </div>
        <%
            } else {
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Odabrani poslužitelj</div>
                <div class="info-value">${posluzitelj}</div>
            </div>

            <div class="info-item">
                <div class="info-label">ID ulaznice</div>
                <div class="info-value"><%= idUlaznice.isBlank() ? "-" : idUlaznice %></div>
            </div>
        </div>

        <h2 class="card-title">Privremene rezervacije zastupnika</h2>

        <div class="alert alert-success">
            Zastupnik smije privremeno rezervirati sjedala samo na različitim letovima avio tvrtke kojoj je zastupnik.
        </div>

        <div class="actions">
            <a class="btn btn-danger" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/obrisiPrivremeneRezervacije?idUlaznice=<%= idUlaznice %>">
                Obriši privremene rezervacije
            </a>
        </div>

        <%
            List<Map<String, String>> rezervacije =
                (List<Map<String, String>>) request.getAttribute("rezervacije");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID leta</th>
                    <th>Oznaka leta</th>
                    <th>Sjedalo</th>
                    <th>Putnik ime</th>
                    <th>Putnik prezime</th>
                </tr>

                <%
                    int i = 0;

                    if (rezervacije != null && !rezervacije.isEmpty()) {
                        for (Map<String, String> r : rezervacije) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= r.get("idLeta") == null ? "" : r.get("idLeta") %></td>
                    <td><%= r.get("oznakaLeta") == null ? "" : r.get("oznakaLeta") %></td>
                    <td><%= r.get("sjedalo") == null ? "" : r.get("sjedalo") %></td>
                    <td><%= r.get("putnikIme") == null ? "" : r.get("putnikIme") %></td>
                    <td><%= r.get("putnikPrezime") == null ? "" : r.get("putnikPrezime") %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="6">Nema privremenih rezervacija.</td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>

        <%
            }
        %>
    </section>

</main>
</body>
</html>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Pregled avio tvrtki</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled avio tvrtki</h1>
        <p class="page-subtitle">Pregled avio tvrtki i dostupnih letova za odabrani poslužitelj rezervacija.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>

            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">
                Početna rezervacije
            </a>

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik">
                Privatni dio za avio tvrtke
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");
            Object oPogreska = request.getAttribute("pogreska");

            String idUlaznice = (String) request.getAttribute("idUlaznice");
            if (idUlaznice == null) {
                idUlaznice = "";
            }

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
                Za pregled letova prvo je potrebno odabrati poslužitelj rezervacija.
            </div>
        <%
            }
        %>

        <h2 class="card-title">Podaci za dohvat letova</h2>

        <form method="get" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">
            <div class="form-row">
                <div class="form-field">
                    <label for="idUlaznice">ID ulaznice</label>
                    <input id="idUlaznice" name="idUlaznice" type="text" value="<%= idUlaznice %>" readonly>
                </div>

                <div class="form-field">
                    <button class="btn" type="submit">Osvježi</button>
                </div>
            </div>
        </form>

        <%
            List<Map<String, String>> avioTvrtke =
                (List<Map<String, String>>) request.getAttribute("avioTvrtke");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Oznaka</th>
                    <th>Naziv</th>
                    <th>Letovi</th>
                </tr>

                <%
                    int i = 0;

                    if (avioTvrtke != null && !avioTvrtke.isEmpty()) {
                        for (Map<String, String> a : avioTvrtke) {
                            i++;

                            String atid = a.get("atid");
                            if (atid == null) {
                                atid = "";
                            }

                            String naziv = a.get("naziv");
                            if (naziv == null) {
                                naziv = "";
                            }
                %>
                <tr>
                    <td><%= i %></td>
                    <td><span class="badge"><%= atid %></span></td>
                    <td><%= naziv %></td>
                    <td>
                        <a class="link" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/letovi/<%= atid %>?idUlaznice=<%= idUlaznice %>">
                            Pregled letova
                        </a>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="4">Nema avio tvrtki za prikaz.</td>
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
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Nadzorna ploča za račune</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Nadzorna ploča za račune</h1>
        <p class="page-subtitle">
            Pregled računa pristiglih putem JMS reda i upravljanje listom zaprimljenih računa.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                Administracijski status
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/korisnici">
                Nadzorna ploča za korisnike
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPloca">
                Nadzorna ploča za rezervacije
            </a>

            <a class="btn btn-danger"
               href="${pageContext.servletContext.contextPath}/mvc/admin/racuni/obrisiSve">
                Obriši sve račune iz liste
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

            List<Map<String, String>> racuni =
                (List<Map<String, String>>) request.getAttribute("racuni");

            int ukupnoRacuna = racuni == null ? 0 : racuni.size();
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Ukupno računa u listi</div>
                <div class="info-value"><%= ukupnoRacuna %></div>
            </div>
        </div>

        <h2 class="card-title">Popis računa</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID računa</th>
                    <th>Korisnik</th>
                    <th>Broj stavki</th>
                    <th>Iznos</th>
                    <th>Vrijeme</th>
                    <th>Stavke</th>
                    <th>Akcija</th>
                </tr>

                <%
                    if (racuni != null && !racuni.isEmpty()) {
                        int i = 0;

                        for (Map<String, String> r : racuni) {
                            i++;

                            String id = r.get("id");

                            if (id == null) {
                                id = "";
                            }

                            String idUrl = URLEncoder.encode(id, "UTF-8").replace("+", "%20");
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= r.get("id") == null ? "" : r.get("id") %></td>
                    <td><%= r.get("korisnik") == null ? "" : r.get("korisnik") %></td>
                    <td><%= r.get("brojstavki") == null ? "" : r.get("brojstavki") %></td>
                    <td><%= r.get("iznos") == null ? "" : r.get("iznos") %></td>
                    <td><%= r.get("vrijeme") == null ? "" : r.get("vrijeme") %></td>
                    <td>
                        <a class="link"
                           href="${pageContext.servletContext.contextPath}/mvc/admin/racuni/stavke/<%= idUrl %>">
                            Pregled stavki
                        </a>
                    </td>
                    <td>
                        <a class="link"
                           href="${pageContext.servletContext.contextPath}/mvc/admin/racuni/obrisi/<%= idUrl %>">
                            Obriši
                        </a>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="8">Nema računa u listi.</td>
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
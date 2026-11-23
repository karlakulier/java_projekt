<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Letovi avio tvrtke</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Letovi avio tvrtke: ${avioTvrtka}</h1>
        <p class="page-subtitle">Pregled letova i dohvat stvarnog leta na datum.</p>
    </div>

    <section class="card">
        <%
            String idUlaznice = (String) request.getAttribute("idUlaznice");
            if (idUlaznice == null) {
                idUlaznice = "";
            }
        %>

        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak?idUlaznice=<%= idUlaznice %>">
                Pregled avio tvrtki
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

            List<Map<String, String>> letovi =
                (List<Map<String, String>>) request.getAttribute("letovi");
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">ID ulaznice</div>
                <div class="info-value"><%= idUlaznice.isBlank() ? "-" : idUlaznice %></div>
            </div>
        </div>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Oznaka leta</th>
                    <th>ID leta</th>
                    <th>Vrijeme polijetanja</th>
                    <th>Polazni aerodrom</th>
                    <th>Odredišni aerodrom</th>
                    <th>Stvarni let na datum</th>
                </tr>

                <%
                    int i = 0;

                    if (letovi != null && !letovi.isEmpty()) {
                        for (Map<String, String> l : letovi) {
                            i++;
                            String oznakaLeta = l.get("oznakaLeta");
                            if (oznakaLeta == null) {
                                oznakaLeta = "";
                            }
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= oznakaLeta %></td>
                    <td><%= l.get("idLeta") == null ? "" : l.get("idLeta") %></td>
                    <td><%= l.get("vrijemePolijetanja") == null ? "" : l.get("vrijemePolijetanja") %></td>
                    <td><%= l.get("polazniAerodrom") == null ? "" : l.get("polazniAerodrom") %></td>
                    <td><%= l.get("odredisniAerodrom") == null ? "" : l.get("odredisniAerodrom") %></td>
                    <td>
                        <form class="inline-form" method="get" action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/let">
                            <input type="hidden" name="oznakaLeta" value="<%= oznakaLeta %>"/>
                            <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>"/>
                            <input class="medium-input" name="datum" placeholder="dd.mm.gggg."/>
                            <button class="btn" type="submit">Prikaži</button>
                        </form>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="7">Nema letova za prikaz.</td>
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
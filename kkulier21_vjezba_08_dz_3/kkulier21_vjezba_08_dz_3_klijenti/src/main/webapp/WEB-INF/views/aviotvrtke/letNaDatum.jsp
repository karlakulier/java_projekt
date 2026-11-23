<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Let na datum</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Let ${oznakaLeta} na datum ${datum}</h1>
        <p class="page-subtitle">Pregled stvarnog leta i dostupnih sjedala.</p>
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

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik?idUlaznice=<%= idUlaznice %>">
                Privatni dio za avio tvrtke
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
                    <th>Sjedala</th>
                </tr>

                <%
                    int i = 0;

                    if (letovi != null && !letovi.isEmpty()) {
                        for (Map<String, String> l : letovi) {
                            i++;
                            String idLeta = l.get("idLeta");

                            if (idLeta == null) {
                                idLeta = "";
                            }
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= l.get("oznakaLeta") == null ? "" : l.get("oznakaLeta") %></td>
                    <td><%= idLeta %></td>
                    <td><%= l.get("vrijemePolijetanja") == null ? "" : l.get("vrijemePolijetanja") %></td>
                    <td><%= l.get("polazniAerodrom") == null ? "" : l.get("polazniAerodrom") %></td>
                    <td><%= l.get("odredisniAerodrom") == null ? "" : l.get("odredisniAerodrom") %></td>
                    <td>
                        <%
                            if (!idLeta.isBlank()) {
                        %>
                            <a class="link" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/sjedala/<%= idLeta %>?idUlaznice=<%= idUlaznice %>">
                                Pregled sjedala
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
                    <td colspan="7">Nema leta za prikaz.</td>
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
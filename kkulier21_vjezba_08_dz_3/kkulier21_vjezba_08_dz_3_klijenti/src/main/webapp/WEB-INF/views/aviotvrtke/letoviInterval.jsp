<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Stvarni letovi u intervalu</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Stvarni letovi u intervalu</h1>
        <p class="page-subtitle">
            Pregled stvarnih letova za odabranu oznaku leta i vremenski interval.
        </p>
    </div>

    <section class="card">

        <%
            String oznakaLeta = (String) request.getAttribute("oznakaLeta");
            String idUlaznice = (String) request.getAttribute("idUlaznice");
            String odDatuma = (String) request.getAttribute("odDatuma");
            String doDatuma = (String) request.getAttribute("doDatuma");
            String atid = (String) request.getAttribute("atid");

            if (oznakaLeta == null) {
                oznakaLeta = "";
            }

            if (idUlaznice == null) {
                idUlaznice = "";
            }

            if (odDatuma == null) {
                odDatuma = "";
            }

            if (doDatuma == null) {
                doDatuma = "";
            }

            if (atid == null) {
                atid = "";
            }

            String idUlazniceZaUrl = "";
            if (!idUlaznice.isBlank()) {
                idUlazniceZaUrl = URLEncoder.encode(idUlaznice, "UTF-8")
                    .replace("+", "%20");
            }

            String oznakaLetaZaUrl = "";
            if (!oznakaLeta.isBlank()) {
                oznakaLetaZaUrl = URLEncoder.encode(oznakaLeta, "UTF-8")
                    .replace("+", "%20");
            }
        %>

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letovi?idUlaznice=<%= idUlazniceZaUrl %>">
                Pregled letova zastupnika
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik?idUlaznice=<%= idUlazniceZaUrl %>">
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
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Avio tvrtka</div>
                <div class="info-value"><%= atid.isBlank() ? "-" : atid %></div>
            </div>

            <div class="info-item">
                <div class="info-label">Oznaka leta</div>
                <div class="info-value"><%= oznakaLeta.isBlank() ? "-" : oznakaLeta %></div>
            </div>

            <div class="info-item">
                <div class="info-label">ID ulaznice</div>
                <div class="info-value"><%= idUlaznice.isBlank() ? "-" : idUlaznice %></div>
            </div>
        </div>

        <h2 class="card-title">Pretraživanje intervala</h2>

        <form method="get"
              action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letoviInterval">

            <input type="hidden" name="oznakaLeta" value="<%= oznakaLeta %>">
            <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">

            <div class="form-row">
                <div class="form-field">
                    <label for="odDatuma">Od datuma</label>
                    <input id="odDatuma"
                           type="text"
                           name="odDatuma"
                           value="<%= odDatuma %>"
                           placeholder="01.06.2026.">
                </div>

                <div class="form-field">
                    <label for="doDatuma">Do datuma</label>
                    <input id="doDatuma"
                           type="text"
                           name="doDatuma"
                           value="<%= doDatuma %>"
                           placeholder="30.06.2026.">
                </div>

                <div class="form-field">
                    <button class="btn" type="submit">
                        Dohvati stvarne letove
                    </button>
                </div>
            </div>
        </form>

        <%
            List<Map<String, String>> letovi =
                (List<Map<String, String>>) request.getAttribute("letovi");
        %>

        <h2 class="card-title">Rezultati</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID leta</th>
                    <th>Oznaka leta</th>
                    <th>Datum i vrijeme polijetanja</th>
                    <th>Polazni aerodrom</th>
                    <th>Odredišni aerodrom</th>
                    <th>Sjedala</th>
                </tr>

                <%
                    int i = 0;

                    if (letovi != null && !letovi.isEmpty()) {
                        for (Map<String, String> let : letovi) {
                            i++;

                            String idLeta = let.get("idLeta");
                            String oznakaLetaRed = let.get("oznakaLeta");
                            String vrijemePolijetanja = let.get("vrijemePolijetanja");
                            String polazniAerodrom = let.get("polazniAerodrom");
                            String odredisniAerodrom = let.get("odredisniAerodrom");

                            if (idLeta == null) {
                                idLeta = "";
                            }

                            if (oznakaLetaRed == null) {
                                oznakaLetaRed = "";
                            }

                            if (vrijemePolijetanja == null) {
                                vrijemePolijetanja = "";
                            }

                            if (polazniAerodrom == null) {
                                polazniAerodrom = "";
                            }

                            if (odredisniAerodrom == null) {
                                odredisniAerodrom = "";
                            }

                            String oznakaLetaZaSjedala = "";
                            if (!oznakaLetaRed.isBlank()) {
                                oznakaLetaZaSjedala = URLEncoder.encode(oznakaLetaRed, "UTF-8")
                                    .replace("+", "%20");
                            }
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= idLeta %></td>
                    <td><%= oznakaLetaRed %></td>
                    <td><%= vrijemePolijetanja %></td>
                    <td><%= polazniAerodrom %></td>
                    <td><%= odredisniAerodrom %></td>
                    <td>
                        <%
                            if (!idLeta.isBlank()) {
                        %>
                            <a class="link"
                               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/sjedala/<%= idLeta %>?oznakaLeta=<%= oznakaLetaZaSjedala %>&idUlaznice=<%= idUlazniceZaUrl %>">
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
                    <td colspan="7">Nema stvarnih letova za prikaz.</td>
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
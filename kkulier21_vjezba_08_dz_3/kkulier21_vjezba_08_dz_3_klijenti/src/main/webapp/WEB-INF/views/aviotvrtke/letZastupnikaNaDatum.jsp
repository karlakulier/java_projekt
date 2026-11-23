<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Let zastupnika na datum</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Let zastupnika na datum</h1>
        <p class="page-subtitle">
            Pregled stvarnog leta na odabrani datum i zatvaranje sjedala po redovima.
        </p>
    </div>

    <section class="card">

        <%
            String oznakaLeta = (String) request.getAttribute("oznakaLeta");
            String idUlaznice = (String) request.getAttribute("idUlaznice");
            String datum = (String) request.getAttribute("datum");
            String atid = (String) request.getAttribute("atid");

            if (oznakaLeta == null) {
                oznakaLeta = "";
            }

            if (idUlaznice == null) {
                idUlaznice = "";
            }

            if (datum == null) {
                datum = "";
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

            String datumZaUrl = "";
            if (!datum.isBlank()) {
                datumZaUrl = URLEncoder.encode(datum, "UTF-8")
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
                <div class="info-label">Avio tvrtka zastupnika</div>
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

            <div class="info-item">
                <div class="info-label">Datum</div>
                <div class="info-value"><%= datum.isBlank() ? "-" : datum %></div>
            </div>
        </div>

        <h2 class="card-title">Dohvat leta na datum</h2>

        <form method="get"
              action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letNaDatum">

            <input type="hidden" name="oznakaLeta" value="<%= oznakaLeta %>">
            <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">

            <div class="form-row">
                <div class="form-field">
                    <label for="datum">Datum</label>
                    <input id="datum"
                           type="text"
                           name="datum"
                           value="<%= datum %>"
                           placeholder="01.06.2026.">
                </div>

                <div class="form-field">
                    <button class="btn" type="submit">
                        Prikaži let
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
                    <th>Slobodna</th>
                    <th>Rezervirana</th>
                    <th>Potvrđena</th>
                    <th>Zatvorena</th>
                    <th>Nevažeća</th>
                    <th>Zatvaranje sjedala</th>
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
                            String slobodna = let.get("slobodna");
                            String rezervirana = let.get("rezervirana");
                            String potvrdjena = let.get("potvrdjena");
                            String zatvorena = let.get("zatvorena");
                            String nevazeca = let.get("nevazeca");

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

                            if (slobodna == null) {
                                slobodna = "0";
                            }

                            if (rezervirana == null) {
                                rezervirana = "0";
                            }

                            if (potvrdjena == null) {
                                potvrdjena = "0";
                            }

                            if (zatvorena == null) {
                                zatvorena = "0";
                            }

                            if (nevazeca == null) {
                                nevazeca = "0";
                            }

                            String idLetaZaUrl = "";
                            if (!idLeta.isBlank()) {
                                idLetaZaUrl = URLEncoder.encode(idLeta, "UTF-8")
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
                    <td><%= slobodna %></td>
                    <td><%= rezervirana %></td>
                    <td><%= potvrdjena %></td>
                    <td><%= zatvorena %></td>
                    <td><%= nevazeca %></td>
                    <td>
                        <%
                            if (!idLeta.isBlank()) {
                        %>
                            <form class="inline-form"
                                  method="get"
                                  action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/zatvoriSjedala/<%= idLetaZaUrl %>">

                                <input type="hidden" name="oznakaLeta" value="<%= oznakaLeta %>">
                                <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">
                                <input type="hidden" name="datum" value="<%= datum %>">

                                <input class="small-input"
                                       type="text"
                                       name="odReda"
                                       placeholder="Od reda">

                                <input class="small-input"
                                       type="text"
                                       name="doReda"
                                       placeholder="Do reda">

                                <button class="btn btn-danger" type="submit">
                                    Zatvori
                                </button>
                            </form>
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
                    <td colspan="12">Nema leta za prikaz.</td>
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
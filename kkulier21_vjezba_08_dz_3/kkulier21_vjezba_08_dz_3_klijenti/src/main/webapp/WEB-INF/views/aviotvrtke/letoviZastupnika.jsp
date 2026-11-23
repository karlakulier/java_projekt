<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="java.net.URLEncoder" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Letovi avio tvrtke zastupnika</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Letovi avio tvrtke zastupnika</h1>
        <p class="page-subtitle">
            Pregled letova avio tvrtke zastupnika, inicijalizacija letova i zatvaranje sjedala u intervalu.
        </p>
    </div>

    <section class="card">

        <%
            String idUlaznice = (String) request.getAttribute("idUlaznice");
            String atid = (String) request.getAttribute("atid");

            if (idUlaznice == null) {
                idUlaznice = "";
            }

            if (atid == null) {
                atid = "";
            }

            String idUlazniceZaUrl = "";
            if (!idUlaznice.isBlank()) {
                idUlazniceZaUrl = URLEncoder.encode(idUlaznice, "UTF-8")
                    .replace("+", "%20");
            }
        %>

        <div class="actions">
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
                <div class="info-label">ID ulaznice</div>
                <div class="info-value"><%= idUlaznice.isBlank() ? "-" : idUlaznice %></div>
            </div>
        </div>

        <h2 class="card-title">Dohvat letova</h2>

        <form method="get"
              action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letovi">

            <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">

            <div class="actions">
                <button class="btn" type="submit">Dohvati letove</button>
            </div>
        </form>

        <section class="card" style="box-shadow: none;">
            <h2 class="card-title">Inicijalizacija svih letova avio tvrtke u intervalu</h2>

            <form method="get"
                  action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/inicijalizirajLetove">

                <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">

                <div class="form-row">
                    <div class="form-field">
                        <label for="odDatumaSve">Od datuma</label>
                        <input id="odDatumaSve"
                               class="small-input"
                               type="text"
                               name="odDatuma"
                               placeholder="01.06.2026.">
                    </div>

                    <div class="form-field">
                        <label for="doDatumaSve">Do datuma</label>
                        <input id="doDatumaSve"
                               class="small-input"
                               type="text"
                               name="doDatuma"
                               placeholder="30.06.2026.">
                    </div>

                    <div class="form-field">
                        <button class="btn" type="submit">
                            Inicijaliziraj sve letove
                        </button>
                    </div>
                </div>
            </form>
        </section>

        <%
            List<Map<String, String>> letovi =
                (List<Map<String, String>>) request.getAttribute("letovi");
        %>

        <h2 class="card-title">Popis letova</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Oznaka leta</th>
                    <th>ID leta</th>
                    <th>Vrijeme polijetanja</th>
                    <th>Polazni aerodrom</th>
                    <th>Odredišni aerodrom</th>
                    <th>Let na datum</th>
                    <th>Inicijalizacija leta</th>
                    <th>Zatvaranje u intervalu</th>
                    <th>Stvarni letovi u intervalu</th>
                </tr>

                <%
                    int i = 0;

                    if (letovi != null && !letovi.isEmpty()) {
                        for (Map<String, String> let : letovi) {
                            i++;

                            String oznakaLeta = let.get("oznakaLeta");
                            String idLeta = let.get("idLeta");
                            String vrijemePolijetanja = let.get("vrijemePolijetanja");
                            String polazniAerodrom = let.get("polazniAerodrom");
                            String odredisniAerodrom = let.get("odredisniAerodrom");

                            if (oznakaLeta == null) {
                                oznakaLeta = "";
                            }

                            if (idLeta == null) {
                                idLeta = "";
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

                            String oznakaLetaZaUrl = "";
                            if (!oznakaLeta.isBlank()) {
                                oznakaLetaZaUrl = URLEncoder.encode(oznakaLeta, "UTF-8")
                                    .replace("+", "%20");
                            }
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= oznakaLeta %></td>
                    <td><%= idLeta %></td>
                    <td><%= vrijemePolijetanja %></td>
                    <td><%= polazniAerodrom %></td>
                    <td><%= odredisniAerodrom %></td>

                    <td>
                        <%
                            if (!oznakaLeta.isBlank()) {
                        %>
                            <form class="inline-form"
                                  method="get"
                                  action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letNaDatum">

                                <input type="hidden" name="oznakaLeta" value="<%= oznakaLeta %>">
                                <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">

                                <input class="medium-input"
                                       type="text"
                                       name="datum"
                                       placeholder="dd.mm.gggg.">

                                <button class="btn" type="submit">Prikaži</button>
                            </form>
                        <%
                            } else {
                        %>
                            -
                        <%
                            }
                        %>
                    </td>

                    <td>
                        <%
                            if (!oznakaLeta.isBlank()) {
                        %>
                            <form class="inline-form"
                                  method="get"
                                  action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/inicijalizirajLet">

                                <input type="hidden" name="oznakaLeta" value="<%= oznakaLeta %>">
                                <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">

                                <input class="small-input"
                                       type="text"
                                       name="odDatuma"
                                       placeholder="Od">

                                <input class="small-input"
                                       type="text"
                                       name="doDatuma"
                                       placeholder="Do">

                                <button class="btn" type="submit">Inicijaliziraj</button>
                            </form>
                        <%
                            } else {
                        %>
                            -
                        <%
                            }
                        %>
                    </td>

                    <td>
                        <%
                            if (!oznakaLeta.isBlank()) {
                        %>
                            <form class="inline-form"
                                  method="get"
                                  action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/zatvoriSjedalaInterval">

                                <input type="hidden" name="oznakaLeta" value="<%= oznakaLeta %>">
                                <input type="hidden" name="idUlaznice" value="<%= idUlaznice %>">

                                <input class="small-input"
                                       type="text"
                                       name="odDatuma"
                                       placeholder="Od dat.">

                                <input class="small-input"
                                       type="text"
                                       name="doDatuma"
                                       placeholder="Do dat.">

                                <input class="small-input"
                                       type="text"
                                       name="odReda"
                                       placeholder="Od reda">

                                <input class="small-input"
                                       type="text"
                                       name="doReda"
                                       placeholder="Do reda">

                                <button class="btn btn-danger" type="submit">Zatvori</button>
                            </form>
                        <%
                            } else {
                        %>
                            -
                        <%
                            }
                        %>
                    </td>

                    <td>
                        <%
                            if (!oznakaLeta.isBlank()) {
                        %>
                            <a class="link"
                               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letoviInterval?oznakaLeta=<%= oznakaLetaZaUrl %>&idUlaznice=<%= idUlazniceZaUrl %>">
                                Pregled u intervalu
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
                    <td colspan="10">Nema letova za prikaz.</td>
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
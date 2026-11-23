<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Letovi putnika</title>
   <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Letovi putnika</h1>
        <p class="page-subtitle">
            Pregled letova za odabranog putnika u zadanom vremenskom intervalu.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/putnici">
                Pregled putnika
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik">
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

            String putnikId = (String) request.getAttribute("putnikId");
            String idUlaznice = (String) request.getAttribute("idUlaznice");
            String odDatuma = (String) request.getAttribute("odDatuma");
            String doDatuma = (String) request.getAttribute("doDatuma");
            String atid = (String) request.getAttribute("atid");

            Map<String, String> putnik =
                (Map<String, String>) request.getAttribute("putnik");
        %>

        <%
            if (putnik != null) {
        %>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Putnik</div>
                    <div class="info-value">
                        <%= putnik.get("ime") == null ? "" : putnik.get("ime") %>
                        <%= putnik.get("prezime") == null ? "" : putnik.get("prezime") %>
                    </div>
                </div>

                <div class="info-item">
                    <div class="info-label">Korisnik</div>
                    <div class="info-value">
                        <%= putnik.get("korisnik") == null ? "-" : putnik.get("korisnik") %>
                    </div>
                </div>

                <div class="info-item">
                    <div class="info-label">Email</div>
                    <div class="info-value">
                        <%= putnik.get("email") == null ? "-" : putnik.get("email") %>
                    </div>
                </div>

                <div class="info-item">
                    <div class="info-label">Avio tvrtka zastupnika</div>
                    <div class="info-value">
                        <%= atid == null || atid.isBlank() ? "-" : atid %>
                    </div>
                </div>
            </div>
        <%
            } else {
        %>
            <div class="alert alert-error">
                Putnik nije odabran. Vrati se na pregled putnika i klikni Odaberi.
            </div>

            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Avio tvrtka zastupnika</div>
                    <div class="info-value">
                        <%= atid == null || atid.isBlank() ? "-" : atid %>
                    </div>
                </div>
            </div>
        <%
            }
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">ID ulaznice</div>
                <div class="info-value">
                    <%= idUlaznice == null || idUlaznice.isBlank() ? "-" : idUlaznice %>
                </div>
            </div>
        </div>

        <h2 class="card-title">Pretraživanje letova putnika</h2>

        <form method="get"
              action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letoviPutnika/<%= putnikId == null ? "" : putnikId %>">

            <div class="form-row">
                <div class="form-field">
                    <label for="odDatuma">Od datuma</label>
                    <input id="odDatuma"
                           type="text"
                           name="odDatuma"
                           value="<%= odDatuma == null ? "" : odDatuma %>"
                           placeholder="01.06.2026.">
                </div>

                <div class="form-field">
                    <label for="doDatuma">Do datuma</label>
                    <input id="doDatuma"
                           type="text"
                           name="doDatuma"
                           value="<%= doDatuma == null ? "" : doDatuma %>"
                           placeholder="30.06.2026.">
                </div>

                <div class="form-field">
                    <button class="btn" type="submit">
                        Dohvati letove putnika
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
                    <th>Oznaka leta</th>
                    <th>ID leta</th>
                    <th>Datum i vrijeme polijetanja</th>
                    <th>Polazni aerodrom</th>
                    <th>Odredišni aerodrom</th>
                </tr>

                <%
                    int i = 0;

                    if (letovi != null && !letovi.isEmpty()) {
                        for (Map<String, String> l : letovi) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= l.get("oznakaLeta") == null ? "" : l.get("oznakaLeta") %></td>
                    <td><%= l.get("idLeta") == null ? "" : l.get("idLeta") %></td>
                    <td><%= l.get("vrijemePolijetanja") == null ? "" : l.get("vrijemePolijetanja") %></td>
                    <td><%= l.get("polazniAerodrom") == null ? "" : l.get("polazniAerodrom") %></td>
                    <td><%= l.get("odredisniAerodrom") == null ? "" : l.get("odredisniAerodrom") %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="6">
                        Nema letova putnika za prikaz.
                    </td>
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
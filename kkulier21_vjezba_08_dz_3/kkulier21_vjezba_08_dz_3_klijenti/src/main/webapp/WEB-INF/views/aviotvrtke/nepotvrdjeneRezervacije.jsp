<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Nepotvrđene rezervacije zastupnika</title>
   <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Nepotvrđene rezervacije zastupnika</h1>
        <p class="page-subtitle">
            Pregled nepotvrđenih rezervacija za odabranog putnika i mogućnost potvrđivanja rezervacije.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik">
                Privatni dio za avio tvrtke
            </a>

            <a class="btn"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/racuni">
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

            Map<String, String> putnik = (Map<String, String>) request.getAttribute("putnik");
            Boolean imaOtvorenRacun = (Boolean) request.getAttribute("imaOtvorenRacun");
        %>

        <%
            if (putnik != null) {
        %>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Odabrani putnik</div>
                    <div class="info-value">
                        <%= putnik.get("ime") == null ? "" : putnik.get("ime") %>
                        <%= putnik.get("prezime") == null ? "" : putnik.get("prezime") %>
                    </div>
                </div>

                <div class="info-item">
                    <div class="info-label">Email putnika</div>
                    <div class="info-value">
                        <%= putnik.get("email") == null ? "-" : putnik.get("email") %>
                    </div>
                </div>

                <div class="info-item">
                    <div class="info-label">Otvoreni račun</div>
                    <div class="info-value">
                        <%= imaOtvorenRacun != null && imaOtvorenRacun ? "Da" : "Ne" %>
                    </div>
                </div>
            </div>
        <%
            } else {
        %>
            <div class="alert alert-error">
                Putnik nije odabran.
            </div>

            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Otvoreni račun</div>
                    <div class="info-value">
                        <%= imaOtvorenRacun != null && imaOtvorenRacun ? "Da" : "Ne" %>
                    </div>
                </div>
            </div>
        <%
            }

            if (imaOtvorenRacun == null || !imaOtvorenRacun) {
        %>
            <div class="alert alert-error">
                Ne postoji otvoreni račun zastupnika.
            </div>
        <%
            }

            List<Map<String, String>> rezervacije =
                (List<Map<String, String>>) request.getAttribute("rezervacije");
        %>

        <h2 class="card-title">Popis nepotvrđenih rezervacija</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID rezervacije sjedala</th>
                    <th>ID leta</th>
                    <th>Oznaka leta</th>
                    <th>Datum i vrijeme polijetanja</th>
                    <th>Polazni aerodrom</th>
                    <th>Odredišni aerodrom</th>
                    <th>Razred</th>
                    <th>Putnik ime</th>
                    <th>Putnik prezime</th>
                    <th>Email putnika</th>
                    <th>Potvrđivanje</th>
                </tr>

                <%
                    int i = 0;

                    if (rezervacije != null && !rezervacije.isEmpty()) {
                        for (Map<String, String> r : rezervacije) {
                            i++;

                            String idRezervacijeSjedala = r.get("idRezervacijeSjedala");
                            String idLeta = r.get("idLeta");
                            String oznakaLeta = r.get("oznakaLeta");
                            String vrijemePolijetanja = r.get("vrijemePolijetanja");
                            String polazniAerodrom = r.get("polazniAerodrom");
                            String odredisniAerodrom = r.get("odredisniAerodrom");
                            String razred = r.get("razred");
                            String putnikIme = r.get("putnikIme");
                            String putnikPrezime = r.get("putnikPrezime");
                            String putnikEmail = r.get("putnikEmail");

                            if (idRezervacijeSjedala == null) {
                                idRezervacijeSjedala = "";
                            }

                            if (idLeta == null) {
                                idLeta = "";
                            }

                            if (oznakaLeta == null) {
                                oznakaLeta = "";
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

                            if (razred == null) {
                                razred = "";
                            }

                            if (putnikIme == null) {
                                putnikIme = "";
                            }

                            if (putnikPrezime == null) {
                                putnikPrezime = "";
                            }

                            if (putnikEmail == null) {
                                putnikEmail = "";
                            }
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= idRezervacijeSjedala %></td>
                    <td><%= idLeta %></td>
                    <td><%= oznakaLeta %></td>
                    <td><%= vrijemePolijetanja %></td>
                    <td><%= polazniAerodrom %></td>
                    <td><%= odredisniAerodrom %></td>
                    <td><span class="badge"><%= razred %></span></td>
                    <td><%= putnikIme %></td>
                    <td><%= putnikPrezime %></td>
                    <td><%= putnikEmail %></td>
                    <td>
                        <%
                            if (imaOtvorenRacun != null && imaOtvorenRacun
                                && putnik != null
                                && idRezervacijeSjedala != null
                                && !idRezervacijeSjedala.isBlank()) {
                        %>
                            <a class="link"
                               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/potvrdiRezervaciju/<%= idRezervacijeSjedala %>">
                                Potvrdi
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
                    <td colspan="12">Nema nepotvrđenih rezervacija za prikaz.</td>
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
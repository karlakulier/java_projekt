<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Pregled putnika</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled putnika</h1>
        <p class="page-subtitle">
            Pregled putnika, odabir putnika za rezervaciju i upravljanje statusom zastupnika.
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

            <a class="btn"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/nepotvrdjeneRezervacije">
                Nepotvrđene rezervacije
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

            Integer odStranice = (Integer) request.getAttribute("odStranice");
            Integer prethodnaStranica = (Integer) request.getAttribute("prethodnaStranica");
            Integer sljedecaStranica = (Integer) request.getAttribute("sljedecaStranica");
            Integer brojRedaka = (Integer) request.getAttribute("brojRedaka");
            String atid = (String) request.getAttribute("atid");

            if (odStranice == null || odStranice < 1) {
                odStranice = 1;
            }

            if (brojRedaka == null || brojRedaka < 1) {
                brojRedaka = 7;
            }

            if (prethodnaStranica == null || prethodnaStranica < 1) {
                prethodnaStranica = odStranice > 1 ? odStranice - 1 : 1;
            }

            if (sljedecaStranica == null || sljedecaStranica < 1) {
                sljedecaStranica = odStranice + 1;
            }

            int pocetniRedniBroj = (odStranice - 1) * brojRedaka;

            Map<String, String> odabraniPutnik =
                (Map<String, String>) request.getAttribute("putnik");
        %>

        <%
            if (odabraniPutnik != null) {
        %>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Odabrani putnik</div>
                    <div class="info-value">
                        <%= odabraniPutnik.get("ime") == null ? "" : odabraniPutnik.get("ime") %>
                        <%= odabraniPutnik.get("prezime") == null ? "" : odabraniPutnik.get("prezime") %>
                    </div>
                </div>

                <div class="info-item">
                    <div class="info-label">Korisnik</div>
                    <div class="info-value">
                        <%= odabraniPutnik.get("korisnik") == null ? "-" : odabraniPutnik.get("korisnik") %>
                    </div>
                </div>

                <div class="info-item">
                    <div class="info-label">Email</div>
                    <div class="info-value">
                        <%= odabraniPutnik.get("email") == null ? "-" : odabraniPutnik.get("email") %>
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
                Putnik još nije odabran. Klikni "Odaberi" kod putnika za kojeg želiš raditi rezervaciju.
            </div>

            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">Avio tvrtka prijavljenog zastupnika</div>
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
                <div class="info-label">Trenutna stranica</div>
                <div class="info-value"><%= odStranice %></div>
            </div>

            <div class="info-item">
                <div class="info-label">Broj redaka po stranici</div>
                <div class="info-value"><%= brojRedaka %></div>
            </div>
        </div>

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/putnici?odStranice=<%= prethodnaStranica %>">
                Prethodna stranica
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/putnici?odStranice=<%= sljedecaStranica %>">
                Sljedeća stranica
            </a>
        </div>

        <%
            List<Map<String, String>> putnici =
                (List<Map<String, String>>) request.getAttribute("putnici");
        %>

        <h2 class="card-title">Popis putnika</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Korisnik</th>
                    <th>Ime</th>
                    <th>Prezime</th>
                    <th>Email</th>
                    <th>Odabir putnika</th>
                    <th>Letovi putnika</th>
                    <th>Status zastupnika</th>
                    <th>Akcija zastupnika</th>
                </tr>

                <%
                    int i = 0;

                    if (putnici != null && !putnici.isEmpty()) {
                        for (Map<String, String> p : putnici) {
                            i++;

                            int redniBroj = pocetniRedniBroj + i;

                            String korisnik = p.get("korisnik");
                            String ime = p.get("ime");
                            String prezime = p.get("prezime");
                            String email = p.get("email");

                            String zastupnikBiloKoje = p.get("zastupnikBiloKoje");
                            String zastupnikIste = p.get("zastupnikIste");

                            boolean jeZastupnikBiloKoje = "true".equals(zastupnikBiloKoje);
                            boolean jeZastupnikIste = "true".equals(zastupnikIste);

                            boolean jeOdabraniPutnik = false;

                            if (odabraniPutnik != null
                                && korisnik != null
                                && korisnik.equals(odabraniPutnik.get("korisnik"))) {
                                jeOdabraniPutnik = true;
                            }
                %>
                <tr>
                    <td><%= redniBroj %></td>
                    <td><%= korisnik == null ? "" : korisnik %></td>
                    <td><%= ime == null ? "" : ime %></td>
                    <td><%= prezime == null ? "" : prezime %></td>
                    <td><%= email == null ? "" : email %></td>

                    <td>
                        <%
                            if (korisnik != null && !korisnik.isBlank()) {
                                if (jeOdabraniPutnik) {
                        %>
                                    <span class="badge">Odabran</span>
                        <%
                                } else {
                        %>
                                    <a class="link"
                                       href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/odaberiPutnika/<%= korisnik %>?odStranice=<%= odStranice %>">
                                        Odaberi
                                    </a>
                        <%
                                }
                            } else {
                        %>
                                -
                        <%
                            }
                        %>
                    </td>

                    <td>
                        <%
                            if (korisnik != null && !korisnik.isBlank()) {
                        %>
                            <a class="link"
                               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/letoviPutnika/<%= korisnik %>">
                                Pregled letova
                            </a>
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
                            if (jeZastupnikIste) {
                        %>
                            <span class="badge">Zastupnik ove avio tvrtke</span>
                        <%
                            } else if (jeZastupnikBiloKoje) {
                        %>
                            <span class="badge">Zastupnik druge avio tvrtke</span>
                        <%
                            } else {
                        %>
                            Nije zastupnik
                        <%
                            }
                        %>
                    </td>

                    <td>
                        <%
                            if (korisnik == null || korisnik.isBlank()) {
                        %>
                            -
                        <%
                            } else if (jeZastupnikIste) {
                        %>
                            <a class="link"
                               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/obrisiZastupnika/<%= korisnik %>?odStranice=<%= odStranice %>">
                                Obriši kao zastupnika
                            </a>
                        <%
                            } else if (!jeZastupnikBiloKoje) {
                        %>
                            <a class="link"
                               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/dodajZastupnika/<%= korisnik %>?odStranice=<%= odStranice %>">
                                Dodaj kao zastupnika
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
                    <td colspan="9">
                        Nema pronađenih putnika za prikaz.
                    </td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>

        <div class="actions" style="margin-top: 16px;">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/putnici?odStranice=<%= prethodnaStranica %>">
                Prethodna stranica
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik/putnici?odStranice=<%= sljedecaStranica %>">
                Sljedeća stranica
            </a>
        </div>

    </section>

</main>
</body>
</html>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sjedala leta</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Sjedala za let ${idLeta}</h1>
        <p class="page-subtitle">
            Pregled statusa sjedala i rezervacija slobodnih sjedala na odabranom letu.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak?idUlaznice=${idUlaznice}">
                Pregled avio tvrtki
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");
            Object oPogreska = request.getAttribute("pogreska");

            if (poruka != null && poruka.length() > 0) {
                boolean pogreska = oPogreska != null && (Boolean) oPogreska;
                String klasaPoruke = pogreska ? "alert alert-error" : "alert alert-success";
        %>
            <div class="<%= klasaPoruke %>"><%= poruka %></div>
        <%
            }

            Boolean imaOtvorenRacun = (Boolean) request.getAttribute("imaOtvorenRacun");

            if (imaOtvorenRacun == null || !imaOtvorenRacun) {
        %>
            <div class="alert alert-error">
                Nema otvorenog računa. Rezervacija slobodnih sjedala nije dostupna.
            </div>
        <%
            } else {
        %>
            <div class="alert alert-success">
                Postoji otvoreni račun. Slobodna sjedala mogu se rezervirati klikom na oznaku sjedala.
            </div>
        <%
            }

            List<Map<String, String>> sjedala =
                (List<Map<String, String>>) request.getAttribute("sjedala");

            String trenutniRed = "";
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">ID leta</div>
                <div class="info-value">${idLeta}</div>
            </div>

            <div class="info-item">
                <div class="info-label">ID ulaznice</div>
                <div class="info-value">${idUlaznice}</div>
            </div>

            <div class="info-item">
                <div class="info-label">Otvoreni račun</div>
                <div class="info-value">
                    <%= imaOtvorenRacun != null && imaOtvorenRacun ? "Da" : "Ne" %>
                </div>
            </div>
        </div>

        <h2 class="card-title">Raspored sjedala</h2>

        <div class="seat-layout">

        <%
            if (sjedala != null && !sjedala.isEmpty()) {
                for (Map<String, String> s : sjedala) {
                    String red = s.get("red");
                    String oznakaSjedala = s.get("sjedalo");
                    String status = s.get("status");
                    String razred = s.get("razred");

                    if (red == null) {
                        red = "";
                    }

                    if (oznakaSjedala == null) {
                        oznakaSjedala = "";
                    }

                    if (status == null) {
                        status = "";
                    }

                    if (razred == null) {
                        razred = "";
                    }

                    String oznakaRazreda = "";

                    if ("EKONOMSKI".equalsIgnoreCase(razred)) {
                        oznakaRazreda = "E";
                    } else if ("POSLOVNI".equalsIgnoreCase(razred)) {
                        oznakaRazreda = "P";
                    } else {
                        oznakaRazreda = razred;
                    }

                    if (oznakaRazreda == null) {
                        oznakaRazreda = "";
                    }

                    if (!red.equals(trenutniRed)) {
                        if (!trenutniRed.isBlank()) {
        %>
                            </div>
        <%
                        }

                        trenutniRed = red;
        %>
                        <div class="seat-row-title">Red <%= trenutniRed %></div>
                        <div class="seat-row">
        <%
                    }

                    String statusLower = status.toLowerCase().trim();

                    statusLower = statusLower.replace("\"", "");
                    statusLower = statusLower.replace(",", "");
                    statusLower = statusLower.replace("statusrezervacije", "");
                    statusLower = statusLower.replace(":", "");
                    statusLower = statusLower.trim();

                    String klasa = "nepoznato";

                    if (statusLower.startsWith("s") || statusLower.contains("slobod")) {
                        klasa = "slobodno";
                    } else if (statusLower.startsWith("r")
                            || statusLower.contains("rezerv")
                            || statusLower.contains("kreir")) {
                        klasa = "rezervirano";
                    } else if (statusLower.startsWith("p")
                            || statusLower.contains("potvr")) {
                        klasa = "potvrdeno";
                    } else if (statusLower.startsWith("z")
                            || statusLower.contains("zatvor")) {
                        klasa = "zatvoreno";
                    } else if (statusLower.startsWith("n")
                            || statusLower.contains("neva")
                            || statusLower.contains("invalid")) {
                        klasa = "nevazece";
                    }

                    boolean slobodno = "slobodno".equals(klasa);
                    boolean mozeRezervirati =
                        slobodno && imaOtvorenRacun != null && imaOtvorenRacun
                        && oznakaRazreda.length() > 0;
        %>
                    <span class="sjedalo <%= klasa %>"
                          title="Status: <%= status %>, razred: <%= razred %>">
                        <%
                            if (mozeRezervirati) {
                        %>
                            <form method="post"
                                  action="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/rezerviraj/${idLeta}/<%= oznakaRazreda %>?idUlaznice=${idUlaznice}">
                                <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}">
                                <button type="submit"><%= oznakaSjedala %></button>
                            </form>
                        <%
                            } else {
                        %>
                            <%= oznakaSjedala %>
                        <%
                            }
                        %>
                    </span>
        <%
                }

                if (!trenutniRed.isBlank()) {
        %>
                    </div>
        <%
                }
            } else {
        %>
                <div class="alert alert-error">
                    Nema sjedala za prikaz.
                </div>
        <%
            }
        %>

        </div>

        <h2 class="card-title">Legenda</h2>

        <div class="legend">
            <div><span class="sjedalo slobodno">S</span> slobodno</div>
            <div><span class="sjedalo rezervirano">R</span> rezervirano, nije potvrđeno</div>
            <div><span class="sjedalo potvrdeno">P</span> potvrđeno</div>
            <div><span class="sjedalo zatvoreno">Z</span> zatvoreno</div>
            <div><span class="sjedalo nevazece">N</span> nevažeće</div>
            <div><span class="sjedalo nepoznato">?</span> nepoznato</div>
        </div>

    </section>

</main>
</body>
</html>
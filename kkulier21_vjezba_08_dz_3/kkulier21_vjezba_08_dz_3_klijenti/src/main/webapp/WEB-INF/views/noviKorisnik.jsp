<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>REST MVC - Dodavanje korisnika</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Dodavanje korisnika</h1>
        <p class="page-subtitle">Unos podataka za kreiranje novog korisnika.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/korisnici/ispisKorisnika">
                Ispis svih korisnika
            </a>
        </div>

        <%
            if (request.getAttribute("poruka") != null) {
                String poruka = (String) request.getAttribute("poruka");
                Object oPogreska = request.getAttribute("pogreska");

                boolean pogreska = false;

                if (oPogreska != null) {
                    pogreska = (Boolean) oPogreska;
                }

                if (poruka.length() > 0) {
                    String klasa = pogreska ? "alert alert-error" : "alert alert-success";
        %>
                    <div class="<%= klasa %>"><%= poruka %></div>
        <%
                }
            }
        %>

        <form method="post" action="${pageContext.servletContext.contextPath}/mvc/korisnici/dodajKorisnika">
            <div class="form-row">
                <div class="form-field">
                    <label for="korisnik">Korisnik</label>
                    <input id="korisnik" name="korisnik" type="text" value="${korId}">
                    <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}">
                </div>

                <div class="form-field">
                    <label for="lozinka">Lozinka</label>
                    <input id="lozinka" name="lozinka" type="password">
                </div>
            </div>

            <div class="form-row">
                <div class="form-field">
                    <label for="ime">Ime</label>
                    <input id="ime" name="ime" type="text" value="${ime}">
                </div>

                <div class="form-field">
                    <label for="prezime">Prezime</label>
                    <input id="prezime" name="prezime" type="text" value="${prezime}">
                </div>

                <div class="form-field">
                    <label for="email">Email</label>
                    <input id="email" name="email" type="text" value="${email}">
                </div>
            </div>

            <button class="btn" type="submit">Dodaj korisnika</button>
        </form>
    </section>

</main>
</body>
</html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>REST MVC - Početna stranica</title>
   <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">REST MVC - Početna stranica</h1>
        <p class="page-subtitle">Pregled glavnih grupa pogleda i funkcionalnosti aplikacije.</p>
    </div>

    <c:if test="${korisnik != null}">
        <div class="alert alert-success">
            Prijavljen je korisnik: <strong>${korisnik}</strong>
        </div>
    </c:if>

    <div class="grid">

        <section class="card">
            <h2 class="card-title">Prijava i odjava</h2>

            <ul class="nav-list">
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/prijava/prijavaKorisnika">
                        Prijava korisnika
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/prijava/odjavaKorisnika">
                        Odjava korisnika
                    </a>
                </li>
            </ul>
        </section>

        <section class="card">
            <h2 class="card-title">Korisnici</h2>

            <ul class="nav-list">
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/noviKorisnik">
                        Dodavanje novog korisnika
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/ispisKorisnika">
                        Ispis svih korisnika
                    </a>
                </li>
            </ul>

            <h3>Pretraživanje korisnika</h3>

            <form method="post" action="${pageContext.servletContext.contextPath}/mvc/korisnici/pretrazivanjeKorisnika">
                <div class="form-row">
                    <div class="form-field">
                        <label for="ime">Ime</label>
                        <input id="ime" name="ime" type="text">
                        <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}">
                    </div>

                    <div class="form-field">
                        <label for="prezime">Prezime</label>
                        <input id="prezime" name="prezime" type="text">
                    </div>

                    <div class="form-field">
                        <button class="btn" type="submit">Dohvati korisnike</button>
                    </div>
                </div>
            </form>
        </section>

        <section class="card">
            <h2 class="card-title">Avio tvrtke</h2>

            <ul class="nav-list">
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/ispisAvioTvrtki">
                        Pregled avio tvrtki
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">
                        Pregled avio tvrtki, letova i sjedala
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/zastupnik">
                        Privatni dio za avio tvrtke
                    </a>
                </li>
            </ul>
        </section>

        <section class="card">
            <h2 class="card-title">Rezervacije</h2>

            <ul class="nav-list">
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">
                        Početna rezervacije
                    </a>
                </li>
            </ul>
        </section>

        <section class="card">
            <h2 class="card-title">JMS poruke</h2>

            <ul class="nav-list">
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pregledRedaPoruka">
                        Pregled reda poruka
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/obrisiRedPoruka">
                        Obriši red poruka
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pregledPorukaTema">
                        Pregled poruka za temu
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/obrisiPorukeTema">
                        Obriši poruke za temu
                    </a>
                </li>
            </ul>
        </section>

        <section class="card">
            <h2 class="card-title">Administracijski dio</h2>

            <ul class="nav-list">
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                        Administracijski status poslužitelja
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPloca">
                        Nadzorna ploča za rezervacije
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/admin/korisnici">
                        Nadzorna ploča za korisnike
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/admin/racuni">
                        Nadzorna ploča za račune iz JMS reda
                    </a>
                </li>
                <li>
                    <a href="${pageContext.servletContext.contextPath}/mvc/admin/zatvoreniRacuni">
                        Pregled zatvorenih računa
                    </a>
                </li>
            </ul>
        </section>

    </div>

</main>
</body>
</html>
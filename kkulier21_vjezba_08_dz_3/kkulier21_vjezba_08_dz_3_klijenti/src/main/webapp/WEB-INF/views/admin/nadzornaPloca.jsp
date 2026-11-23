<%@ page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>WS MVC - Nadzorna ploča za rezervacije</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Nadzorna ploča za rezervacije</h1>
        <p class="page-subtitle">
            Praćenje WebSocket poruka za rezervacije i avio tvrtke u stvarnom vremenu.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                Administracijski status
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
        </div>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">WebSocket rezervacije</div>
                <div class="info-value">
                    <span id="statusRezervacije" class="status-error">nije spojeno</span>
                </div>
            </div>

            <div class="info-item">
                <div class="info-label">WebSocket avio tvrtke</div>
                <div class="info-value">
                    <span id="statusAvioTvrtke" class="status-error">nije spojeno</span>
                </div>
            </div>
        </div>

    </section>

    <section class="card">
        <h2 class="card-title">WebSocket /ws/rezervacije</h2>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>R.br.</th>
                        <th>Vrijeme primitka</th>
                        <th>Dio 1</th>
                        <th>Dio 2</th>
                        <th>Dio 3</th>
                        <th>Dio 4</th>
                        <th>Dio 5</th>
                        <th>Cijela poruka</th>
                    </tr>
                </thead>
                <tbody id="tablicaRezervacije">
                    <tr>
                        <td colspan="8">Još nema poruka.</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>

    <section class="card">
        <h2 class="card-title">WebSocket /ws/aviotvrtke</h2>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>R.br.</th>
                        <th>Vrijeme primitka</th>
                        <th>Dio 1</th>
                        <th>Dio 2</th>
                        <th>Dio 3</th>
                        <th>Dio 4</th>
                        <th>Dio 5</th>
                        <th>Cijela poruka</th>
                    </tr>
                </thead>
                <tbody id="tablicaAvioTvrtke">
                    <tr>
                        <td colspan="8">Još nema poruka.</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </section>

</main>

<script type="text/javascript">
    var porukeRezervacije = [];
    var porukeAvioTvrtke = [];

    function napraviWsAdresu(putanja) {
        var protokol = window.location.protocol === "https:" ? "wss://" : "ws://";
        var adresa = window.location.pathname;
        var dijelovi = adresa.split("/");

        return protokol + window.location.hostname + ":"
            + window.location.port + "/" + dijelovi[1] + putanja;
    }

    function connect() {
        spojiRezervacije();
        spojiAvioTvrtke();
    }

    function spojiRezervacije() {
        var adresa = napraviWsAdresu("/ws/rezervacije");
        var ws = napraviWebSocket(adresa);

        if (ws == null) {
            return;
        }

        ws.onopen = function() {
            postaviStatus("statusRezervacije", true);
        };

        ws.onclose = function() {
            postaviStatus("statusRezervacije", false);
        };

        ws.onerror = function() {
            postaviStatus("statusRezervacije", false);
        };

        ws.onmessage = function(evt) {
            dodajPoruku(porukeRezervacije, "tablicaRezervacije", evt.data);
        };
    }

    function spojiAvioTvrtke() {
        var adresa = napraviWsAdresu("/ws/aviotvrtke");
        var ws = napraviWebSocket(adresa);

        if (ws == null) {
            return;
        }

        ws.onopen = function() {
            postaviStatus("statusAvioTvrtke", true);
        };

        ws.onclose = function() {
            postaviStatus("statusAvioTvrtke", false);
        };

        ws.onerror = function() {
            postaviStatus("statusAvioTvrtke", false);
        };

        ws.onmessage = function(evt) {
            dodajPoruku(porukeAvioTvrtke, "tablicaAvioTvrtke", evt.data);
        };
    }

    function napraviWebSocket(adresa) {
        if ("WebSocket" in window) {
            return new WebSocket(adresa);
        }

        if ("MozWebSocket" in window) {
            return new MozWebSocket(adresa);
        }

        alert("WebSocket nije podržan od web preglednika.");
        return null;
    }

    function postaviStatus(idElementa, radi) {
        var element = document.getElementById(idElementa);

        if (radi) {
            element.innerHTML = "spojeno";
            element.className = "status-ok";
        } else {
            element.innerHTML = "nije spojeno";
            element.className = "status-error";
        }
    }

    function dodajPoruku(lista, idTablice, poruka) {
        var zapis = {
            vrijeme: new Date().toLocaleString(),
            tekst: poruka
        };

        lista.unshift(zapis);

        if (lista.length > 10) {
            lista.pop();
        }

        osvjeziTablicu(lista, idTablice);
    }

    function osvjeziTablicu(lista, idTablice) {
        var tbody = document.getElementById(idTablice);
        tbody.innerHTML = "";

        if (lista.length === 0) {
            tbody.innerHTML = "<tr><td colspan='8'>Još nema poruka.</td></tr>";
            return;
        }

        for (var i = 0; i < lista.length; i++) {
            var zapis = lista[i];
            var dijelovi = zapis.tekst.split(",");

            var red = "<tr>";
            red += "<td>" + (i + 1) + "</td>";
            red += "<td>" + escapeHtml(zapis.vrijeme) + "</td>";

            for (var j = 0; j < 5; j++) {
                var vrijednost = "";

                if (j < dijelovi.length) {
                    vrijednost = dijelovi[j].trim();
                }

                red += "<td>" + escapeHtml(vrijednost) + "</td>";
            }

            red += "<td>" + escapeHtml(zapis.tekst) + "</td>";
            red += "</tr>";

            tbody.innerHTML += red;
        }
    }

    function escapeHtml(tekst) {
        if (tekst == null) {
            return "";
        }

        return tekst
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    window.addEventListener("load", connect, false);
</script>

</body>
</html>
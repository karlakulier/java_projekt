<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Pregled rezervacija putnika</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled mojih rezervacija</h1>
        <p class="page-subtitle">Pretraživanje i potvrđivanje rezervacija putnika.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/pocetak">
                Početna rezervacije
            </a>
            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/racuni">
                Pregled mojih računa
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

            Boolean odabranPosluzitelj = (Boolean) request.getAttribute("odabranPosluzitelj");

            if (odabranPosluzitelj == null || !odabranPosluzitelj) {
        %>

            <div class="alert alert-error">
                Prvo je potrebno odabrati poslužitelj rezervacija.
            </div>

        <%
            } else {
        %>

        <h2 class="card-title">Pretraživanje rezervacija</h2>

        <form method="post" action="${pageContext.servletContext.contextPath}/mvc/rezervacije/rezervacijePutnika">
            <div class="form-row">
                <div class="form-field">
                    <label for="idUlaznice">ID ulaznice</label>
                    <input id="idUlaznice" name="idUlaznice" type="text" value="${idUlaznice}">
                    <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}">
                </div>

                <div class="form-field">
                    <label for="odDatuma">Od datuma</label>
                    <input id="odDatuma" name="odDatuma" type="text" value="${odDatuma}" placeholder="dd.mm.gggg.">
                </div>

                <div class="form-field">
                    <label for="doDatuma">Do datuma</label>
                    <input id="doDatuma" name="doDatuma" type="text" value="${doDatuma}" placeholder="dd.mm.gggg.">
                </div>

                <div class="form-field">
                    <button class="btn" type="submit">Dohvati rezervacije</button>
                </div>
            </div>
        </form>

        <%
            Boolean imaOtvorenRacun = (Boolean) request.getAttribute("imaOtvorenRacun");

            if (imaOtvorenRacun == null || !imaOtvorenRacun) {
        %>
            <div class="alert alert-error">
                Nema otvorenog računa. Potvrđivanje rezervacija nije dostupno.
            </div>
        <%
            }

            List<Map<String, String>> rezervacije =
                (List<Map<String, String>>) request.getAttribute("rezervacije");

            String idUlaznice = (String) request.getAttribute("idUlaznice");

            if (rezervacije != null && !rezervacije.isEmpty()) {
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Broj rezervacije</th>
                    <th>Oznaka leta</th>
                    <th>ID leta</th>
                    <th>Vrijeme polijetanja</th>
                    <th>Polazni aerodrom</th>
                    <th>Odredišni aerodrom</th>
                    <th>Potvrđivanje</th>
                </tr>

                <%
                    int i = 0;

                    for (Map<String, String> r : rezervacije) {
                        i++;

                        String idRezervacije = r.get("idRezervacijeSjedala");
                        String status = r.get("status");
                        boolean potvrdena = status != null && status.toLowerCase().contains("potvr");

                        boolean mozePotvrditi = !potvrdena && imaOtvorenRacun != null && imaOtvorenRacun;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= idRezervacije %></td>
                    <td><%= r.get("oznakaLeta") %></td>
                    <td><%= r.get("idLeta") %></td>
                    <td><%= r.get("vrijemePolijetanja") %></td>
                    <td><%= r.get("polazniAerodrom") %></td>
                    <td><%= r.get("odredisniAerodrom") %></td>
                    <td>
                        <%
                            if (mozePotvrditi) {
                        %>
                            <a class="link" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/potvrdiRezervaciju/<%= idRezervacije %>?idUlaznice=<%= idUlaznice %>">
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
                %>
            </table>
        </div>

        <%
            } else {
        %>

            <div class="alert alert-error">
                Nema rezervacija za prikaz. Unesi podatke za pretraživanje i pokreni dohvat.
            </div>

        <%
            }
        %>

        <%
            }
        %>
    </section>

</main>
</body>
</html>
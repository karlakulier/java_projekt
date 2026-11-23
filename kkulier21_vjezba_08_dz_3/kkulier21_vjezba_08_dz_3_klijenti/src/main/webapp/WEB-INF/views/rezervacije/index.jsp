<%@ page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>MVC - Rezervacije</title>
   <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Rezervacije</h1>
        <p class="page-subtitle">Odabir poslužitelja rezervacija i pregled dostupnih aktivnosti.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">
                Pregled avio tvrtki
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");
            Object oPogreska = request.getAttribute("pogreska");

            if (poruka != null && poruka.length() > 0) {
                boolean pogreska = false;

                if (oPogreska != null) {
                    pogreska = (Boolean) oPogreska;
                }

                String klasa = pogreska ? "alert alert-error" : "alert alert-success";
        %>
            <div class="<%= klasa %>"><%= poruka %></div>
        <%
            }
        %>

        <h2 class="card-title">Odabir poslužitelja rezervacija</h2>

        <form method="post" action="${pageContext.servletContext.contextPath}/mvc/rezervacije/odaberiPosluzitelja">
            <div class="form-row">
                <div class="form-field">
                    <label for="posluzitelj">Poslužitelj</label>
                    <select id="posluzitelj" name="posluzitelj">
                        <option value="0">Poslužitelj 0</option>
                        <option value="1">Poslužitelj 1</option>
                    </select>
                    <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}">
                </div>

                <div class="form-field">
                    <button class="btn" type="submit">Odaberi poslužitelj</button>
                </div>
            </div>
        </form>

        <%
            Object odabranObjekt = request.getAttribute("odabranPosluzitelj");
            boolean odabranPosluzitelj = false;

            if (odabranObjekt != null) {
                odabranPosluzitelj = (Boolean) odabranObjekt;
            }

            if (odabranPosluzitelj) {
        %>

            <div class="alert alert-success">
                Trenutno odabrani poslužitelj: <strong>${posluzitelj}</strong>
            </div>

            <h2 class="card-title">Dostupne aktivnosti</h2>

            <div class="actions">
            

                <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/racuni">
                    Pregled mojih računa
                </a>

                <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/rezervacijePutnika">
                    Pregled mojih rezervacija
                </a>

                <a class="btn btn-danger" href="${pageContext.servletContext.contextPath}/mvc/rezervacije/ponistiOdabirPosluzitelja">
                    Poništi odabir poslužitelja
                </a>
            </div>

        <%
            } else {
        %>

            <div class="alert alert-error">
                Prvo odaberi poslužitelj rezervacija. Ostale aktivnosti nisu dostupne dok poslužitelj nije odabran.
            </div>

        <%
            }
        %>
    </section>

</main>
</body>
</html>
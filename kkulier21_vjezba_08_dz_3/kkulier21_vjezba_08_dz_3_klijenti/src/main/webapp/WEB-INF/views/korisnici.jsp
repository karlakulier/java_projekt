<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.Korisnik" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>REST MVC - Pregled korisnika</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled korisnika</h1>
        <p class="page-subtitle">Popis svih korisnika dohvaćenih iz sustava.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/korisnici/noviKorisnik">
                Dodaj novog korisnika
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");
            if (poruka != null && poruka.length() > 0) {
        %>
            <div class="alert alert-error"><%= poruka %></div>
        <%
            }

            List<Korisnik> korisnici = (List<Korisnik>) request.getAttribute("korisnici");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Korisnik</th>
                    <th>Ime</th>
                    <th>Prezime</th>
                    <th>Email</th>
                </tr>

                <%
                    if (korisnici != null && !korisnici.isEmpty()) {
                        int i = 0;

                        for (Korisnik k : korisnici) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= k.korisnik() %></td>
                    <td><%= k.ime() %></td>
                    <td><%= k.prezime() %></td>
                    <td><%= k.email() %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="5">Nema korisnika za prikaz.</td>
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
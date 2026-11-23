<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="edu.unizg.foi.nwtis.Korisnik" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JPA MVC - Pregled poruka za temu</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled poruka za temu</h1>
        <p class="page-subtitle">Pregled korisnika primljenih preko JMS teme.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
            <a class="btn btn-danger" href="${pageContext.servletContext.contextPath}/mvc/korisnici/obrisiPorukeTema">
                Obriši poruke za temu
            </a>
            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/admin/korisnici">
                Admin nadzorna ploča za korisnike
            </a>
        </div>

        <%
            List<Korisnik> poruke = (List<Korisnik>) request.getAttribute("poruke");
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
                    if (poruke != null && !poruke.isEmpty()) {
                        int i = 0;

                        for (Korisnik k : poruke) {
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
                    <td colspan="5">Nema poruka za temu.</td>
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
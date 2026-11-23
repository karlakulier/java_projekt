<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.AvioTvrtka" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>REST MVC - Pregled avio tvrtki</title>
   <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled avio tvrtki</h1>
        <p class="page-subtitle">Popis avio tvrtki i pregled njihovih zastupnika.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">
                Pregled letova i sjedala
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");
            if (poruka != null && poruka.length() > 0) {
        %>
            <div class="alert alert-error"><%= poruka %></div>
        <%
            }

            List<AvioTvrtka> avioTvrtke =
                (List<AvioTvrtka>) request.getAttribute("avioTvrtke");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Oznaka / ID</th>
                    <th>Podaci avio tvrtke</th>
                    <th>Zastupnici</th>
                </tr>

                <%
                    if (avioTvrtke != null && !avioTvrtke.isEmpty()) {
                        int i = 0;

                        for (AvioTvrtka a : avioTvrtke) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><span class="badge"><%= a.id() %></span></td>
                    <td><%= a.toString() %></td>
                    <td>
                        <a class="link" href="${pageContext.servletContext.contextPath}/mvc/korisnici/ispisZastupnika/<%= a.id() %>">
                            Pregled zastupnika
                        </a>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="4">Nema avio tvrtki za prikaz.</td>
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
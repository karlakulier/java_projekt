<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>JPA MVC - Pregled reda poruka</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled reda poruka</h1>
        <p class="page-subtitle">Pregled računa primljenih preko JMS reda.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
            <a class="btn btn-danger" href="${pageContext.servletContext.contextPath}/mvc/korisnici/obrisiRedPoruka">
                Obriši red poruka
            </a>
            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/admin/racuni">
                Admin nadzorna ploča za račune
            </a>
        </div>

        <%
            List<String> poruke = (List<String>) request.getAttribute("poruke");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>Poruka</th>
                </tr>

                <%
                    if (poruke != null && !poruke.isEmpty()) {
                        int i = 0;

                        for (String p : poruke) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= p %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="2">Nema poruka u redu.</td>
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
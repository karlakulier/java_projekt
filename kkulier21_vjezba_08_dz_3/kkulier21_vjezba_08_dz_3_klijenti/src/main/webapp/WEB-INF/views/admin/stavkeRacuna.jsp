<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Stavke računa</title>
   <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Stavke računa</h1>
        <p class="page-subtitle">
            Pregled podataka računa i pripadajućih stavki zaprimljenih putem JMS reda.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/racuni">
                Povratak na nadzornu ploču za račune
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                Administracijski status
            </a>
        </div>

        <%
            String poruka = (String) request.getAttribute("poruka");

            if (poruka != null && poruka.length() > 0) {
        %>
            <div class="alert alert-error"><%= poruka %></div>
        <%
            }

            Map<String, String> racun =
                (Map<String, String>) request.getAttribute("racun");

            List<Map<String, String>> stavke =
                (List<Map<String, String>>) request.getAttribute("stavke");
        %>

        <h2 class="card-title">Račun</h2>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">ID računa</div>
                <div class="info-value"><%= racun != null && racun.get("id") != null ? racun.get("id") : "-" %></div>
            </div>

            <div class="info-item">
                <div class="info-label">Korisnik</div>
                <div class="info-value"><%= racun != null && racun.get("korisnik") != null ? racun.get("korisnik") : "-" %></div>
            </div>

            <div class="info-item">
                <div class="info-label">Broj stavki</div>
                <div class="info-value"><%= racun != null && racun.get("brojstavki") != null ? racun.get("brojstavki") : "-" %></div>
            </div>

            <div class="info-item">
                <div class="info-label">Iznos</div>
                <div class="info-value"><%= racun != null && racun.get("iznos") != null ? racun.get("iznos") : "-" %></div>
            </div>
        </div>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Vrijeme</div>
                <div class="info-value"><%= racun != null && racun.get("vrijeme") != null ? racun.get("vrijeme") : "-" %></div>
            </div>
        </div>

        <h2 class="card-title">Stavke računa</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID stavke</th>
                    <th>Iznos</th>
                    <th>Ime putnika</th>
                    <th>Prezime putnika</th>
                    <th>Rezervacija</th>
                </tr>

                <%
                    if (stavke != null && !stavke.isEmpty()) {
                        int i = 0;

                        for (Map<String, String> s : stavke) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= s.get("id") == null ? "" : s.get("id") %></td>
                    <td><%= s.get("iznos") == null ? "" : s.get("iznos") %></td>
                    <td><%= s.get("putnikime") == null ? "" : s.get("putnikime") %></td>
                    <td><%= s.get("putnikiprezime") == null ? "" : s.get("putnikiprezime") %></td>
                    <td><%= s.get("rezervacija") == null ? "" : s.get("rezervacija") %></td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="6">Nema stavki za prikaz.</td>
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
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.Map" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Administracija poslužitelja</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Administracijski dio</h1>
        <p class="page-subtitle">
            Pregled statusa poslužitelja Kontrola i Rezervacije te pristup administracijskim nadzornim pločama.
        </p>
    </div>

    <section class="card">

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
        %>

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                Osvježi status
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/aviotvrtke/pocetak">
                Početna avio tvrtke
            </a>

            <a class="btn"
               href="${pageContext.servletContext.contextPath}/mvc/admin/nadzornaPloca">
                Nadzorna ploča za rezervacije
            </a>

            <a class="btn"
               href="${pageContext.servletContext.contextPath}/mvc/admin/korisnici">
                Nadzorna ploča za korisnike
            </a>

            <a class="btn"
               href="${pageContext.servletContext.contextPath}/mvc/admin/racuni">
                Nadzorna ploča za račune
            </a>

            <a class="btn"
               href="${pageContext.servletContext.contextPath}/mvc/admin/zatvoreniRacuni">
                Pregled zatvorenih računa
            </a>
        </div>

    </section>

    <section class="card">
        <h2 class="card-title">Poslužitelj Kontrola</h2>

        <%
            Map<String, String> kontrolaKorisnicki =
                (Map<String, String>) request.getAttribute("kontrolaKorisnicki");

            Map<String, String> kontrolaAdmin =
                (Map<String, String>) request.getAttribute("kontrolaAdmin");

            if (kontrolaKorisnicki == null) {
                kontrolaKorisnicki = new java.util.HashMap<String, String>();
            }

            if (kontrolaAdmin == null) {
                kontrolaAdmin = new java.util.HashMap<String, String>();
            }
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>Dio poslužitelja</th>
                    <th>HTTP status</th>
                    <th>Status</th>
                    <th>Akcija</th>
                </tr>

                <tr>
                    <td><%= kontrolaKorisnicki.get("naziv") == null ? "" : kontrolaKorisnicki.get("naziv") %></td>
                    <td><%= kontrolaKorisnicki.get("httpStatus") == null ? "" : kontrolaKorisnicki.get("httpStatus") %></td>
                    <td>
                        <span class="<%= "RADI".equals(kontrolaKorisnicki.get("status")) ? "status-ok" : "status-error" %>">
                            <%= kontrolaKorisnicki.get("status") == null ? "" : kontrolaKorisnicki.get("status") %>
                        </span>
                    </td>
                    <td>-</td>
                </tr>

                <tr>
                    <td><%= kontrolaAdmin.get("naziv") == null ? "" : kontrolaAdmin.get("naziv") %></td>
                    <td><%= kontrolaAdmin.get("httpStatus") == null ? "" : kontrolaAdmin.get("httpStatus") %></td>
                    <td>
                        <span class="<%= "RADI".equals(kontrolaAdmin.get("status")) ? "status-ok" : "status-error" %>">
                            <%= kontrolaAdmin.get("status") == null ? "" : kontrolaAdmin.get("status") %>
                        </span>
                    </td>
                    <td>
                        <a class="link"
                           href="${pageContext.servletContext.contextPath}/mvc/admin/kontrola/kraj">
                            Kraj rada admin dijela
                        </a>
                    </td>
                </tr>
            </table>
        </div>
    </section>

    <section class="card">
        <h2 class="card-title">Poslužitelji Rezervacije</h2>

        <%
            List<Map<String, String>> rezervacije =
                (List<Map<String, String>>) request.getAttribute("rezervacije");
        %>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>ID poslužitelja</th>
                    <th>Korisnički dio</th>
                    <th>Dio za avio tvrtke</th>
                    <th>Administracijski dio</th>
                    <th>Akcija</th>
                </tr>

                <%
                    if (rezervacije != null && !rezervacije.isEmpty()) {
                        for (Map<String, String> r : rezervacije) {
                            String id = r.get("id");
                            String korisnickiStatus = r.get("korisnickiStatus");
                            String avioTvrtkeStatus = r.get("avioTvrtkeStatus");
                            String adminStatus = r.get("adminStatus");

                            if (id == null) {
                                id = "";
                            }

                            if (korisnickiStatus == null) {
                                korisnickiStatus = "";
                            }

                            if (avioTvrtkeStatus == null) {
                                avioTvrtkeStatus = "";
                            }

                            if (adminStatus == null) {
                                adminStatus = "";
                            }
                %>
                <tr>
                    <td><%= id %></td>

                    <td>
                        <span class="<%= "RADI".equals(korisnickiStatus) ? "status-ok" : "status-error" %>">
                            <%= korisnickiStatus %>
                        </span>
                    </td>

                    <td>
                        <span class="<%= "RADI".equals(avioTvrtkeStatus) ? "status-ok" : "status-error" %>">
                            <%= avioTvrtkeStatus %>
                        </span>
                    </td>

                    <td>
                        <span class="<%= "RADI".equals(adminStatus) ? "status-ok" : "status-error" %>">
                            <%= adminStatus %>
                        </span>
                    </td>

                    <td>
                        <a class="link"
                           href="${pageContext.servletContext.contextPath}/mvc/admin/rezervacije/<%= id %>/kraj">
                            Kraj rada admin dijela
                        </a>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="5">Nema poslužitelja Rezervacije za prikaz.</td>
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
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti.Racun" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Zatvoreni računi</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Pregled zatvorenih računa</h1>
        <p class="page-subtitle">
            Pregled zatvorenih računa iz baze podataka uz filtriranje po imenu i prezimenu putnika.
        </p>
    </div>

    <section class="card">

        <div class="actions">
            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/status">
                Administracijski status
            </a>

            <a class="btn btn-secondary"
               href="${pageContext.servletContext.contextPath}/mvc/admin/racuni">
                Nadzorna ploča za račune iz JMS reda
            </a>
        </div>

        <h2 class="card-title">Filter zatvorenih računa</h2>

        <form method="get"
              action="${pageContext.servletContext.contextPath}/mvc/admin/zatvoreniRacuni">

            <div class="form-row">
                <div class="form-field">
                    <label for="ime">Ime</label>
                    <input type="text"
                           id="ime"
                           name="ime"
                           value="${ime}">
                </div>

                <div class="form-field">
                    <label for="prezime">Prezime</label>
                    <input type="text"
                           id="prezime"
                           name="prezime"
                           value="${prezime}">
                </div>

                <div class="form-field">
                    <button class="btn" type="submit">
                        Pretraži
                    </button>
                </div>

                <div class="form-field">
                    <a class="btn btn-secondary"
                       href="${pageContext.servletContext.contextPath}/mvc/admin/zatvoreniRacuni">
                        Očisti filter
                    </a>
                </div>
            </div>
        </form>

        <%
            List<Racun> racuni = (List<Racun>) request.getAttribute("racuni");
            int ukupnoRacuna = racuni == null ? 0 : racuni.size();
        %>

        <div class="info-grid">
            <div class="info-item">
                <div class="info-label">Ukupno zatvorenih računa</div>
                <div class="info-value"><%= ukupnoRacuna %></div>
            </div>
        </div>

        <h2 class="card-title">Popis zatvorenih računa</h2>

        <div class="table-wrap">
            <table>
                <tr>
                    <th>R.br.</th>
                    <th>ID računa</th>
                    <th>Korisnik</th>
                    <th>Broj stavki</th>
                    <th>Iznos</th>
                    <th>Vrijeme</th>
                    <th>Stavke</th>
                </tr>

                <%
                    if (racuni != null && !racuni.isEmpty()) {
                        int i = 0;

                        for (Racun r : racuni) {
                            i++;
                %>
                <tr>
                    <td><%= i %></td>
                    <td><%= r.getId() %></td>
                    <td>
                        <%= r.getKorisnici() != null ? r.getKorisnici().getKorisnik() : "" %>
                    </td>
                    <td><%= r.getBrojstavki() %></td>
                    <td><%= r.getIznos() %></td>
                    <td><%= r.getVrijeme() %></td>
                    <td>
                        <a class="link"
                           href="${pageContext.servletContext.contextPath}/mvc/admin/zatvoreniRacuni/stavke/<%= r.getId() %>">
                            Pregled stavki
                        </a>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="7">Nema zatvorenih računa za prikaz.</td>
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
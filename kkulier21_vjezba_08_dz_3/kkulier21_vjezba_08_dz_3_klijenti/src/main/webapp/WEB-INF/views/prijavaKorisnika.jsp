<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>REST MVC - Prijava korisnika</title>
    <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Prijava korisnika</h1>
        <p class="page-subtitle">Unesite korisničko ime i lozinku za prijavu u aplikaciju.</p>
    </div>

    <section class="card">
        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>
        </div>

      <form method="post" action="/kkulier21_vjezba_08_dz_3_klijenti/j_security_check">
            <div class="form-row">
                <div class="form-field">
                    <label for="j_username">Korisnik</label>
                    <input id="j_username" type="text" name="j_username">
                </div>

                <div class="form-field">
                    <label for="j_password">Lozinka</label>
                    <input id="j_password" type="password" name="j_password">
                </div>
            </div>

            <button class="btn" type="submit">Prijavi korisnika</button>
        </form>
    </section>

</main>
</body>
</html>
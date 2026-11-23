<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>REST MVC - Prijava korisnika greška</title>
  <link rel="stylesheet" href="${pageContext.servletContext.contextPath}/resources/css/app.css">
</head>

<body>
<main class="page">

    <div class="page-header">
        <h1 class="page-title">Prijava korisnika</h1>
        <p class="page-subtitle">Prijava nije bila uspješna.</p>
    </div>

    <section class="card">
        <div class="alert alert-error">
            Prijava korisnika nije bila uspješna. Provjerite korisničko ime i lozinku.
        </div>

        <div class="actions">
            <a class="btn btn-secondary" href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">
                Početna stranica
            </a>

            <a class="btn" href="${pageContext.servletContext.contextPath}/mvc/prijava/prijavaKorisnika">
                Pokušaj ponovno
            </a>
        </div>
    </section>

</main>
</body>
</html>
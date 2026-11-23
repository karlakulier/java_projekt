module kkulier21_vjezba_08_dz_3_servisi {
  requires transitive jakarta.ws.rs;

  requires transitive org.glassfish.grizzly.http.server;

  requires org.glassfish.jersey.core.server;
  requires org.glassfish.jersey.container.grizzly2.http;

  requires transitive kkulier21_vjezba_08_dz_3_konfiguracije;
  requires transitive kkulier21_vjezba_08_dz_3_podaci;
  requires transitive kkulier21_vjezba_08_dz_3_dao;
  requires java.sql;
  requires org.glassfish.jersey.media.json.jackson;

  exports edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;
}

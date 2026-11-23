FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8010 8011 8012

CMD ["../bin/java", "-m", "kkulier21_vjezba_08_dz_3_rezervacije/edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.PosluziteljRezervacije", "NWTiS_04_rezervacije.txt"]

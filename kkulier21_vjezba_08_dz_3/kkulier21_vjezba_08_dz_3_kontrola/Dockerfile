FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8000 8001

CMD ["../bin/java", "-m", "kkulier21_vjezba_08_dz_3_kontrola/edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.PosluziteljKontrola", "NWTiS_04_kontrola.txt"]

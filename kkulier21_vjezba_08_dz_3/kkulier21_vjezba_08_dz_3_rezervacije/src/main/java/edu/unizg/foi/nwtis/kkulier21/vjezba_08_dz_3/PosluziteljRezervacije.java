package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.Avion;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.LetPodaci;
import edu.unizg.foi.nwtis.LetSjedalo;
import edu.unizg.foi.nwtis.OdredivanjeSjedala;
import edu.unizg.foi.nwtis.ProblemKodRezervacije;
import edu.unizg.foi.nwtis.RazredSjedala;
import edu.unizg.foi.nwtis.RezervacijaSjedala;
import edu.unizg.foi.nwtis.StatusRezervacijeSjedala;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

public class PosluziteljRezervacije {

  public static final String OK = "OK";
  public static final String ERROR_20 = "ERROR 20";
  public static final String ERROR_21 = "ERROR 21";
  public static final String ERROR_22 = "ERROR 22";
  public static final String ERROR_23 = "ERROR 23";
  public static final String ERROR_24 = "ERROR 24";
  public static final String ERROR_25 = "ERROR 25";
  public static final String ERROR_26 = "ERROR 26";
  public static final String ERROR_27 = "ERROR 27";
  public static final String ERROR_28 = "ERROR 28";
  public static final String ERROR_29 = "ERROR 29";
  public static final String ERROR_39 = "ERROR 39";

  public static final Pattern UZORAK_REZERVIRAJ =
      Pattern.compile("^REZERVIRAJ (\\S+) (\\S+) (\\S+) ([PE])$");

  public static final Pattern UZORAK_POTVRDI = Pattern.compile("^POTVRDI (\\S+) (\\S+) (\\S+)$");

  public static final Pattern UZORAK_REZERVIRAJ_GRUPA =
      Pattern.compile("^REZERVIRAJ GRUPA (\\S+) (\\S+) \\[(.+)] ([PE])$");

  public static final Pattern UZORAK_PAUZA = Pattern.compile("^PAUZA (\\d+)$");

  public static final Pattern UZORAK_PING = Pattern.compile("^PING$");

  public static final Pattern UZORAK_ADMIN_PONISTI = Pattern.compile("^REZERVACIJE PONIŠTI$");

  public static final Pattern UZORAK_ADMIN_KRAJ = Pattern.compile("^KRAJ (\\S+)$");

  public static final Pattern UZORAK_AVIO_ZATVORI_REDOVI =
      Pattern.compile("^ZATVORI (\\S+) (\\S+) (\\S+) (\\d+) (\\d+)$");

  public static final Pattern UZORAK_AVIO_ZATVORI_DATUMI =
      Pattern.compile("^ZATVORI (\\S+) (\\S+) (\\d{4}-\\d{2}-\\d{2}) " + "(\\d{4}-\\d{2}-\\d{2})$");

  public static final Pattern UZORAK_AVIO_INICIJALIZIRAJ_LET =
      Pattern.compile("^INICIJALIZIRAJ (\\S+) (\\S+) \"([^\"]+)\" "
          + "(\\d{4}-\\d{2}-\\d{2}) (\\d{4}-\\d{2}-\\d{2})$");

  public static final Pattern UZORAK_AVIO_INICIJALIZIRAJ_TVRTKA = Pattern
      .compile("^INICIJALIZIRAJ (\\S+) (\\S+) (\\d{4}-\\d{2}-\\d{2}) " + "(\\d{4}-\\d{2}-\\d{2})$");
  public static final Pattern UZORAK_AVIO_ISPIS_LETOVI =
      Pattern.compile("^ISPIS LETOVI (\\S+) (\\S+)$");


  public static final Pattern UZORAK_KORISNIK_ISPIS =
      Pattern.compile("^ISPIS (\\S+) \"([^\"]+)\" (\\d{4}-\\d{2}-\\d{2})$");

  public static final Pattern UZORAK_ISPIS_REZERVACIJA = Pattern.compile("^ISPIS (\\S+) (\\S+)$");

  public static final Pattern UZORAK_ISPIS_PUTNIK = Pattern
      .compile("^ISPIS (\\S+) (\\S+) " + "(\\d{4}-\\d{2}-\\d{2}) " + "(\\d{4}-\\d{2}-\\d{2})$");

  public static final Pattern UZORAK_AVIO_ISPIS_PUTNIK = Pattern
      .compile("^ISPIS (\\S+) (\\S+) (\\S+) " + "(\\d{4}-\\d{2}-\\d{2}) (\\d{4}-\\d{2}-\\d{2})$");

  public static final Pattern UZORAK_AVIO_ISPIS = Pattern.compile("^ISPIS (\\S+) (\\S+)$");



  public static final Pattern UZORAK_AVIO_STANJE = Pattern.compile("^STANJE (\\S+) (\\S+)$");

  private static PosluziteljRezervacije instanca;

  private Konfiguracija konfig;

  private ExecutorService executor = null;

  private int maksBrojDretvi = 5;

  private int maksBrojCekaca = 5;

  private int pauzaDretve = 1000;

  private String kodZaKraj = "";

  private AtomicBoolean kraj = new AtomicBoolean(false);

  private AtomicBoolean primaNoveKorisnickeKomande = new AtomicBoolean(true);

  private AtomicBoolean primaNoveAvioKomande = new AtomicBoolean(true);

  private AtomicInteger aktivneKorisnickeObrade = new AtomicInteger(0);

  private AtomicInteger aktivneAvioObrade = new AtomicInteger(0);

  private ServerSocket korisnickaUticnica;

  private ServerSocket avioUticnica;

  private ServerSocket administracijskaUticnica;

  private int mreznaVrata = 0;

  private int mreznaVrataAvioKompanije = 0;

  private int mreznaVrataAdministracija = 0;

  private String adresaKontrola = "localhost";

  private int mreznaVrataKontrola = 8000;

  private String datotekaAvioTvrtke = "";

  private String datotekaAvioni = "";

  private String datotekaLetovi = "";

  private String odredivanjeSjedalaKlasa = "";

  private LocalDate datumOd;

  private LocalDate datumDo;

  private OdredivanjeSjedala odredivanjeSjedala;

  private final Map<String, AvioTvrtka> avioTvrtke = new ConcurrentHashMap<>();

  private final Map<String, Avion> avioni = new ConcurrentHashMap<>();

  private final Map<String, LetPodaci> letoviPodaci = new ConcurrentHashMap<>();

  private final Map<String, Let> letovi = new ConcurrentHashMap<>();

  private final Map<String, List<RezervacijaSjedala>> rezervacijeSjedala =
      new ConcurrentHashMap<>();

  private final Map<String, Map<Integer, String>> rasporediRedovaPoLetu = new ConcurrentHashMap<>();

  public PosluziteljRezervacije() {
    instanca = this;
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      return;
    }

    var program = new PosluziteljRezervacije();
    program.pripremiKreni(args[0]);
  }

  public static PosluziteljRezervacije dohvatiInstancu() {
    return instanca;
  }

  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }

    this.ucitajPodatke();
    this.ucitajOdredivanjeSjedala();

    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newFixedThreadPool(this.maksBrojDretvi, factory);

    var dretvaAdministracija = this.executor.submit(this::pokreniPosluziteljAdministracija);
    var dretvaKorisnici = this.executor.submit(this::pokreniPosluziteljKorisnici);
    var dretvaAvioTvrtke = this.executor.submit(this::pokreniPosluziteljAvioTvrtke);

    while (!dretvaAdministracija.isDone() || !dretvaKorisnici.isDone()
        || !dretvaAvioTvrtke.isDone()) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  public Konfiguracija getKonfig() {
    return this.konfig;
  }

  public void pokreniPosluziteljKorisnici() {
    try (ServerSocket ss = new ServerSocket(this.mreznaVrata, this.maksBrojCekaca)) {
      this.korisnickaUticnica = ss;
      ss.setSoTimeout(this.pauzaDretve);

      while (!this.kraj.get()) {
        if (!this.primaNoveKorisnickeKomande.get()) {
          Thread.sleep(this.pauzaDretve);
          continue;
        }

        try {
          Socket mreznaUticnica = ss.accept();
          this.executor.submit(() -> this.obradiKorisnici(mreznaUticnica));
        } catch (SocketTimeoutException e) {
          continue;
        }
      }
    } catch (IOException e) {
      // kontrolirano
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void pokreniPosluziteljAvioTvrtke() {
    try (ServerSocket ss = new ServerSocket(this.mreznaVrataAvioKompanije, this.maksBrojCekaca)) {
      this.avioUticnica = ss;
      ss.setSoTimeout(this.pauzaDretve);

      while (!this.kraj.get()) {
        if (!this.primaNoveAvioKomande.get()) {
          Thread.sleep(this.pauzaDretve);
          continue;
        }

        try {
          Socket mreznaUticnica = ss.accept();
          this.executor.submit(() -> this.obradiAvioTvrtke(mreznaUticnica));
        } catch (SocketTimeoutException e) {
          continue;
        }
      }
    } catch (IOException e) {
      // kontrolirano
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void pokreniPosluziteljAdministracija() {
    try (ServerSocket ss = new ServerSocket(this.mreznaVrataAdministracija, 1)) {
      this.administracijskaUticnica = ss;

      while (!this.kraj.get()) {
        Socket mreznaUticnica = ss.accept();
        Future<Boolean> future =
            this.executor.submit(() -> this.obradiAdministracija(mreznaUticnica));
        future.get();
      }
    } catch (IOException e) {
    } catch (Exception e) {
    }
  }

  public Boolean obradiKorisnici(Socket mreznaUticnica) {
    this.aktivneKorisnickeObrade.incrementAndGet();

    try (mreznaUticnica;
        BufferedReader in = new BufferedReader(
            new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = mreznaUticnica.getOutputStream()) {

      String linija = in.readLine();
      String odgovor = this.obradiKorisnickuKomandu(linija);
      this.posaljiOdgovor(out, odgovor);
    } catch (Exception e) {
      // kontrolirano
    } finally {
      this.aktivneKorisnickeObrade.decrementAndGet();
    }

    return Boolean.TRUE;
  }

  public Boolean obradiAvioTvrtke(Socket mreznaUticnica) {
    this.aktivneAvioObrade.incrementAndGet();

    try (mreznaUticnica;
        BufferedReader in = new BufferedReader(
            new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = mreznaUticnica.getOutputStream()) {

      String linija = in.readLine();
      String odgovor = this.obradiAvioKomandu(linija);
      this.posaljiOdgovor(out, odgovor);
    } catch (Exception e) {
      // kontrolirano
    } finally {
      this.aktivneAvioObrade.decrementAndGet();
    }

    return Boolean.TRUE;
  }

  public Boolean obradiAdministracija(Socket mreznaUticnica) {
    try (mreznaUticnica;
        BufferedReader in = new BufferedReader(
            new InputStreamReader(mreznaUticnica.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = mreznaUticnica.getOutputStream()) {

      this.zaustaviPrijemNovihKomandi();
      this.pricekajAktivneObrade();

      String linija = in.readLine();
      String odgovor = this.obradiAdministracijskuKomandu(linija);
      this.posaljiOdgovor(out, odgovor);

      boolean komandaKraj = linija != null && linija.startsWith("KRAJ ");

      if (!komandaKraj || !OK.equals(odgovor)) {
        this.nastaviPrijemNovihKomandi();
      }
    } catch (Exception e) {
      this.nastaviPrijemNovihKomandi();
    }

    return Boolean.TRUE;
  }

  public String obradiKorisnickuKomandu(String komanda) {
    if (komanda == null || komanda.isBlank()) {
      return ERROR_20;
    }

    if (UZORAK_KORISNIK_ISPIS.matcher(komanda).matches()) {
      return this.obradiKomanduAvioIspisOznakaDatum(komanda);
    }
    if (UZORAK_ISPIS_PUTNIK.matcher(komanda).matches()) {
      return this.obradiKomanduIspisPutnik(komanda);
    }

    if (UZORAK_ISPIS_REZERVACIJA.matcher(komanda).matches()) {
      return this.obradiKomanduIspisRezervacija(komanda);
    }

    if (komanda.startsWith("REZERVIRAJ GRUPA ")) {
      return this.obradiKomanduRezervirajGrupa(komanda);
    }

    if (komanda.startsWith("REZERVIRAJ ")) {
      return this.obradiKomanduRezerviraj(komanda);
    }

    if (komanda.startsWith("POTVRDI ")) {
      return this.obradiKomanduPotvrdi(komanda);
    }

    if (komanda.startsWith("PAUZA ")) {
      return this.obradiKomanduPauza(komanda);
    }

    if (komanda.equals("PING")) {
      return this.obradiKomanduPing(komanda);
    }

    return ERROR_20;
  }

  public String obradiAvioKomandu(String komanda) {
    if (komanda == null || komanda.isBlank()) {
      return ERROR_20;
    }

    if (UZORAK_PING.matcher(komanda).matches()) {
      return OK;
    }

    if (UZORAK_AVIO_ZATVORI_REDOVI.matcher(komanda).matches()) {
      return this.obradiKomanduAvioZatvoriRedove(komanda);
    }

    if (UZORAK_AVIO_ZATVORI_DATUMI.matcher(komanda).matches()) {
      return this.obradiKomanduAvioZatvoriDatume(komanda);
    }

    if (UZORAK_AVIO_INICIJALIZIRAJ_LET.matcher(komanda).matches()) {
      return this.obradiKomanduAvioInicijalizirajLet(komanda);
    }

    if (UZORAK_AVIO_INICIJALIZIRAJ_TVRTKA.matcher(komanda).matches()) {
      return this.obradiKomanduAvioInicijalizirajTvrtku(komanda);
    }

    if (UZORAK_KORISNIK_ISPIS.matcher(komanda).matches()) {
      return this.obradiKomanduAvioIspisOznakaDatum(komanda);
    }

    if (UZORAK_AVIO_ISPIS.matcher(komanda).matches()) {
      return this.obradiKomanduAvioIspis(komanda);
    }
    if (UZORAK_AVIO_ISPIS_LETOVI.matcher(komanda).matches()) {
      return this.obradiKomanduAvioIspisLetovi(komanda);
    }

    if (UZORAK_AVIO_ISPIS_PUTNIK.matcher(komanda).matches()) {
      return this.obradiKomanduAvioIspisPutnik(komanda);
    }

    if (UZORAK_AVIO_STANJE.matcher(komanda).matches()) {
      return this.obradiKomanduAvioStanje(komanda);
    }

    return ERROR_20;
  }

  public String obradiKomanduAvioIspisPutnik(String komanda) {
    var podudaranje = UZORAK_AVIO_ISPIS_PUTNIK.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String avioTvrtka = podudaranje.group(2);
    String putnik = podudaranje.group(3);
    LocalDate odDatuma = LocalDate.parse(podudaranje.group(4));
    LocalDate doDatuma = LocalDate.parse(podudaranje.group(5));

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    List<RezervacijaSjedala> rezultat = new ArrayList<>();

    for (List<RezervacijaSjedala> lista : this.rezervacijeSjedala.values()) {
      for (RezervacijaSjedala rezervacija : lista) {
        if (rezervacija.putnik() == null) {
          continue;
        }

        Let let = rezervacija.sjedalo().let();
        LocalDate datum = let.datumPolijetanja();

        boolean istiPutnik = rezervacija.putnik().equals(putnik);
        boolean istaTvrtka = this.odgovaraAvioTvrtka(let, avioTvrtka);
        boolean uRasponu = !datum.isBefore(odDatuma) && !datum.isAfter(doDatuma);
        boolean rezervirano = rezervacija.statusRezervacije() != StatusRezervacijeSjedala.SLOBODNA;

        if (istiPutnik && istaTvrtka && uRasponu && rezervirano) {
          rezultat.add(rezervacija);
        }
      }
    }

    if (rezultat.isEmpty()) {
      return ERROR_24;
    }

    return OK + " " + this.pretvoriRezervacijeUJson(rezultat);
  }

  public String obradiKomanduAvioIspisLetovi(String komanda) {
    var podudaranje = UZORAK_AVIO_ISPIS_LETOVI.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String avioTvrtka = podudaranje.group(2);

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    List<LetPodaci> rezultat = new ArrayList<>();

    for (LetPodaci letPodaci : this.letoviPodaci.values()) {
      if (letPodaci.avioTvrtka().id().equals(avioTvrtka)) {
        rezultat.add(letPodaci);
      }
    }

    if (rezultat.isEmpty()) {
      return ERROR_24;
    }

    return OK + " " + this.pretvoriLetovePodatkeUJson(rezultat);
  }

  public String obradiKomanduAvioStanje(String komanda) {
    var podudaranje = UZORAK_AVIO_STANJE.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idLeta = podudaranje.group(2);

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    if (!this.letovi.containsKey(idLeta)) {
      return ERROR_22;
    }

    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(idLeta);

    if (lista == null) {
      return ERROR_22;
    }

    int slobodna = 0;
    int kreirana = 0;
    int potvrdena = 0;
    int zatvorena = 0;
    int nevazeca = 0;

    for (RezervacijaSjedala rezervacija : lista) {
      switch (rezervacija.statusRezervacije()) {
        case SLOBODNA -> slobodna++;
        case KREIRANA -> kreirana++;
        case POTVRDENA -> potvrdena++;
        case ZATVORENA -> zatvorena++;
        case NEVAZECA -> nevazeca++;
        default -> {
        }
      }
    }

    return OK + " " + slobodna + " " + kreirana + " " + potvrdena + " " + zatvorena + " "
        + nevazeca;
  }

  public String pretvoriLetovePodatkeUJson(List<LetPodaci> lista) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");

    for (int i = 0; i < lista.size(); i++) {
      LetPodaci letPodaci = lista.get(i);

      if (i > 0) {
        sb.append(",");
      }

      sb.append("{");
      sb.append("\"oznakaLeta\":\"").append(this.escapeJson(letPodaci.oznakaLeta())).append("\",");
      sb.append("\"avioTvrtka\":{");
      sb.append("\"id\":\"").append(this.escapeJson(letPodaci.avioTvrtka().id())).append("\",");
      sb.append("\"naziv\":\"").append(this.escapeJson(letPodaci.avioTvrtka().naziv()))
          .append("\"},");
      sb.append("\"vrijemePolijetanja\":\"").append(letPodaci.vrijemePolijetanja()).append("\",");
      sb.append("\"polazniAerodrom\":\"").append(this.escapeJson(letPodaci.polazniAerodrom()))
          .append("\",");
      sb.append("\"odredisniAerodrom\":\"").append(this.escapeJson(letPodaci.odredisniAerodrom()))
          .append("\",");
      sb.append("\"avion\":{");
      sb.append("\"id\":\"").append(this.escapeJson(letPodaci.avion().id())).append("\",");
      sb.append("\"model\":\"").append(this.escapeJson(letPodaci.avion().naziv())).append("\"}");
      sb.append("}");
    }

    sb.append("]");
    return sb.toString();
  }

  public String obradiKomanduIspisRezervacija(String komanda) {
    var podudaranje = UZORAK_ISPIS_REZERVACIJA.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idRezervacije = podudaranje.group(2);

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    RezervacijaSjedala rezervacija = this.pronadiRezervacijuPoId(idRezervacije);

    if (rezervacija == null) {
      return ERROR_24;
    }

    try {
      List<RezervacijaSjedala> lista = new ArrayList<>();
      lista.add(rezervacija);

      String json = this.pretvoriRezervacijeUJson(lista);

      if (json.startsWith("[") && json.endsWith("]")) {
        json = json.substring(1, json.length() - 1);
      }

      return OK + " " + json;
    } catch (Exception e) {
      return ERROR_39;
    }
  }

  public String obradiKomanduIspisPutnik(String komanda) {
    var podudaranje = UZORAK_ISPIS_PUTNIK.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String putnik = podudaranje.group(2);

    LocalDate odDatuma;
    LocalDate doDatuma;

    try {
      odDatuma = LocalDate.parse(podudaranje.group(3));
      doDatuma = LocalDate.parse(podudaranje.group(4));
    } catch (Exception e) {
      return ERROR_20;
    }

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    List<RezervacijaSjedala> rezervacije =
        this.pronadiRezervacijePutnika(putnik, odDatuma, doDatuma);

    if (rezervacije.isEmpty()) {
      return ERROR_24;
    }

    try {
      return OK + " " + this.pretvoriRezervacijeUJson(rezervacije);
    } catch (Exception e) {
      return ERROR_39;
    }
  }

  public List<RezervacijaSjedala> pronadiRezervacijePutnika(String putnik, LocalDate odDatuma,
      LocalDate doDatuma) {

    List<RezervacijaSjedala> rezultat = new ArrayList<>();

    for (List<RezervacijaSjedala> lista : this.rezervacijeSjedala.values()) {

      for (RezervacijaSjedala rezervacija : lista) {

        if (rezervacija.putnik() == null) {
          continue;
        }

        boolean istiPutnik = rezervacija.putnik().equals(putnik);

        LocalDate datumLeta = rezervacija.sjedalo().let().datumPolijetanja();

        boolean uRasponu = !datumLeta.isBefore(odDatuma) && !datumLeta.isAfter(doDatuma);

        boolean rezervirano = rezervacija.statusRezervacije() != StatusRezervacijeSjedala.SLOBODNA;

        if (istiPutnik && uRasponu && rezervirano) {
          rezultat.add(rezervacija);
        }
      }
    }

    return rezultat;
  }

  public String obradiKomanduAvioIspisOznakaDatum(String komanda) {
    var podudaranje = UZORAK_KORISNIK_ISPIS.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String oznakaLeta = podudaranje.group(2);

    LocalDate datum;
    try {
      datum = LocalDate.parse(podudaranje.group(3));
    } catch (Exception e) {
      return ERROR_20;
    }

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    for (Let let : this.letovi.values()) {
      boolean istaOznaka = let.letPodaci().oznakaLeta().equals(oznakaLeta);
      boolean istiDatum = let.datumPolijetanja().equals(datum);

      if (istaOznaka && istiDatum) {
        return OK + " " + let.id();
      }
    }

    return ERROR_27;
  }

  public String obradiAdministracijskuKomandu(String komanda) {
    if (komanda == null || komanda.isBlank()) {
      return ERROR_20;
    }
    if (UZORAK_ADMIN_PONISTI.matcher(komanda).matches()) {
      return this.obradiKomanduRezervacijePonisti();
    }
    if (UZORAK_PING.matcher(komanda).matches()) {
      return OK;
    }
    if (komanda.startsWith("KRAJ ")) {
      return this.obradiKomanduKraj(komanda);
    }
    return ERROR_20;
  }

  public String obradiKomanduRezerviraj(String komanda) {
    var podudaranje = UZORAK_REZERVIRAJ.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idLeta = podudaranje.group(2);
    String putnik = podudaranje.group(3);
    String oznakaRazreda = podudaranje.group(4);

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    Let let = this.letovi.get(idLeta);
    if (let == null) {
      return ERROR_22;
    }

    RazredSjedala razred = this.pretvoriRazred(oznakaRazreda);
    if (razred == null) {
      return ERROR_20;
    }

    if (this.odredivanjeSjedala == null) {
      return ERROR_23;
    }

    try {
      RezervacijaSjedala rezervacija =
          this.odredivanjeSjedala.rezervirajSjedalo(let, putnik, razred);

      if (rezervacija == null || rezervacija.id() == null
          || rezervacija.statusRezervacije() != StatusRezervacijeSjedala.KREIRANA) {
        return ERROR_23;
      }

      this.azurirajRezervaciju(let.id(), rezervacija);
      return OK + " " + rezervacija.id();
    } catch (ProblemKodRezervacije e) {
      return ERROR_23;
    } catch (Exception e) {
      return ERROR_39;
    }
  }

  public String obradiKomanduPotvrdi(String komanda) {
    var podudaranje = UZORAK_POTVRDI.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idRezervacije = podudaranje.group(2);
    String putnik = podudaranje.group(3);

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    RezervacijaSjedala rezervacija = this.pronadiRezervacijuPoId(idRezervacije);
    if (rezervacija == null) {
      return ERROR_24;
    }

    if (rezervacija.putnik() == null || !rezervacija.putnik().equals(putnik)
        || rezervacija.statusRezervacije() != StatusRezervacijeSjedala.KREIRANA) {
      return ERROR_25;
    }

    try {
      RezervacijaSjedala potvrdena;

      if (this.odredivanjeSjedala != null) {
        potvrdena = this.odredivanjeSjedala.potvrdiRezervacijuSjedala(rezervacija);
      } else {
        potvrdena = rezervacija.potvrdiRezervacijuSjedala();
      }

      if (potvrdena == null
          || potvrdena.statusRezervacije() != StatusRezervacijeSjedala.POTVRDENA) {
        return ERROR_25;
      }

      this.azurirajRezervaciju(rezervacija.sjedalo().let().id(), potvrdena);
      return OK;
    } catch (ProblemKodRezervacije e) {
      return ERROR_25;
    } catch (Exception e) {
      return ERROR_39;
    }
  }

  public String obradiKomanduRezervirajGrupa(String komanda) {
    var podudaranje = UZORAK_REZERVIRAJ_GRUPA.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idLeta = podudaranje.group(2);
    String putniciTekst = podudaranje.group(3);
    String oznakaRazreda = podudaranje.group(4);

    List<String> putnici = this.parsirajPutnikeGrupe(putniciTekst);
    if (putnici.isEmpty()) {
      return ERROR_20;
    }

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    Let let = this.letovi.get(idLeta);
    if (let == null) {
      return ERROR_22;
    }

    RazredSjedala razred = this.pretvoriRazred(oznakaRazreda);
    if (razred == null) {
      return ERROR_20;
    }

    if (this.odredivanjeSjedala == null) {
      return ERROR_23;
    }

    try {
      List<RezervacijaSjedala> rezervacije =
          this.odredivanjeSjedala.rezervirajSjedalaZaGrupu(let, putnici, razred);

      if (rezervacije == null || rezervacije.isEmpty() || rezervacije.size() != putnici.size()) {
        return ERROR_23;
      }

      StringBuilder odgovor = new StringBuilder(OK);

      for (RezervacijaSjedala rezervacija : rezervacije) {
        if (rezervacija == null || rezervacija.id() == null
            || rezervacija.statusRezervacije() != StatusRezervacijeSjedala.KREIRANA) {
          return ERROR_23;
        }

        this.azurirajRezervaciju(let.id(), rezervacija);
        odgovor.append(" ").append(rezervacija.id());
      }

      return odgovor.toString();
    } catch (ProblemKodRezervacije e) {
      return ERROR_23;
    } catch (Exception e) {
      return ERROR_39;
    }
  }

  public String obradiKomanduPing(String komanda) {
    if (!UZORAK_PING.matcher(komanda).matches()) {
      return ERROR_20;
    }
    return OK;
  }

  public String obradiKomanduPauza(String komanda) {
    var podudaranje = UZORAK_PAUZA.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    try {
      long milisekunde = Long.parseLong(podudaranje.group(1));
      Thread.sleep(milisekunde);
      return OK;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ERROR_29;
    } catch (Exception e) {
      return ERROR_20;
    }
  }

  public String obradiKomanduRezervacijePonisti() {
    this.rezervacijeSjedala.clear();
    this.rasporediRedovaPoLetu.clear();
    this.inicijalizirajRezervacijeSjedala();
    return OK;
  }

  public String obradiKomanduKraj(String komanda) {
    var podudaranje = UZORAK_ADMIN_KRAJ.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String primljeniKod = podudaranje.group(1);

    if (!this.kodZaKraj.equals(primljeniKod)) {
      return ERROR_28;
    }

    this.kraj.set(true);
    this.zatvoriUticnice();
    return OK;
  }

  public void azurirajRezervaciju(String idLeta, RezervacijaSjedala rezervacija) {
    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(idLeta);
    if (lista == null) {
      return;
    }

    for (int i = 0; i < lista.size(); i++) {
      RezervacijaSjedala postojeca = lista.get(i);
      boolean istiRed = postojeca.sjedalo().red() == rezervacija.sjedalo().red();
      boolean istoSjedalo =
          postojeca.sjedalo().oznakaSjedala().equals(rezervacija.sjedalo().oznakaSjedala());

      if (istiRed && istoSjedalo) {
        lista.set(i, rezervacija);
        return;
      }
    }

    lista.add(rezervacija);
  }

  public String obradiKomanduAvioZatvoriRedove(String komanda) {
    var podudaranje = UZORAK_AVIO_ZATVORI_REDOVI.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idAvioTvrtke = podudaranje.group(2);
    String idLeta = podudaranje.group(3);

    int odReda;
    int doReda;
    try {
      odReda = Integer.parseInt(podudaranje.group(4));
      doReda = Integer.parseInt(podudaranje.group(5));
    } catch (Exception e) {
      return ERROR_20;
    }

    if (odReda > doReda) {
      return ERROR_20;
    }

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    Let let = this.letovi.get(idLeta);
    if (let == null || !this.odgovaraAvioTvrtka(let, idAvioTvrtke)) {
      return ERROR_22;
    }

    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(idLeta);
    if (lista == null) {
      return ERROR_22;
    }

    for (RezervacijaSjedala rezervacija : lista) {
      int red = rezervacija.sjedalo().red();
      boolean uRasponu = red >= odReda && red <= doReda;
      boolean zauzeto = rezervacija.statusRezervacije() == StatusRezervacijeSjedala.KREIRANA
          || rezervacija.statusRezervacije() == StatusRezervacijeSjedala.POTVRDENA;

      if (uRasponu && zauzeto) {
        return ERROR_26;
      }
    }

    this.zatvoriSjedalaURedovima(idLeta, odReda, doReda);
    return OK;
  }

  public String obradiKomanduAvioZatvoriDatume(String komanda) {
    var podudaranje = UZORAK_AVIO_ZATVORI_DATUMI.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idAvioTvrtke = podudaranje.group(2);

    LocalDate odDatuma;
    LocalDate doDatuma;
    try {
      odDatuma = LocalDate.parse(podudaranje.group(3));
      doDatuma = LocalDate.parse(podudaranje.group(4));
    } catch (Exception e) {
      return ERROR_20;
    }

    if (odDatuma.isAfter(doDatuma)) {
      return ERROR_20;
    }

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    for (Let let : this.letovi.values()) {
      boolean odgovaraTvrtka = this.odgovaraAvioTvrtka(let, idAvioTvrtke);
      boolean uIntervalu =
          !let.datumPolijetanja().isBefore(odDatuma) && !let.datumPolijetanja().isAfter(doDatuma);

      if (odgovaraTvrtka && uIntervalu) {
        this.zatvoriSvaSjedalaLeta(let.id());
      }
    }

    return OK;
  }

  public String obradiKomanduAvioInicijalizirajLet(String komanda) {
    var podudaranje = UZORAK_AVIO_INICIJALIZIRAJ_LET.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idAvioTvrtke = podudaranje.group(2);
    String oznakaLeta = podudaranje.group(3);

    LocalDate odDatuma;
    LocalDate doDatuma;
    try {
      odDatuma = LocalDate.parse(podudaranje.group(4));
      doDatuma = LocalDate.parse(podudaranje.group(5));
    } catch (Exception e) {
      return ERROR_20;
    }

    if (odDatuma.isAfter(doDatuma)) {
      return ERROR_20;
    }

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    LetPodaci letPodaci = this.letoviPodaci.get(oznakaLeta);
    if (letPodaci == null || !letPodaci.avioTvrtka().id().equals(idAvioTvrtke)) {
      return ERROR_27;
    }

    LocalDate datum = odDatuma;
    while (!datum.isAfter(doDatuma)) {
      this.inicijalizirajLetZaDatum(letPodaci, datum);
      datum = datum.plusDays(1);
    }

    return OK;
  }

  public String obradiKomanduAvioInicijalizirajTvrtku(String komanda) {
    var podudaranje = UZORAK_AVIO_INICIJALIZIRAJ_TVRTKA.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idAvioTvrtke = podudaranje.group(2);

    LocalDate odDatuma;
    LocalDate doDatuma;
    try {
      odDatuma = LocalDate.parse(podudaranje.group(3));
      doDatuma = LocalDate.parse(podudaranje.group(4));
    } catch (Exception e) {
      return ERROR_20;
    }

    if (odDatuma.isAfter(doDatuma)) {
      return ERROR_20;
    }

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    for (LetPodaci letPodaci : this.letoviPodaci.values()) {
      if (!letPodaci.avioTvrtka().id().equals(idAvioTvrtke)) {
        continue;
      }

      LocalDate datum = odDatuma;
      while (!datum.isAfter(doDatuma)) {
        this.inicijalizirajLetZaDatum(letPodaci, datum);
        datum = datum.plusDays(1);
      }
    }

    return OK;
  }

  public String obradiKomanduAvioIspis(String komanda) {
    var podudaranje = UZORAK_AVIO_ISPIS.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_20;
    }

    String idUlaznice = podudaranje.group(1);
    String idLeta = podudaranje.group(2);

    if (!this.provjeriUlaznicu(idUlaznice)) {
      return ERROR_21;
    }

    Let let = this.letovi.get(idLeta);
    if (let == null) {
      return ERROR_22;
    }

    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(idLeta);
    if (lista == null) {
      return OK + " []";
    }

    try {
      return OK + " " + this.pretvoriRezervacijeUJson(lista);
    } catch (Exception e) {
      return ERROR_23;
    }
  }

  public boolean odgovaraAvioTvrtka(Let let, String idAvioTvrtke) {
    if (let == null || let.letPodaci() == null || let.letPodaci().avioTvrtka() == null) {
      return false;
    }
    return let.letPodaci().avioTvrtka().id().equals(idAvioTvrtke);
  }

  public void zatvoriSjedalaURedovima(String idLeta, int odReda, int doReda) {
    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(idLeta);
    if (lista == null) {
      return;
    }

    for (int i = 0; i < lista.size(); i++) {
      RezervacijaSjedala rezervacija = lista.get(i);
      int red = rezervacija.sjedalo().red();

      if (red >= odReda && red <= doReda) {
        lista.set(i, rezervacija.zatvoriRezervacijuSjedala());
      }
    }
  }

  public void zatvoriSvaSjedalaLeta(String idLeta) {
    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(idLeta);
    if (lista == null) {
      return;
    }

    for (int i = 0; i < lista.size(); i++) {
      RezervacijaSjedala rezervacija = lista.get(i);
      lista.set(i, rezervacija.zatvoriRezervacijuSjedala());
    }
  }

  public void inicijalizirajLetZaDatum(LetPodaci letPodaci, LocalDate datum) {
    if (letPodaci == null || datum == null) {
      return;
    }

    Let let = Let.kreirajLet(letPodaci, datum);
    if (this.letovi.containsKey(let.id())) {
      return;
    }

    this.letovi.put(let.id(), let);
    this.inicijalizirajRezervacijeSjedalaZaLet(let);
  }

  public void inicijalizirajRezervacijeSjedalaZaLet(Let let) {
    List<RezervacijaSjedala> lista = new CopyOnWriteArrayList<>();
    Map<Integer, String> rasporediRedova = new ConcurrentHashMap<>();
    Avion avion = let.letPodaci().avion();

    if (avion == null || avion.datoteka() == null || avion.datoteka().isBlank()) {
      return;
    }

    List<String> linije = this.ucitajLinijeDatoteke(avion.datoteka());
    if (linije.isEmpty()) {
      return;
    }

    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      if (linija.isBlank()) {
        continue;
      }

      List<String> polja = this.razdvojiCsvLiniju(linija);
      if (polja.size() < 3) {
        continue;
      }

      int red;
      try {
        red = Integer.parseInt(polja.get(0).trim());
      } catch (Exception e) {
        continue;
      }

      RazredSjedala razred = this.pretvoriRazred(polja.get(1).trim());
      if (razred == null) {
        continue;
      }

      String rasporedSjedala = polja.get(2).trim();
      List<String> oznakeSjedala = this.parsirajOznakeSjedala(rasporedSjedala);
      rasporediRedova.put(red, rasporedSjedala);

      for (String oznakaSjedala : oznakeSjedala) {
        LetSjedalo letSjedalo = new LetSjedalo(let, razred, red, oznakaSjedala);
        LocalDateTime vrijeme =
            LocalDateTime.of(let.datumPolijetanja(), let.letPodaci().vrijemePolijetanja());

        RezervacijaSjedala inicijalna =
            RezervacijaSjedala.inicijalizirajRezervacijuSjedala(letSjedalo, vrijeme);

        if (inicijalna != null) {
          lista.add(inicijalna);
        }
      }
    }

    this.rezervacijeSjedala.put(let.id(), lista);
    this.rasporediRedovaPoLetu.put(let.id(), rasporediRedova);
  }

  public String pretvoriRezervacijeUJson(List<RezervacijaSjedala> lista) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");

    for (int i = 0; i < lista.size(); i++) {
      RezervacijaSjedala rezervacija = lista.get(i);
      if (i > 0) {
        sb.append(",");
      }

      sb.append("{");
      sb.append("\"id\":\"").append(this.escapeJson(rezervacija.id())).append("\",");
      sb.append("\"sjedalo\":{");
      sb.append("\"let\":{");
      sb.append("\"id\":\"").append(this.escapeJson(rezervacija.sjedalo().let().id()))
          .append("\",");
      sb.append("\"letPodaci\":{");
      sb.append("\"oznakaLeta\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().let().letPodaci().oznakaLeta()))
          .append("\",");
      sb.append("\"avioTvrtka\":{");
      sb.append("\"id\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().let().letPodaci().avioTvrtka().id()))
          .append("\",");
      sb.append("\"naziv\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().let().letPodaci().avioTvrtka().naziv()))
          .append("\"},");
      sb.append("\"vrijemePolijetanja\":\"")
          .append(rezervacija.sjedalo().let().letPodaci().vrijemePolijetanja()).append("\",");
      sb.append("\"polazniAerodrom\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().let().letPodaci().polazniAerodrom()))
          .append("\",");
      sb.append("\"odredisniAerodrom\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().let().letPodaci().odredisniAerodrom()))
          .append("\",");
      sb.append("\"avion\":{");
      sb.append("\"id\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().let().letPodaci().avion().id()))
          .append("\",");
      sb.append("\"model\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().let().letPodaci().avion().naziv()))
          .append("\"}");
      sb.append("},");
      sb.append("\"datumPolijetanja\":\"").append(rezervacija.sjedalo().let().datumPolijetanja())
          .append("\"},");
      sb.append("\"razred\":\"").append(rezervacija.sjedalo().razred()).append("\",");
      sb.append("\"red\":").append(rezervacija.sjedalo().red()).append(",");
      sb.append("\"oznakaSjedala\":\"")
          .append(this.escapeJson(rezervacija.sjedalo().oznakaSjedala())).append("\"},");
      sb.append("\"putnik\":");
      if (rezervacija.putnik() == null) {
        sb.append("null");
      } else {
        sb.append("\"").append(this.escapeJson(rezervacija.putnik())).append("\"");
      }
      sb.append(",");
      sb.append("\"vrijemeRezervacije\":\"").append(rezervacija.vrijemeRezervacije()).append("\",");
      sb.append("\"statusRezervacije\":\"").append(rezervacija.statusRezervacije()).append("\"");
      sb.append("}");
    }

    sb.append("]");
    return sb.toString();
  }

  public String escapeJson(String tekst) {
    if (tekst == null) {
      return "";
    }

    return tekst.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  public RazredSjedala pretvoriRazred(String oznakaRazreda) {
    if ("P".equals(oznakaRazreda)) {
      return RazredSjedala.POSLOVNI;
    }
    if ("E".equals(oznakaRazreda)) {
      return RazredSjedala.EKONOMSKI;
    }
    return null;
  }

  public boolean provjeriUlaznicu(String idUlaznice) {
    try (Socket uticnica = new Socket(this.adresaKontrola, this.mreznaVrataKontrola);
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(uticnica.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(uticnica.getInputStream(), StandardCharsets.UTF_8))) {

      out.println("ULAZNICA PROVJERI " + idUlaznice);
      String odgovor = in.readLine();
      return odgovor != null && odgovor.startsWith("OK");
    } catch (Exception e) {
      return false;
    }
  }

  public void posaljiOdgovor(OutputStream izlaz, String odgovor) throws IOException {
    izlaz.write((odgovor + "\n").getBytes(StandardCharsets.UTF_8));
    izlaz.flush();
  }

  public void zaustaviPrijemNovihKomandi() {
    this.primaNoveKorisnickeKomande.set(false);
    this.primaNoveAvioKomande.set(false);
  }

  public void nastaviPrijemNovihKomandi() {
    this.primaNoveKorisnickeKomande.set(true);
    this.primaNoveAvioKomande.set(true);
  }

  public void pricekajAktivneObrade() {
    while (this.aktivneKorisnickeObrade.get() > 0 || this.aktivneAvioObrade.get() > 0) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  public void zatvoriUticnice() {
    this.zatvoriKorisnickuUticnicu();
    this.zatvoriAvioUticnicu();
    this.zatvoriAdministracijskuUticnicu();
  }

  public void zatvoriKorisnickuUticnicu() {
    try {
      if (this.korisnickaUticnica != null && !this.korisnickaUticnica.isClosed()) {
        this.korisnickaUticnica.close();
      }
    } catch (IOException e) {
      // kontrolirano
    }
  }

  public void zatvoriAvioUticnicu() {
    try {
      if (this.avioUticnica != null && !this.avioUticnica.isClosed()) {
        this.avioUticnica.close();
      }
    } catch (IOException e) {
      // kontrolirano
    }
  }

  public void zatvoriAdministracijskuUticnicu() {
    try {
      if (this.administracijskaUticnica != null && !this.administracijskaUticnica.isClosed()) {
        this.administracijskaUticnica.close();
      }
    } catch (IOException e) {
    }
  }

  public void ucitajOdredivanjeSjedala() {
    try {
      if (this.odredivanjeSjedalaKlasa == null || this.odredivanjeSjedalaKlasa.isBlank()) {
        return;
      }

      Class<?> klasa = Class.forName(this.odredivanjeSjedalaKlasa);
      Object objekt = klasa.getDeclaredConstructor().newInstance();

      if (objekt instanceof OdredivanjeSjedala algoritam) {
        this.odredivanjeSjedala = algoritam;
        this.odredivanjeSjedala.inicijaliziraj();
      }
    } catch (Exception e) {
      this.odredivanjeSjedala = null;
    }
  }

  public void ucitajPodatke() {
    this.ucitajAvioTvrtke();
    this.ucitajAvione();
    this.ucitajLetovePodaci();
    this.inicijalizirajLetove();
    this.inicijalizirajRezervacijeSjedala();


  }

  public void ucitajAvioTvrtke() {
    List<String> linije = this.ucitajLinijeDatoteke(this.datotekaAvioTvrtke);
    if (linije.isEmpty()) {
      return;
    }

    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      if (linija.isBlank()) {
        continue;
      }

      List<String> polja = this.razdvojiCsvLiniju(linija);
      if (polja.size() < 2) {
        continue;
      }

      String id = polja.get(0).trim();
      String naziv = polja.get(1).trim();

      if (id.isBlank() || naziv.isBlank()) {
        continue;
      }

      if (this.avioTvrtke.containsKey(id)) {
        continue;
      }

      AvioTvrtka avioTvrtka = new AvioTvrtka(id, naziv, new ConcurrentHashMap<>());
      this.avioTvrtke.put(id, avioTvrtka);
    }
  }

  public void ucitajAvione() {
    List<String> linije = this.ucitajLinijeDatoteke(this.datotekaAvioni);
    if (linije.isEmpty()) {
      return;
    }

    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      if (linija.isBlank()) {
        continue;
      }

      List<String> polja = this.razdvojiCsvLiniju(linija);
      polja = this.ispraviPoljaAviona(polja);

      if (polja.size() < 4) {
        continue;
      }

      String id = polja.get(0).trim();
      String datoteka = polja.get(1).trim();
      String naziv = polja.get(2).trim();
      String specifikacijeTekst = polja.get(3).trim();

      if (id.isBlank() || datoteka.isBlank() || naziv.isBlank()) {
        continue;
      }

      if (this.avioni.containsKey(id)) {
        continue;
      }

      Map<String, String> specifikacije = this.parsirajSpecifikacijeAviona(specifikacijeTekst);

      Avion avion = new Avion(id, datoteka, naziv, specifikacije);
      this.avioni.put(id, avion);
    }
  }

  public void ucitajLetovePodaci() {
    List<String> linije = this.ucitajLinijeDatoteke(this.datotekaLetovi);
    if (linije.isEmpty()) {
      return;
    }

    for (int i = 1; i < linije.size(); i++) {
      String linija = linije.get(i).trim();
      if (linija.isBlank()) {
        continue;
      }

      List<String> polja = this.razdvojiCsvLiniju(linija);
      polja = this.ispraviPoljaLeta(polja);

      if (polja.size() < 7) {
        continue;
      }

      String oznakaLeta = polja.get(0).trim();
      String idAvioTvrtke = polja.get(1).trim();
      String polazni = polja.get(2).trim();
      String odredisni = polja.get(3).trim();
      String vrijeme = polja.get(5).trim();
      String oznakaAviona = polja.get(6).trim();

      if (oznakaLeta.isBlank() || idAvioTvrtke.isBlank() || polazni.isBlank() || odredisni.isBlank()
          || vrijeme.isBlank() || oznakaAviona.isBlank()) {
        continue;
      }

      if (this.letoviPodaci.containsKey(oznakaLeta)) {
        continue;
      }

      AvioTvrtka avioTvrtka = this.avioTvrtke.get(idAvioTvrtke);
      Avion avion = this.avioni.get(oznakaAviona);

      if (avioTvrtka == null || avion == null) {
        continue;
      }

      LocalTime vrijemePolijetanja;
      try {
        vrijemePolijetanja = LocalTime.parse(vrijeme);
      } catch (Exception e) {
        continue;
      }

      avioTvrtka.dodajAvion(avion);

      LetPodaci letPodaci =
          new LetPodaci(oznakaLeta, avioTvrtka, vrijemePolijetanja, polazni, odredisni, avion);

      this.letoviPodaci.put(oznakaLeta, letPodaci);
    }
  }

  public void inicijalizirajLetove() {
    if (this.datumOd == null || this.datumDo == null) {
      return;
    }

    for (LetPodaci letPodaci : this.letoviPodaci.values()) {
      LocalDate datum = this.datumOd;

      while (!datum.isAfter(this.datumDo)) {
        Let let = Let.kreirajLet(letPodaci, datum);

        if (!this.letovi.containsKey(let.id())) {
          this.letovi.put(let.id(), let);
        }

        datum = datum.plusDays(1);
      }
    }
  }

  public void inicijalizirajRezervacijeSjedala() {
    for (Let let : this.letovi.values()) {
      List<RezervacijaSjedala> lista = new CopyOnWriteArrayList<>();
      Map<Integer, String> rasporediRedova = new ConcurrentHashMap<>();
      Avion avion = let.letPodaci().avion();

      if (avion == null || avion.datoteka() == null || avion.datoteka().isBlank()) {
        continue;
      }

      List<String> linije = this.ucitajLinijeDatoteke(avion.datoteka());
      if (linije.isEmpty()) {
        continue;
      }

      for (int i = 1; i < linije.size(); i++) {
        String linija = linije.get(i).trim();
        if (linija.isBlank()) {
          continue;
        }

        List<String> polja = this.razdvojiCsvLiniju(linija);
        if (polja.size() < 3) {
          continue;
        }

        int red;
        try {
          red = Integer.parseInt(polja.get(0).trim());
        } catch (Exception e) {
          continue;
        }

        RazredSjedala razred = this.pretvoriRazred(polja.get(1).trim());
        if (razred == null) {
          continue;
        }

        String rasporedSjedala = polja.get(2).trim();
        List<String> oznakeSjedala = this.parsirajOznakeSjedala(rasporedSjedala);
        rasporediRedova.put(red, rasporedSjedala);

        for (String oznakaSjedala : oznakeSjedala) {
          LetSjedalo letSjedalo = new LetSjedalo(let, razred, red, oznakaSjedala);
          LocalDateTime vrijeme =
              LocalDateTime.of(let.datumPolijetanja(), let.letPodaci().vrijemePolijetanja());

          RezervacijaSjedala inicijalna =
              RezervacijaSjedala.inicijalizirajRezervacijuSjedala(letSjedalo, vrijeme);

          if (inicijalna != null) {
            lista.add(inicijalna);
          }
        }
      }

      this.rezervacijeSjedala.put(let.id(), lista);
      this.rasporediRedovaPoLetu.put(let.id(), rasporediRedova);
    }
  }

  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
      this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
      this.maksBrojDretvi = Integer.parseInt(this.konfig.dajPostavku("maksBrojDretvi"));
      this.maksBrojCekaca = Integer.parseInt(this.konfig.dajPostavku("maksBrojCekaca"));
      this.mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
      this.mreznaVrataAdministracija =
          Integer.parseInt(this.konfig.dajPostavku("mreznaVrataAdministracija"));
      this.mreznaVrataAvioKompanije = this.ucitajMreznaVrataAvioKompanije();
      this.adresaKontrola = this.konfig.dajPostavku("adresaKontrola");
      this.mreznaVrataKontrola = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKontrola"));
      this.datotekaAvioTvrtke = this.konfig.dajPostavku("datoteka_avioTvrtke");
      this.datotekaAvioni = this.konfig.dajPostavku("datoteka_avioni");
      this.datotekaLetovi = this.konfig.dajPostavku("datoteka_letovi");
      this.odredivanjeSjedalaKlasa = this.konfig.dajPostavkuOsnovno("odredivanjeSjedala", "");
      this.datumOd = LocalDate.parse(this.konfig.dajPostavku("datumOd"));
      this.datumDo = LocalDate.parse(this.konfig.dajPostavku("datumDo"));

      return true;
    } catch (NeispravnaKonfiguracija e) {
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  public int ucitajMreznaVrataAvioKompanije() {
    String vrijednost = this.konfig.dajPostavkuOsnovno("mreznaVrataAvioKompanije", null);
    if (vrijednost == null || vrijednost.isBlank()) {
      vrijednost = this.konfig.dajPostavku("mreznaVrataAvioTvrtke");
    }
    return Integer.parseInt(vrijednost);
  }

  public RezervacijaSjedala pronadiRezervacijuPoId(String idRezervacije) {
    for (List<RezervacijaSjedala> lista : this.rezervacijeSjedala.values()) {
      for (RezervacijaSjedala rezervacija : lista) {
        if (rezervacija.id().equals(idRezervacije)) {
          return rezervacija;
        }
      }
    }
    return null;
  }

  public List<String> parsirajPutnikeGrupe(String putniciTekst) {
    if (putniciTekst == null || putniciTekst.isBlank()) {
      return List.of();
    }

    String ocisceniTekst = putniciTekst.trim();
    String[] dijelovi = ocisceniTekst.split("\\s+");
    List<String> putnici = new ArrayList<>();

    for (String dio : dijelovi) {
      if (dio != null && !dio.isBlank()) {
        putnici.add(dio.trim());
      }
    }

    return putnici;
  }

  public List<RezervacijaSjedala> dohvatiRezervacijeLeta(String idLeta) {
    return this.rezervacijeSjedala.get(idLeta);
  }

  public List<RezervacijaSjedala> dohvatiSlobodnaSjedala(Let let, RazredSjedala razred) {
    List<RezervacijaSjedala> rezultat = new ArrayList<>();
    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(let.id());

    if (lista == null) {
      return rezultat;
    }

    for (RezervacijaSjedala rezervacija : lista) {
      boolean istiLet = rezervacija.sjedalo().let().id().equals(let.id());
      boolean istiRazred = rezervacija.sjedalo().razred() == razred;
      boolean slobodno = rezervacija.statusRezervacije() == StatusRezervacijeSjedala.SLOBODNA;

      if (istiLet && istiRazred && slobodno) {
        rezultat.add(rezervacija);
      }
    }

    return rezultat;
  }

  public List<RezervacijaSjedala> dohvatiSlobodnaSjedalaReda(Let let, RazredSjedala razred,
      int red) {
    List<RezervacijaSjedala> rezultat = new ArrayList<>();
    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(let.id());

    if (lista == null) {
      return rezultat;
    }

    for (RezervacijaSjedala rezervacija : lista) {
      boolean istiLet = rezervacija.sjedalo().let().id().equals(let.id());
      boolean istiRazred = rezervacija.sjedalo().razred() == razred;
      boolean istiRed = rezervacija.sjedalo().red() == red;
      boolean slobodno = rezervacija.statusRezervacije() == StatusRezervacijeSjedala.SLOBODNA;

      if (istiLet && istiRazred && istiRed && slobodno) {
        rezultat.add(rezervacija);
      }
    }

    return rezultat;
  }

  public List<RezervacijaSjedala> dohvatiSlobodnaSjedalaDijelaReda(Let let, RazredSjedala razred,
      int red, String dio) {
    List<RezervacijaSjedala> rezultat = new ArrayList<>();
    List<RezervacijaSjedala> slobodna = this.dohvatiSlobodnaSjedalaReda(let, razred, red);

    for (RezervacijaSjedala rezervacija : slobodna) {
      String oznaka = rezervacija.sjedalo().oznakaSjedala();
      if (dio.contains(oznaka)) {
        rezultat.add(rezervacija);
      }
    }

    return rezultat;
  }

  public List<Integer> dohvatiRedoveRazreda(Let let, RazredSjedala razred) {
    List<Integer> redovi = new ArrayList<>();
    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(let.id());

    if (lista == null) {
      return redovi;
    }

    for (RezervacijaSjedala rezervacija : lista) {
      if (rezervacija.sjedalo().razred() != razred) {
        continue;
      }

      int red = rezervacija.sjedalo().red();
      if (!redovi.contains(red)) {
        redovi.add(red);
      }
    }

    redovi.sort(Integer::compareTo);
    return redovi;
  }

  public String dohvatiRasporedReda(Let let, int red) {
    if (let == null) {
      return null;
    }

    Map<Integer, String> rasporedi = this.rasporediRedovaPoLetu.get(let.id());
    if (rasporedi == null) {
      return null;
    }

    return rasporedi.get(red);
  }

  public List<String> dohvatiDijeloveReda(Let let, int red) {
    String raspored = this.dohvatiRasporedReda(let, red);
    List<String> rezultat = new ArrayList<>();

    if (raspored == null || raspored.isBlank()) {
      return rezultat;
    }

    String[] dijelovi = raspored.split("_");
    for (String dio : dijelovi) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < dio.length(); i++) {
        char znak = dio.charAt(i);
        if (znak != '-') {
          sb.append(znak);
        }
      }

      if (sb.length() > 0) {
        rezultat.add(sb.toString());
      }
    }

    return rezultat;
  }

  public int dohvatiNajveciBrojSjedalaUDijelu(Let let, RazredSjedala razred) {
    List<RezervacijaSjedala> lista = this.rezervacijeSjedala.get(let.id());
    if (lista == null) {
      return 0;
    }

    int najveci = 0;
    List<Integer> obradeniRedovi = new ArrayList<>();

    for (RezervacijaSjedala rezervacija : lista) {
      if (rezervacija.sjedalo().razred() != razred) {
        continue;
      }

      int red = rezervacija.sjedalo().red();
      if (obradeniRedovi.contains(red)) {
        continue;
      }

      obradeniRedovi.add(red);
      List<String> dijelovi = this.dohvatiDijeloveReda(let, red);

      for (String dio : dijelovi) {
        if (dio.length() > najveci) {
          najveci = dio.length();
        }
      }
    }

    return najveci;
  }

  public RezervacijaSjedala kreirajRezervaciju(RezervacijaSjedala slobodna, String putnik) {
    return slobodna.kreirajRezervacijuSjedala(putnik, LocalDateTime.now());
  }

  public RezervacijaSjedala potvrdiPostojecuRezervaciju(RezervacijaSjedala rezervacija) {
    return rezervacija.potvrdiRezervacijuSjedala();
  }

  public void azurirajRezervacijeGrupe(String idLeta, List<RezervacijaSjedala> rezervacije) {
    for (RezervacijaSjedala rezervacija : rezervacije) {
      this.azurirajRezervaciju(idLeta, rezervacija);
    }
  }

  public void vratiRezervacijuNaSlobodna(String idLeta, RezervacijaSjedala rezervacija) {
    if (idLeta == null || rezervacija == null || rezervacija.sjedalo() == null) {
      return;
    }

    LocalDateTime vrijeme = LocalDateTime.of(rezervacija.sjedalo().let().datumPolijetanja(),
        rezervacija.sjedalo().let().letPodaci().vrijemePolijetanja());

    RezervacijaSjedala slobodna =
        RezervacijaSjedala.inicijalizirajRezervacijuSjedala(rezervacija.sjedalo(), vrijeme);

    if (slobodna != null) {
      this.azurirajRezervaciju(idLeta, slobodna);
    }
  }

  public void vratiRezervacijeGrupeNaSlobodna(String idLeta, List<RezervacijaSjedala> rezervacije) {
    if (idLeta == null || rezervacije == null || rezervacije.isEmpty()) {
      return;
    }

    for (RezervacijaSjedala rezervacija : rezervacije) {
      this.vratiRezervacijuNaSlobodna(idLeta, rezervacija);
    }
  }

  public List<String> ucitajLinijeDatoteke(String nazivDatoteke) {
    try {
      Path putanja = Path.of(nazivDatoteke);
      if (!Files.exists(putanja)) {
        return List.of();
      }
      return Files.readAllLines(putanja, StandardCharsets.UTF_8);
    } catch (IOException e) {
      return List.of();
    }
  }

  public List<String> razdvojiCsvLiniju(String linija) {
    List<String> polja = new ArrayList<>();
    StringBuilder trenutno = new StringBuilder();
    boolean uNavodnicima = false;

    for (int i = 0; i < linija.length(); i++) {
      char znak = linija.charAt(i);

      if (znak == '"') {
        uNavodnicima = !uNavodnicima;
        continue;
      }

      if (znak == ',' && !uNavodnicima) {
        polja.add(trenutno.toString().trim());
        trenutno.setLength(0);
      } else {
        trenutno.append(znak);
      }
    }

    polja.add(trenutno.toString().trim());
    return polja;
  }

  public List<String> ispraviPoljaAviona(List<String> polja) {
    if (polja.size() == 4) {
      return polja;
    }

    if (polja.size() == 3 && polja.get(1).contains(".csv")) {
      String drugi = polja.get(1).trim();
      int indeks = drugi.indexOf(".csv");
      if (indeks > 0 && indeks + 4 < drugi.length()) {
        String datoteka = drugi.substring(0, indeks + 4).trim();
        String naziv = drugi.substring(indeks + 4).trim();
        List<String> nova = new ArrayList<>();
        nova.add(polja.get(0).trim());
        nova.add(datoteka);
        nova.add(naziv);
        nova.add(polja.get(2).trim());
        return nova;
      }
    }

    return polja;
  }

  public List<String> ispraviPoljaLeta(List<String> polja) {
    if (polja.size() == 7) {
      return polja;
    }

    if (polja.size() == 6) {
      String spojeno = polja.get(4).trim();
      String[] dijelovi = spojeno.split("\\s+");
      if (dijelovi.length == 2) {
        List<String> nova = new ArrayList<>();
        nova.add(polja.get(0).trim());
        nova.add(polja.get(1).trim());
        nova.add(polja.get(2).trim());
        nova.add(polja.get(3).trim());
        nova.add(dijelovi[0].trim());
        nova.add(dijelovi[1].trim());
        nova.add(polja.get(5).trim());
        return nova;
      }
    }

    return polja;
  }

  public Map<String, String> parsirajSpecifikacijeAviona(String tekst) {
    Map<String, String> specifikacije = new LinkedHashMap<>();
    if (tekst == null || tekst.isBlank()) {
      return specifikacije;
    }

    String ocisceno = tekst.trim();
    if (ocisceno.startsWith("\"") && ocisceno.endsWith("\"") && ocisceno.length() >= 2) {
      ocisceno = ocisceno.substring(1, ocisceno.length() - 1);
    }

    String[] dijelovi = ocisceno.split(",");
    for (String dio : dijelovi) {
      String zapis = dio.trim();
      if (zapis.isBlank()) {
        continue;
      }

      int dvotocka = zapis.indexOf(':');
      if (dvotocka <= 0) {
        continue;
      }

      String kljuc = zapis.substring(0, dvotocka).trim();
      String vrijednost = zapis.substring(dvotocka + 1).trim();

      if (!kljuc.isBlank()) {
        specifikacije.put(kljuc, vrijednost);
      }
    }

    return specifikacije;
  }

  public List<String> parsirajOznakeSjedala(String rasporedSjedala) {
    List<String> rezultat = new ArrayList<>();

    if (rasporedSjedala == null || rasporedSjedala.isBlank()) {
      return rezultat;
    }

    for (int i = 0; i < rasporedSjedala.length(); i++) {
      char znak = rasporedSjedala.charAt(i);
      if (znak == '_' || znak == '-') {
        continue;
      }
      rezultat.add(String.valueOf(znak));
    }

    return rezultat;
  }
}

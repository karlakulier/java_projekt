package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import edu.unizg.foi.nwtis.Ulaznica;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Poslužitelj kontrole.
 */
public class PosluziteljKontrola {

  public static final String USLUGA_REZERVACIJE = "REZERVACIJE";
  public static final String ERROR_10 = "ERROR 10";
  public static final String ERROR_11 = "ERROR 11";
  public static final String ERROR_12 = "ERROR 12";
  public static final String ERROR_13 = "ERROR 13";
  public static final String ERROR_14 = "ERROR 14";
  public static final String ERROR_15 = "ERROR 15";
  public static final String ERROR_16_DODAJ = "ERROR 16";
  public static final String ERROR_16_BRISI = "ERROR 16";
  public static final String ERROR_17 = "ERROR 17";
  public static final String ERROR_18 = "ERROR 18";
  public static final String ERROR_19 = "ERROR 19";

  private static final Pattern UZORAK_KORISNIK_DODAJ =
      Pattern.compile("^KORISNIK DODAJ \"([^\"]+)\" \"([^\"]+)\" (\\S+) (\\S+)$");

  private static final Pattern UZORAK_KORISNIK_PROVJERI =
      Pattern.compile("^KORISNIK PROVJERI (\\S+)$");

  private static final Pattern UZORAK_ULAZNICA_DODAJ =
      Pattern.compile("^ULAZNICA DODAJ (\\S+) (\\S+)$");

  private static final Pattern UZORAK_ULAZNICA_PROVJERI =
      Pattern.compile("^ULAZNICA PROVJERI (\\S+)$");

  private static final Pattern UZORAK_ULAZNICA_KORISNIK =
      Pattern.compile("^ULAZNICA KORISNIK (\\S+)$");

  private static final Pattern UZORAK_ULAZNICA_PONISTI =
      Pattern.compile("^ULAZNICA PONIŠTI (\\S+)$");

  private static final Pattern UZORAK_ADMIN_ULAZNICA_PONISTI =
      Pattern.compile("^ULAZNICA PONIŠTI$");

  private static final Pattern UZORAK_ADMIN_REGISTRIRAJ =
      Pattern.compile("^REGISTRIRAJ (\\S+) (\\d+) (\\d+) (\\S+)$");

  private static final Pattern UZORAK_ADMIN_ZABRANA = Pattern.compile("^ZABRANA (\\S+)$");

  private static final Pattern UZORAK_ADMIN_DOZVOLA = Pattern.compile("^DOZVOLA (\\S+)$");

  private static final Pattern UZORAK_PAUZA = Pattern.compile("^PAUZA (\\d+)$");

  private static final Pattern UZORAK_PING = Pattern.compile("^PING$");

  private static final Pattern UZORAK_ADMIN_KRAJ = Pattern.compile("^KRAJ (\\S+)$");


  private final int mreznaVrata;
  private final int mreznaVrataAdministracija;
  private final long pauzaDretve;
  private final int maksBrojCekaca;
  private final int maksBrojDretvi;
  private final long trajanjeUlaznice;
  private final String kodZaKraj;

  private final Map<String, Korisnik> korisnici = new ConcurrentHashMap<>();
  private final Map<String, Ulaznica> ulaznice = new ConcurrentHashMap<>();
  private final Set<String> zabranjeneAdrese = ConcurrentHashMap.newKeySet();
  private final Map<String, PosluziteljPodaci> registriraniPosluzitelji = new ConcurrentHashMap<>();

  private volatile boolean radiKorisnickiDio = true;
  private volatile boolean radiAdministracijskiDio = true;
  private volatile boolean primaNoveKorisnickeKomande = true;
  private volatile boolean zahtjevZaGasenje = false;

  private final AtomicInteger brojAktivnihKorisnickihObrada = new AtomicInteger(0);

  private ServerSocket korisnickaUticnica;
  private ServerSocket administracijskaUticnica;
  private ExecutorService executorKorisnickeObrade;
  private ExecutorService executorAdministracijskeObrade;

  /**
   * Podaci registriranog poslužitelja.
   */
  public record PosluziteljPodaci(String adresa, int mreznaVrata, int mreznaVrataAdministracija,
      String kodZaKraj) {
  }

  /**
   * Konstruktor.
   *
   * @param konfiguracija konfiguracija
   */
  public PosluziteljKontrola(Konfiguracija konfiguracija) {
    this.mreznaVrata = Integer.parseInt(konfiguracija.dajPostavku("mreznaVrata"));
    this.mreznaVrataAdministracija =
        Integer.parseInt(konfiguracija.dajPostavku("mreznaVrataAdministracija"));
    this.pauzaDretve = Long.parseLong(konfiguracija.dajPostavkuOsnovno("pauzaDretve", "1000"));
    this.maksBrojCekaca = Integer.parseInt(konfiguracija.dajPostavkuOsnovno("maksBrojCekaca", "5"));
    this.maksBrojDretvi = Integer.parseInt(konfiguracija.dajPostavkuOsnovno("maksBrojDretvi", "5"));
    this.trajanjeUlaznice = Long.parseLong(konfiguracija.dajPostavku("trajanjeUlaznice"));
    this.kodZaKraj = konfiguracija.dajPostavku("kodZaKraj");
  }

  /**
   * Glavna metoda.
   *
   * @param args argumenti
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      return;
    }

    try {
      var konfiguracija = KonfiguracijaApstraktna.preuzmiKonfiguraciju(args[0]);
      var posluzitelj = new PosluziteljKontrola(konfiguracija);
      posluzitelj.pokreniPosluzitelj();
    } catch (NeispravnaKonfiguracija e) {
      // error
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Pokreće poslužitelj.
   *
   * @throws InterruptedException u slučaju prekida dretve
   * @throws ExecutionException u slučaju greške izvršavanja
   */
  public void pokreniPosluzitelj() throws InterruptedException, ExecutionException {
    this.dodajShutdownHook();

    var factoryVirtual = Thread.ofVirtual().factory();
    this.executorKorisnickeObrade =
        Executors.newFixedThreadPool(this.maksBrojDretvi, factoryVirtual);
    this.executorAdministracijskeObrade = Executors.newSingleThreadExecutor(factoryVirtual);

    try (var executorGlavneDretve = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<?> korisnickiFuture = executorGlavneDretve.submit(this::pokreniKorisnickiDio);
      Future<?> administracijskiFuture =
          executorGlavneDretve.submit(this::pokreniAdministracijskiDio);

      Thread.sleep(this.pauzaDretve);

      korisnickiFuture.get();
      administracijskiFuture.get();
    } finally {
      this.ugasiIzvrsitelje();
    }
  }

  /**
   * Pokreće korisnički dio.
   */
  public void pokreniKorisnickiDio() {
    try (var server = new ServerSocket(this.mreznaVrata, this.maksBrojCekaca)) {
      this.korisnickaUticnica = server;
      server.setSoTimeout((int) this.pauzaDretve);

      while (this.radiKorisnickiDio) {
        this.prihvatiKorisnickiZahtjev(server);
      }
    } catch (IOException e) {
      this.ispisiGreskuKorisnickogDijela(e);
    }
  }

  /**
   * Prihvaća korisnički zahtjev.
   *
   * @param server poslužiteljska utičnica
   */
  private void prihvatiKorisnickiZahtjev(ServerSocket server) {
    try {
      Socket uticnica = server.accept();
      this.executorKorisnickeObrade.submit(() -> this.obradiKorisnickiZahtjev(uticnica));
    } catch (SocketTimeoutException ex) {
      // timeout služi samo za periodičnu provjeru rada poslužitelja
    } catch (IOException ex) {
      this.ispisiGreskuKorisnickogDijela(ex);
    }
  }

  /**
   * Pokreće administracijski dio.
   */
  public void pokreniAdministracijskiDio() {
    try (var server = new ServerSocket(this.mreznaVrataAdministracija, 1)) {
      this.administracijskaUticnica = server;

      while (this.radiAdministracijskiDio) {
        Socket uticnica = server.accept();
        Future<?> future = this.executorAdministracijskeObrade
            .submit(() -> this.obradiAdministracijskiZahtjev(uticnica));
        future.get();
      }
    } catch (IOException e) {
      this.ispisiGreskuAdministracijskogDijela(e);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      if (this.radiAdministracijskiDio) {
        // eror
      }
    }
  }

  /**
   * Ispisuje grešku korisničkog dijela.
   *
   * @param e iznimka
   */
  public void ispisiGreskuKorisnickogDijela(IOException e) {
    e.printStackTrace();
  }

  public void ispisiGreskuAdministracijskogDijela(IOException e) {
    e.printStackTrace();
  }

  /**
   * Obrađuje korisnički zahtjev.
   *
   * @param uticnica utičnica
   */
  public void obradiKorisnickiZahtjev(Socket uticnica) {
    this.brojAktivnihKorisnickihObrada.incrementAndGet();

    try (uticnica;
        var ulaz = new BufferedReader(
            new InputStreamReader(uticnica.getInputStream(), StandardCharsets.UTF_8));
        var izlaz = uticnica.getOutputStream()) {

      if (this.adresaJeZabranjena(uticnica)) {
        this.posaljiOdgovor(izlaz, ERROR_17);
        return;
      }

      String komanda = ulaz.readLine();
      String odgovor = this.obradiKorisnickuKomandu(komanda);
      this.posaljiOdgovor(izlaz, odgovor);
    } catch (IOException ex) {
      // nema ispisa prema zadatku
    } finally {
      this.brojAktivnihKorisnickihObrada.decrementAndGet();
    }
  }

  /**
   * Obrađuje administracijski zahtjev.
   *
   * @param uticnica utičnica
   */
  public void obradiAdministracijskiZahtjev(Socket uticnica) {
    try (uticnica;
        var ulaz = new BufferedReader(
            new InputStreamReader(uticnica.getInputStream(), StandardCharsets.UTF_8));
        var izlaz = uticnica.getOutputStream()) {

      String komanda = ulaz.readLine();
      this.zahtjevZaGasenje = false;
      this.zaustaviPrijemNovihKorisnickihKomandi();
      this.pricekajAktivneKorisnickeObrade();

      String odgovor = this.obradiAdministracijskuKomandu(komanda);
      this.posaljiOdgovor(izlaz, odgovor);

      if (this.zahtjevZaGasenje && "OK".equals(odgovor)) {
        this.zaustaviPosluzitelj();
      } else {
        this.nastaviPrijemKorisnickihKomandi();
      }
    } catch (IOException ex) {
      this.nastaviPrijemKorisnickihKomandi();
    }
  }

  /**
   * Šalje odgovor.
   *
   * @param izlaz izlazni tok
   * @param odgovor odgovor
   * @throws IOException u slučaju greške
   */
  public void posaljiOdgovor(OutputStream izlaz, String odgovor) throws IOException {
    izlaz.write((odgovor + "\n").getBytes(StandardCharsets.UTF_8));
    izlaz.flush();
  }

  /**
   * Provjerava je li adresa zabranjena.
   *
   * @param uticnica utičnica
   * @return true ako je zabranjena
   */
  public boolean adresaJeZabranjena(Socket uticnica) {
    try {
      InetAddress inetAdresa = uticnica.getInetAddress();

      String brojcanaAdresa = inetAdresa.getHostAddress();
      String opisnaAdresa = inetAdresa.getHostName();
      String kanonskaAdresa = inetAdresa.getCanonicalHostName();

      return this.zabranjeneAdrese.contains(brojcanaAdresa)
          || this.zabranjeneAdrese.contains(opisnaAdresa)
          || this.zabranjeneAdrese.contains(kanonskaAdresa);
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Obrađuje korisničku komandu.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKorisnickuKomandu(String komanda) {
    if (komanda == null || komanda.isBlank()) {
      return ERROR_10;
    }
    if (komanda.startsWith("KORISNIK DODAJ ")) {
      return this.obradiKomanduKorisnikDodaj(komanda);
    }
    if (komanda.startsWith("KORISNIK PROVJERI ")) {
      return this.obradiKomanduKorisnikProvjeri(komanda);
    }
    if (komanda.startsWith("ULAZNICA DODAJ ")) {
      return this.obradiKomanduUlaznicaDodaj(komanda);
    }
    if (komanda.startsWith("ULAZNICA PROVJERI ")) {
      return this.obradiKomanduUlaznicaProvjeri(komanda);
    }
    if (komanda.startsWith("ULAZNICA KORISNIK ")) {
      return this.obradiKomanduUlaznicaKorisnik(komanda);
    }
    if (komanda.startsWith("ULAZNICA PONIŠTI ")) {
      return this.obradiKomanduUlaznicaPonisti(komanda);
    }
    if (komanda.startsWith("PAUZA ")) {
      return this.obradiKomanduPauza(komanda);
    }
    if (komanda.equals("PING")) {
      return this.obradiKomanduPing(komanda);
    }
    return ERROR_10;
  }

  /**
   * Obrađuje administracijsku komandu.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiAdministracijskuKomandu(String komanda) {
    if (komanda == null || komanda.isBlank()) {
      return ERROR_10;
    }
    if (UZORAK_ADMIN_ULAZNICA_PONISTI.matcher(komanda).matches()) {
      return this.obradiAdministracijskuKomanduUlaznicaPonisti();
    }
    if (komanda.startsWith("REGISTRIRAJ ")) {
      return this.obradiAdministracijskuKomanduRegistriraj(komanda);
    }
    if (komanda.startsWith("ZABRANA ")) {
      return this.obradiAdministracijskuKomanduZabrana(komanda);
    }
    if (komanda.startsWith("DOZVOLA ")) {
      return this.obradiAdministracijskuKomanduDozvola(komanda);
    }
    if (komanda.equals("PING")) {
      return this.obradiAdministracijskuKomanduPing(komanda);
    }
    if (komanda.startsWith("KRAJ ")) {
      return this.obradiAdministracijskuKomanduKraj(komanda);
    }
    return ERROR_10;
  }

  /**
   * Obrađuje komandu KORISNIK DODAJ.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduKorisnikDodaj(String komanda) {
    var podudaranje = UZORAK_KORISNIK_DODAJ.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String ime = podudaranje.group(1);
    String prezime = podudaranje.group(2);
    String lozinka = podudaranje.group(3);
    String email = podudaranje.group(4);

    if (this.korisnici.containsKey(email)) {
      return ERROR_11;
    }

    var korisnik = new Korisnik(ime, prezime, lozinka, email);
    this.korisnici.put(email, korisnik);
    return "OK";
  }

  /**
   * Obrađuje komandu KORISNIK PROVJERI.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduKorisnikProvjeri(String komanda) {
    var podudaranje = UZORAK_KORISNIK_PROVJERI.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String email = podudaranje.group(1);

    if (this.korisnici.containsKey(email)) {
      return "OK";
    }
    return ERROR_12;
  }

  /**
   * Obrađuje komandu ULAZNICA DODAJ.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduUlaznicaDodaj(String komanda) {
    var podudaranje = UZORAK_ULAZNICA_DODAJ.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String email = podudaranje.group(1);
    String lozinka = podudaranje.group(2);

    Korisnik korisnik = this.korisnici.get(email);
    if (korisnik == null) {
      return ERROR_12;
    }

    if (!korisnik.lozinka().equals(lozinka)) {
      return ERROR_12;
    }

    if (this.korisnikImaVazecuUlaznicu(email)) {
      return ERROR_13;
    }

    String idUlaznice = this.generirajIdUlaznice(email);
    LocalDateTime vaziDo = LocalDateTime.now().plusNanos(this.trajanjeUlaznice * 1_000_000);

    Ulaznica ulaznica = new Ulaznica(idUlaznice, email, USLUGA_REZERVACIJE, vaziDo);
    this.ulaznice.put(idUlaznice, ulaznica);
    return "OK " + idUlaznice;
  }

  /**
   * Provjerava ima li korisnik važeću ulaznicu.
   *
   * @param email email korisnika
   * @return true ako ima važeću ulaznicu
   */
  public boolean korisnikImaVazecuUlaznicu(String email) {
    for (Ulaznica ulaznica : this.ulaznice.values()) {
      if (ulaznica.ispravnaUlaznica(email) && ulaznica.vazecaUlaznica(USLUGA_REZERVACIJE, 0)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generira id ulaznice.
   *
   * @param email email korisnika
   * @return id ulaznice
   */
  public String generirajIdUlaznice(String email) {
    try {
      String prviDioUlaza = email + System.nanoTime();
      String prviDio = Integer.toHexString(prviDioUlaza.hashCode());

      String drugiDioUlaza = InetAddress.getLocalHost() + "" + System.nanoTime();
      String drugiDio = Integer.toHexString(drugiDioUlaza.hashCode());

      return prviDio + drugiDio;
    } catch (Exception ex) {
      String prviDio = Integer.toHexString((email + System.nanoTime()).hashCode());
      String drugiDio = Integer.toHexString(("localhost" + System.nanoTime()).hashCode());
      return prviDio + drugiDio;
    }
  }

  /**
   * Obrađuje komandu ULAZNICA PROVJERI.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduUlaznicaProvjeri(String komanda) {
    var podudaranje = UZORAK_ULAZNICA_PROVJERI.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String id = podudaranje.group(1);
    Ulaznica ulaznica = this.ulaznice.get(id);

    if (ulaznica == null || !ulaznica.vazecaUlaznica(USLUGA_REZERVACIJE, 0)) {
      return ERROR_14;
    }

    Ulaznica produzenaUlaznica = ulaznica.produzi(this.trajanjeUlaznice);
    this.ulaznice.put(id, produzenaUlaznica);

    return "OK";
  }

  /**
   * Obrađuje komandu ULAZNICA KORISNIK.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduUlaznicaKorisnik(String komanda) {
    var podudaranje = UZORAK_ULAZNICA_KORISNIK.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String id = podudaranje.group(1);
    Ulaznica ulaznica = this.ulaznice.get(id);

    if (ulaznica == null || !ulaznica.vazecaUlaznica(USLUGA_REZERVACIJE, 0)) {
      return ERROR_14;
    }

    return "OK " + ulaznica.email();
  }

  /**
   * Obrađuje komandu ULAZNICA PONIŠTI.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduUlaznicaPonisti(String komanda) {
    var podudaranje = UZORAK_ULAZNICA_PONISTI.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String id = podudaranje.group(1);
    Ulaznica ulaznica = this.ulaznice.get(id);

    if (ulaznica == null || !ulaznica.vazecaUlaznica(USLUGA_REZERVACIJE, 0)) {
      return ERROR_14;
    }

    this.ulaznice.remove(id);
    return "OK";
  }

  /**
   * Obrađuje administracijsku komandu ULAZNICA PONIŠTI.
   *
   * @return odgovor
   */
  public String obradiAdministracijskuKomanduUlaznicaPonisti() {
    this.ulaznice.clear();
    return "OK";
  }

  /**
   * Obrađuje administracijsku komandu REGISTRIRAJ.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiAdministracijskuKomanduRegistriraj(String komanda) {
    var podudaranje = UZORAK_ADMIN_REGISTRIRAJ.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String adresa = podudaranje.group(1);
    int vrata;
    int vrataAdministracija;
    String kod;

    try {
      vrata = Integer.parseInt(podudaranje.group(2));
      vrataAdministracija = Integer.parseInt(podudaranje.group(3));
      kod = podudaranje.group(4);
    } catch (Exception e) {
      return ERROR_10;
    }

    if (!this.valjanaVrata(vrata) || !this.valjanaVrata(vrataAdministracija)) {
      return ERROR_10;
    }

    if (adresa.isBlank() || kod.isBlank()) {
      return ERROR_10;
    }

    PosluziteljPodaci podaci = new PosluziteljPodaci(adresa, vrata, vrataAdministracija, kod);

    String kljuc = this.kreirajKljucPosluzitelja(adresa, vrata, vrataAdministracija);
    this.registriraniPosluzitelji.put(kljuc, podaci);
    return "OK";
  }

  /**
   * Obrađuje administracijsku komandu ZABRANA.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiAdministracijskuKomanduZabrana(String komanda) {
    var podudaranje = UZORAK_ADMIN_ZABRANA.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String adresa = podudaranje.group(1);
    Set<String> varijante = this.dohvatiSveVarijanteAdrese(adresa);

    for (String varijanta : varijante) {
      if (this.zabranjeneAdrese.contains(varijanta)) {
        return ERROR_16_DODAJ;
      }
    }

    this.zabranjeneAdrese.addAll(varijante);
    return "OK";
  }

  /**
   * Dohvaća sve varijante adrese.
   *
   * @param adresa adresa
   * @return skup varijanti adrese
   */
  public Set<String> dohvatiSveVarijanteAdrese(String adresa) {
    Set<String> varijante = ConcurrentHashMap.newKeySet();

    if (adresa == null || adresa.isBlank()) {
      return varijante;
    }

    try {
      InetAddress inetAdresa = InetAddress.getByName(adresa);
      varijante.add(adresa);
      varijante.add(inetAdresa.getHostAddress());
      varijante.add(inetAdresa.getHostName());
      varijante.add(inetAdresa.getCanonicalHostName());
    } catch (Exception e) {
      varijante.add(adresa);
    }

    return varijante;
  }

  /**
   * Obrađuje administracijsku komandu DOZVOLA.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiAdministracijskuKomanduDozvola(String komanda) {
    var podudaranje = UZORAK_ADMIN_DOZVOLA.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String adresa = podudaranje.group(1);
    Set<String> varijante = this.dohvatiSveVarijanteAdrese(adresa);

    boolean postoji = false;
    for (String varijanta : varijante) {
      if (this.zabranjeneAdrese.contains(varijanta)) {
        postoji = true;
        break;
      }
    }

    if (!postoji) {
      return ERROR_16_BRISI;
    }

    this.zabranjeneAdrese.removeAll(varijante);
    return "OK";
  }

  /**
   * Obrađuje administracijsku komandu PING.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiAdministracijskuKomanduPing(String komanda) {
    if (!UZORAK_PING.matcher(komanda).matches()) {
      return ERROR_10;
    }
    return "OK";
  }

  /**
   * Obrađuje administracijsku komandu KRAJ.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiAdministracijskuKomanduKraj(String komanda) {
    var podudaranje = UZORAK_ADMIN_KRAJ.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    String primljeniKod = podudaranje.group(1);

    if (!this.kodZaKraj.equals(primljeniKod)) {
      return ERROR_18;
    }

    this.posaljiKrajNaRegistriranePosluzitelje();
    this.zahtjevZaGasenje = true;
    return "OK";
  }

  /**
   * Šalje komandu KRAJ na sve registrirane poslužitelje.
   */
  public void posaljiKrajNaRegistriranePosluzitelje() {
    for (PosluziteljPodaci podaci : this.registriraniPosluzitelji.values()) {
      String odgovor = this.posaljiKomanduKrajPosluzitelju(podaci);
      this.ispisiOdgovorKrajKomande(podaci, odgovor);

      if (!odgovor.startsWith("OK")) {
        String drugiOdgovor = this.posaljiKomanduKrajPosluzitelju(podaci);
        this.ispisiOdgovorKrajKomande(podaci, drugiOdgovor);
      }
    }
  }

  /**
   * Šalje komandu KRAJ pojedinom poslužitelju.
   *
   * @param podaci podaci poslužitelja
   * @return odgovor
   */
  public String posaljiKomanduKrajPosluzitelju(PosluziteljPodaci podaci) {
    try (var uticnica = new Socket(podaci.adresa(), podaci.mreznaVrataAdministracija());
        var izlaz = new PrintWriter(uticnica.getOutputStream(), true, StandardCharsets.UTF_8);
        var ulaz = new BufferedReader(
            new InputStreamReader(uticnica.getInputStream(), StandardCharsets.UTF_8))) {

      izlaz.println("KRAJ " + podaci.kodZaKraj());
      String odgovor = ulaz.readLine();
      if (odgovor == null || odgovor.isBlank()) {
        return ERROR_19;
      }
      return odgovor;
    } catch (Exception e) {
      return ERROR_19;
    }
  }

  /**
   * Ispisuje odgovor komande KRAJ.
   *
   * @param podaci podaci poslužitelja
   * @param odgovor odgovor
   */
  public void ispisiOdgovorKrajKomande(PosluziteljPodaci podaci, String odgovor) {
    System.err.println(podaci.adresa() + " " + podaci.mreznaVrataAdministracija() + " " + odgovor);
  }

  /**
   * Provjerava jesu li vrata valjana.
   *
   * @param vrata vrata
   * @return true ako su valjana
   */
  public boolean valjanaVrata(int vrata) {
    return vrata >= 1 && vrata <= 65535;
  }

  /**
   * Kreira ključ poslužitelja.
   *
   * @param adresa adresa
   * @param vrata vrata
   * @param vrataAdministracija administracijska vrata
   * @return ključ
   */
  public String kreirajKljucPosluzitelja(String adresa, int vrata, int vrataAdministracija) {
    return adresa + ":" + vrata + ":" + vrataAdministracija;
  }

  /**
   * Obrađuje komandu PAUZA.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduPauza(String komanda) {
    var podudaranje = UZORAK_PAUZA.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    long milisekunde;

    try {
      milisekunde = Long.parseLong(podudaranje.group(1));
    } catch (Exception e) {
      return ERROR_10;
    }

    try {
      Thread.sleep(milisekunde);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return ERROR_15;
    }

    return "OK";
  }

  /**
   * Obrađuje komandu PING.
   *
   * @param komanda komanda
   * @return odgovor
   */
  public String obradiKomanduPing(String komanda) {
    var podudaranje = UZORAK_PING.matcher(komanda);

    if (!podudaranje.matches()) {
      return ERROR_10;
    }

    return "OK";
  }

  /**
   * Zaustavlja prijem novih korisničkih komandi.
   */
  public void zaustaviPrijemNovihKorisnickihKomandi() {
    this.primaNoveKorisnickeKomande = false;
  }

  /**
   * Nastavlja prijem korisničkih komandi.
   */
  public void nastaviPrijemKorisnickihKomandi() {
    this.primaNoveKorisnickeKomande = true;
  }

  /**
   * Čeka da završe sve aktivne korisničke obrade.
   */
  public void pricekajAktivneKorisnickeObrade() {
    while (this.brojAktivnihKorisnickihObrada.get() > 0) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  /**
   * Dodaje shutdown hook.
   */
  public void dodajShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::zaustaviPosluzitelj));
  }

  /**
   * Zaustavlja poslužitelj.
   */
  public void zaustaviPosluzitelj() {
    this.radiKorisnickiDio = false;
    this.radiAdministracijskiDio = false;
    this.primaNoveKorisnickeKomande = false;
    this.zatvoriUticnice();
  }

  /**
   * Gasi izvršitelje.
   */
  public void ugasiIzvrsitelje() {
    if (this.executorKorisnickeObrade != null) {
      this.executorKorisnickeObrade.shutdownNow();
    }
    if (this.executorAdministracijskeObrade != null) {
      this.executorAdministracijskeObrade.shutdownNow();
    }
  }

  /**
   * Zatvara utičnice.
   */
  public void zatvoriUticnice() {
    this.zatvoriKorisnickuUticnicu();
    this.zatvoriAdministracijskuUticnicu();
  }

  /**
   * Zatvara korisničku utičnicu.
   */
  public void zatvoriKorisnickuUticnicu() {
    try {
      if (this.korisnickaUticnica != null && !this.korisnickaUticnica.isClosed()) {
        this.korisnickaUticnica.close();
      }
    } catch (IOException e) {
      // nema ispisa prema zadatku
    }
  }

  /**
   * Zatvara administracijsku utičnicu.
   */
  public void zatvoriAdministracijskuUticnicu() {
    try {
      if (this.administracijskaUticnica != null && !this.administracijskaUticnica.isClosed()) {
        this.administracijskaUticnica.close();
      }
    } catch (IOException e) {
      // nema ispisa prema zadatku
    }
  }

  public Map<String, Korisnik> dajKorisnike() {
    return this.korisnici;
  }


  public Set<String> dajZabranjeneAdrese() {
    return this.zabranjeneAdrese;
  }

  public Map<String, PosluziteljPodaci> dajRegistriranePosluzitelje() {
    return this.registriraniPosluzitelji;
  }


  public int dajMreznaVrata() {
    return this.mreznaVrata;
  }

  public int dajMreznaVrataAdministracija() {
    return this.mreznaVrataAdministracija;
  }

  public long dajPauzuDretve() {
    return this.pauzaDretve;
  }

  public int dajMaksBrojCekaca() {
    return this.maksBrojCekaca;
  }

  public int dajMaksBrojDretvi() {
    return this.maksBrojDretvi;
  }

  public long dajTrajanjeUlaznice() {
    return this.trajanjeUlaznice;
  }

  public String dajKodZaKraj() {
    return this.kodZaKraj;
  }

  public boolean dajRadiKorisnickiDio() {
    return this.radiKorisnickiDio;
  }

  public boolean dajRadiAdministracijskiDio() {
    return this.radiAdministracijskiDio;
  }

  public boolean dajPrimaNoveKorisnickeKomande() {
    return this.primaNoveKorisnickeKomande;
  }

  public int dajBrojAktivnihKorisnickihObrada() {
    return this.brojAktivnihKorisnickihObrada.get();
  }
}

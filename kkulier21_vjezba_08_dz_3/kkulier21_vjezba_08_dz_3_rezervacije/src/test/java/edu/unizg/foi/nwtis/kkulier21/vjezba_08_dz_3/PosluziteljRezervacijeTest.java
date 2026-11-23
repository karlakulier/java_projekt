package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.PosluziteljRezervacije;


class PosluziteljRezervacijeTest {

  private PosluziteljRezervacije pos;

  private AvioTvrtka avioTvrtka;
  private Avion avion;
  private LetPodaci letPodaci;
  private Let let;
  private RezervacijaSjedala slobodnoA1;
  private RezervacijaSjedala slobodnoB1;
  private RezervacijaSjedala kreiranaA1;

  @BeforeEach
  void setUp() throws Exception {
    this.pos = new PosluziteljRezervacije();
    this.inicijalizirajOsnovnePodatke(this.pos);
  }

  @AfterEach
  void tearDown() {
    this.pos = null;
  }

  private void inicijalizirajOsnovnePodatke(PosluziteljRezervacije cilj) throws Exception {
    this.avioTvrtka = new AvioTvrtka("OU", "Croatia Airlines", new ConcurrentHashMap<>());
    this.avion = new Avion("A1", "sjedala.csv", "Airbus A220", new ConcurrentHashMap<>());
    this.letPodaci =
        new LetPodaci("OU 4436", this.avioTvrtka, LocalTime.of(10, 15), "ZAG", "MUC", this.avion);
    this.let = Let.kreirajLet(this.letPodaci, LocalDate.of(2026, 5, 1));

    LetSjedalo sjedaloA1 = new LetSjedalo(this.let, RazredSjedala.POSLOVNI, 1, "A");
    LetSjedalo sjedaloB1 = new LetSjedalo(this.let, RazredSjedala.POSLOVNI, 1, "B");

    this.slobodnoA1 = RezervacijaSjedala.inicijalizirajRezervacijuSjedala(sjedaloA1,
        LocalDateTime.of(2026, 5, 1, 10, 15));
    this.slobodnoB1 = RezervacijaSjedala.inicijalizirajRezervacijuSjedala(sjedaloB1,
        LocalDateTime.of(2026, 5, 1, 10, 15));
    this.kreiranaA1 = this.slobodnoA1.kreirajRezervacijuSjedala("pero@foi.hr",
        LocalDateTime.of(2026, 4, 1, 8, 0));

    postaviPrivatnoPolje(cilj, "letovi", new ConcurrentHashMap<String, Let>());
    postaviPrivatnoPolje(cilj, "letoviPodaci", new ConcurrentHashMap<String, LetPodaci>());
    postaviPrivatnoPolje(cilj, "avioTvrtke", new ConcurrentHashMap<String, AvioTvrtka>());
    postaviPrivatnoPolje(cilj, "avioni", new ConcurrentHashMap<String, Avion>());
    postaviPrivatnoPolje(cilj, "rezervacijeSjedala",
        new ConcurrentHashMap<String, List<RezervacijaSjedala>>());
    postaviPrivatnoPolje(cilj, "rasporediRedovaPoLetu",
        new ConcurrentHashMap<String, Map<Integer, String>>());

    dohvatiMapu(cilj, "avioTvrtke").put(this.avioTvrtka.id(), this.avioTvrtka);
    dohvatiMapu(cilj, "avioni").put(this.avion.id(), this.avion);
    dohvatiMapu(cilj, "letoviPodaci").put(this.letPodaci.oznakaLeta(), this.letPodaci);
    dohvatiMapu(cilj, "letovi").put(this.let.id(), this.let);

    List<RezervacijaSjedala> lista = new ArrayList<>();
    lista.add(this.slobodnoA1);
    lista.add(this.slobodnoB1);
    dohvatiMapuLista(cilj, "rezervacijeSjedala").put(this.let.id(), lista);

    Map<Integer, String> raspored = new ConcurrentHashMap<>();
    raspored.put(1, "AB_CD");
    dohvatiMapuMapa(cilj, "rasporediRedovaPoLetu").put(this.let.id(), raspored);
  }

  private PosluziteljRezervacije kreirajPosluziteljSUvijekVazecomUlaznicom() throws Exception {
    PosluziteljRezervacije cilj = new PosluziteljRezervacije() {
      @Override
      public boolean provjeriUlaznicu(String idUlaznice) {
        return true;
      }
    };
    this.inicijalizirajOsnovnePodatke(cilj);
    return cilj;
  }

  @Test
  void testDohvatiInstancu() {
    assertNotNull(PosluziteljRezervacije.dohvatiInstancu());
  }

  @Test
  void testObradiKorisnickuKomanduNullIPrazno() {
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKorisnickuKomandu(null));
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKorisnickuKomandu(""));
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKorisnickuKomandu("   "));
  }

  @Test
  void testObradiKorisnickuKomanduNepoznata() {
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKorisnickuKomandu("NEPOZNATA"));
  }

  @Test
  void testObradiKorisnickuKomanduPing() {
    assertEquals(PosluziteljRezervacije.OK, this.pos.obradiKorisnickuKomandu("PING"));
  }

  @Test
  void testObradiKorisnickuKomanduPauza() {
    assertEquals(PosluziteljRezervacije.OK, this.pos.obradiKorisnickuKomandu("PAUZA 1"));
  }

  @Test
  void testObradiAvioKomanduNullIPrazno() {
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiAvioKomandu(null));
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiAvioKomandu(""));
  }

  @Test
  void testObradiAvioKomanduPing() {
    assertEquals(PosluziteljRezervacije.OK, this.pos.obradiAvioKomandu("PING"));
  }

  @Test
  void testObradiAvioKomanduNepoznata() {
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiAvioKomandu("NEPOZNATO"));
  }

  @Test
  void testObradiAdministracijskuKomanduNullIPrazno() {
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiAdministracijskuKomandu(null));
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiAdministracijskuKomandu(" "));
  }

  @Test
  void testObradiAdministracijskuKomanduPing() {
    assertEquals(PosluziteljRezervacije.OK, this.pos.obradiAdministracijskuKomandu("PING"));
  }

  @Test
  void testObradiAdministracijskuKomanduNepoznata() {
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiAdministracijskuKomandu("XYZ"));
  }

  @Test
  void testObradiKomanduPing() {
    assertEquals(PosluziteljRezervacije.OK, this.pos.obradiKomanduPing("PING"));
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKomanduPing("ping"));
  }

  @Test
  void testObradiKomanduPauza() {
    assertEquals(PosluziteljRezervacije.OK, this.pos.obradiKomanduPauza("PAUZA 1"));
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKomanduPauza("PAUZA x"));
    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKomanduPauza("PAUZA"));
  }

  @Test
  void testObradiKomanduRezervacijePonisti() throws Exception {
    String odgovor = this.pos.obradiKomanduRezervacijePonisti();
    assertEquals(PosluziteljRezervacije.OK, odgovor);
    assertNotNull(dohvatiMapuLista(this.pos, "rezervacijeSjedala"));
    assertNotNull(dohvatiMapuMapa(this.pos, "rasporediRedovaPoLetu"));
  }

  @Test
  void testObradiKomanduKraj() throws Exception {
    postaviPrivatnoPolje(this.pos, "kodZaKraj", "tajni");

    assertEquals(PosluziteljRezervacije.ERROR_20, this.pos.obradiKomanduKraj("KRAJ"));
    assertEquals(PosluziteljRezervacije.ERROR_28, this.pos.obradiKomanduKraj("KRAJ krivi"));
    assertEquals(PosluziteljRezervacije.OK, this.pos.obradiKomanduKraj("KRAJ tajni"));
  }

  @Test
  void testAzurirajRezervacijuZamijeniPostojecu() throws Exception {
    this.pos.azurirajRezervaciju(this.let.id(), this.kreiranaA1);

    RezervacijaSjedala rezultat =
        dohvatiMapuLista(this.pos, "rezervacijeSjedala").get(this.let.id()).get(0);

    assertEquals(StatusRezervacijeSjedala.KREIRANA, rezultat.statusRezervacije());
    assertEquals("pero@foi.hr", rezultat.putnik());
  }

  @Test
  void testAzurirajRezervacijuDodajNovu() throws Exception {
    RezervacijaSjedala nova =
        this.slobodnoB1.kreirajRezervacijuSjedala("ana@foi.hr", LocalDateTime.of(2026, 4, 1, 9, 0));

    List<RezervacijaSjedala> lista =
        dohvatiMapuLista(this.pos, "rezervacijeSjedala").get(this.let.id());
    lista.clear();
    lista.add(this.slobodnoA1);

    this.pos.azurirajRezervaciju(this.let.id(), nova);

    assertEquals(2, lista.size());
  }

  @Test
  void testOdgovaraAvioTvrtka() {
    assertTrue(this.pos.odgovaraAvioTvrtka(this.let, "OU"));
    assertFalse(this.pos.odgovaraAvioTvrtka(this.let, "LH"));
    assertFalse(this.pos.odgovaraAvioTvrtka(null, "OU"));
  }

  @Test
  void testZatvoriSjedalaURedovima() throws Exception {
    this.pos.zatvoriSjedalaURedovima(this.let.id(), 1, 1);

    List<RezervacijaSjedala> lista =
        dohvatiMapuLista(this.pos, "rezervacijeSjedala").get(this.let.id());

    assertEquals(StatusRezervacijeSjedala.ZATVORENA, lista.get(0).statusRezervacije());
    assertEquals(StatusRezervacijeSjedala.ZATVORENA, lista.get(1).statusRezervacije());
  }

  @Test
  void testZatvoriSvaSjedalaLeta() throws Exception {
    this.pos.zatvoriSvaSjedalaLeta(this.let.id());

    List<RezervacijaSjedala> lista =
        dohvatiMapuLista(this.pos, "rezervacijeSjedala").get(this.let.id());

    assertTrue(
        lista.stream().allMatch(r -> r.statusRezervacije() == StatusRezervacijeSjedala.ZATVORENA));
  }

  @Test
  void testPretvoriRezervacijeUJson() {
    List<RezervacijaSjedala> lista = List.of(this.kreiranaA1);
    String json = this.pos.pretvoriRezervacijeUJson(lista);

    assertTrue(json.startsWith("["));
    assertTrue(json.contains("\"putnik\":\"pero@foi.hr\""));
    assertTrue(json.contains("\"statusRezervacije\":\"KREIRANA\""));
  }

  @Test
  void testEscapeJson() {
    assertEquals("", this.pos.escapeJson(null));
    assertEquals("abc", this.pos.escapeJson("abc"));
    assertEquals("\\\"x\\\"", this.pos.escapeJson("\"x\""));
    assertEquals("\\\\", this.pos.escapeJson("\\"));
  }

  @Test
  void testPretvoriRazred() {
    assertEquals(RazredSjedala.POSLOVNI, this.pos.pretvoriRazred("P"));
    assertEquals(RazredSjedala.EKONOMSKI, this.pos.pretvoriRazred("E"));
    assertNull(this.pos.pretvoriRazred("X"));
    assertNull(this.pos.pretvoriRazred(null));
  }

  @Test
  void testPosaljiOdgovor() throws Exception {
    ByteArrayOutputStream izlaz = new ByteArrayOutputStream();
    this.pos.posaljiOdgovor(izlaz, "OK");
    assertEquals("OK\n", izlaz.toString(StandardCharsets.UTF_8));
  }

  @Test
  void testZaustaviINastaviPrijemNovihKomandi() throws Exception {
    this.pos.zaustaviPrijemNovihKomandi();

    assertFalse(dohvatiPrivatnoPolje(this.pos, "primaNoveKorisnickeKomande",
        java.util.concurrent.atomic.AtomicBoolean.class).get());
    assertFalse(dohvatiPrivatnoPolje(this.pos, "primaNoveAvioKomande",
        java.util.concurrent.atomic.AtomicBoolean.class).get());

    this.pos.nastaviPrijemNovihKomandi();

    assertTrue(dohvatiPrivatnoPolje(this.pos, "primaNoveKorisnickeKomande",
        java.util.concurrent.atomic.AtomicBoolean.class).get());
    assertTrue(dohvatiPrivatnoPolje(this.pos, "primaNoveAvioKomande",
        java.util.concurrent.atomic.AtomicBoolean.class).get());
  }

  @Test
  void testPricekajAktivneObrade() throws Exception {
    postaviPrivatnoPolje(this.pos, "pauzaDretve", 1);
    dohvatiPrivatnoPolje(this.pos, "aktivneKorisnickeObrade",
        java.util.concurrent.atomic.AtomicInteger.class).set(0);
    dohvatiPrivatnoPolje(this.pos, "aktivneAvioObrade",
        java.util.concurrent.atomic.AtomicInteger.class).set(0);
    this.pos.pricekajAktivneObrade();
    assertTrue(true);
  }

  @Test
  void testParsirajPutnikeGrupe() {
    assertTrue(this.pos.parsirajPutnikeGrupe(null).isEmpty());
    assertTrue(this.pos.parsirajPutnikeGrupe("   ").isEmpty());

    List<String> putnici = this.pos.parsirajPutnikeGrupe("a@a.hr   b@b.hr c@c.hr");
    assertEquals(3, putnici.size());
    assertEquals("a@a.hr", putnici.get(0));
    assertEquals("b@b.hr", putnici.get(1));
    assertEquals("c@c.hr", putnici.get(2));
  }

  @Test
  void testDohvatiRezervacijeLeta() {
    List<RezervacijaSjedala> lista = this.pos.dohvatiRezervacijeLeta(this.let.id());
    assertNotNull(lista);
    assertEquals(2, lista.size());
  }

  @Test
  void testDohvatiSlobodnaSjedala() {
    List<RezervacijaSjedala> rezultat =
        this.pos.dohvatiSlobodnaSjedala(this.let, RazredSjedala.POSLOVNI);
    assertEquals(2, rezultat.size());
  }

  @Test
  void testDohvatiSlobodnaSjedalaReda() {
    List<RezervacijaSjedala> rezultat =
        this.pos.dohvatiSlobodnaSjedalaReda(this.let, RazredSjedala.POSLOVNI, 1);
    assertEquals(2, rezultat.size());
  }

  @Test
  void testDohvatiSlobodnaSjedalaDijelaReda() {
    List<RezervacijaSjedala> rezultat =
        this.pos.dohvatiSlobodnaSjedalaDijelaReda(this.let, RazredSjedala.POSLOVNI, 1, "A");
    assertEquals(1, rezultat.size());
    assertEquals("A", rezultat.get(0).sjedalo().oznakaSjedala());
  }

  @Test
  void testDohvatiRedoveRazreda() {
    List<Integer> redovi = this.pos.dohvatiRedoveRazreda(this.let, RazredSjedala.POSLOVNI);
    assertEquals(1, redovi.size());
    assertEquals(1, redovi.get(0));
  }

  @Test
  void testDohvatiRasporedReda() {
    assertEquals("AB_CD", this.pos.dohvatiRasporedReda(this.let, 1));
    assertNull(this.pos.dohvatiRasporedReda(null, 1));
  }

  @Test
  void testDohvatiDijeloveReda() {
    List<String> dijelovi = this.pos.dohvatiDijeloveReda(this.let, 1);
    assertEquals(2, dijelovi.size());
    assertEquals("AB", dijelovi.get(0));
    assertEquals("CD", dijelovi.get(1));
  }

  @Test
  void testDohvatiNajveciBrojSjedalaUDijelu() {
    int najveci = this.pos.dohvatiNajveciBrojSjedalaUDijelu(this.let, RazredSjedala.POSLOVNI);
    assertEquals(2, najveci);
  }

  @Test
  void testKreirajRezervaciju() {
    RezervacijaSjedala r = this.pos.kreirajRezervaciju(this.slobodnoA1, "test@foi.hr");

    assertEquals(StatusRezervacijeSjedala.KREIRANA, r.statusRezervacije());
    assertEquals("test@foi.hr", r.putnik());
  }

  @Test
  void testPotvrdiPostojecuRezervaciju() {
    RezervacijaSjedala r = this.pos.potvrdiPostojecuRezervaciju(this.kreiranaA1);

    assertEquals(StatusRezervacijeSjedala.POTVRDENA, r.statusRezervacije());
  }

  @Test
  void testAzurirajRezervacijeGrupe() throws Exception {
    RezervacijaSjedala r1 =
        this.slobodnoA1.kreirajRezervacijuSjedala("a@a.hr", LocalDateTime.now());
    RezervacijaSjedala r2 =
        this.slobodnoB1.kreirajRezervacijuSjedala("b@b.hr", LocalDateTime.now());

    this.pos.azurirajRezervacijeGrupe(this.let.id(), List.of(r1, r2));

    List<RezervacijaSjedala> lista =
        dohvatiMapuLista(this.pos, "rezervacijeSjedala").get(this.let.id());

    assertEquals(StatusRezervacijeSjedala.KREIRANA, lista.get(0).statusRezervacije());
    assertEquals(StatusRezervacijeSjedala.KREIRANA, lista.get(1).statusRezervacije());
  }

  @Test
  void testVratiRezervacijuNaSlobodna() throws Exception {
    this.pos.azurirajRezervaciju(this.let.id(), this.kreiranaA1);
    this.pos.vratiRezervacijuNaSlobodna(this.let.id(), this.kreiranaA1);

    RezervacijaSjedala r =
        dohvatiMapuLista(this.pos, "rezervacijeSjedala").get(this.let.id()).get(0);

    assertEquals(StatusRezervacijeSjedala.SLOBODNA, r.statusRezervacije());
    assertNull(r.putnik());
  }

  @Test
  void testVratiRezervacijuNaSlobodnaNullVrijednosti() {
    this.pos.vratiRezervacijuNaSlobodna(null, this.kreiranaA1);
    this.pos.vratiRezervacijuNaSlobodna(this.let.id(), null);
    assertTrue(true);
  }

  @Test
  void testVratiRezervacijeGrupeNaSlobodna() throws Exception {
    RezervacijaSjedala r1 =
        this.slobodnoA1.kreirajRezervacijuSjedala("a@a.hr", LocalDateTime.now());
    RezervacijaSjedala r2 =
        this.slobodnoB1.kreirajRezervacijuSjedala("b@b.hr", LocalDateTime.now());

    this.pos.azurirajRezervacijeGrupe(this.let.id(), List.of(r1, r2));
    this.pos.vratiRezervacijeGrupeNaSlobodna(this.let.id(), List.of(r1, r2));

    List<RezervacijaSjedala> lista =
        dohvatiMapuLista(this.pos, "rezervacijeSjedala").get(this.let.id());

    assertTrue(
        lista.stream().allMatch(r -> r.statusRezervacije() == StatusRezervacijeSjedala.SLOBODNA));
  }

  @Test
  void testVratiRezervacijeGrupeNaSlobodnaPrazno() {
    this.pos.vratiRezervacijeGrupeNaSlobodna(this.let.id(), List.of());
    assertTrue(true);
  }

  @Test
  void testRazdvojiCsvLiniju() {
    List<String> polja = this.pos.razdvojiCsvLiniju("A,B,\"C,D\",E");
    assertEquals(4, polja.size());
    assertEquals("A", polja.get(0));
    assertEquals("B", polja.get(1));
    assertEquals("C,D", polja.get(2));
    assertEquals("E", polja.get(3));
  }

  @Test
  void testIspraviPoljaAviona() {
    List<String> polja = List.of("A1", "sjedala.csv Airbus A220", "doseg:1000");
    List<String> ispravljena = this.pos.ispraviPoljaAviona(polja);

    assertEquals(4, ispravljena.size());
    assertEquals("A1", ispravljena.get(0));
    assertEquals("sjedala.csv", ispravljena.get(1));
  }

  @Test
  void testIspraviPoljaAvionaBezPromjene() {
    List<String> polja = List.of("A1", "sjedala.csv", "Airbus A220", "doseg:1000");
    List<String> ispravljena = this.pos.ispraviPoljaAviona(polja);
    assertEquals(4, ispravljena.size());
  }

  @Test
  void testIspraviPoljaLeta() {
    List<String> polja = List.of("OU 4436", "OU", "ZAG", "MUC", "0123456 10:15", "A1");
    List<String> ispravljena = this.pos.ispraviPoljaLeta(polja);

    assertEquals(7, ispravljena.size());
    assertEquals("0123456", ispravljena.get(4));
    assertEquals("10:15", ispravljena.get(5));
  }

  @Test
  void testIspraviPoljaLetaBezPromjene() {
    List<String> polja = List.of("OU 4436", "OU", "ZAG", "MUC", "0123456", "10:15", "A1");
    List<String> ispravljena = this.pos.ispraviPoljaLeta(polja);
    assertEquals(7, ispravljena.size());
  }

  @Test
  void testParsirajSpecifikacijeAviona() {
    Map<String, String> mapa = this.pos.parsirajSpecifikacijeAviona("\"doseg:1000, brzina:900\"");

    assertEquals(2, mapa.size());
    assertEquals("1000", mapa.get("doseg"));
    assertEquals("900", mapa.get("brzina"));
  }

  @Test
  void testParsirajSpecifikacijeAvionaPrazno() {
    Map<String, String> mapa = this.pos.parsirajSpecifikacijeAviona("");
    assertTrue(mapa.isEmpty());
  }

  @Test
  void testParsirajOznakeSjedala() {
    List<String> oznake = this.pos.parsirajOznakeSjedala("AB_CD-E");
    assertEquals(5, oznake.size());
    assertEquals("A", oznake.get(0));
    assertEquals("E", oznake.get(4));
  }

  @Test
  void testParsirajOznakeSjedalaPrazno() {
    List<String> oznake = this.pos.parsirajOznakeSjedala("");
    assertTrue(oznake.isEmpty());
  }

  @Test
  void testUcitajLinijeDatoteke(@TempDir Path tempDir) throws IOException {
    Path dat = tempDir.resolve("test.txt");
    Files.write(dat, List.of("a", "b"), StandardCharsets.UTF_8);

    List<String> linije = this.pos.ucitajLinijeDatoteke(dat.toString());
    assertEquals(2, linije.size());

    List<String> prazno =
        this.pos.ucitajLinijeDatoteke(tempDir.resolve("ne-postoji.txt").toString());
    assertTrue(prazno.isEmpty());
  }

  @Test
  void testInicijalizirajLetZaDatum() throws Exception {
    int prije = dohvatiMapu(this.pos, "letovi").size();
    LetPodaci novi =
        new LetPodaci("OU 9999", this.avioTvrtka, LocalTime.of(12, 0), "ZAG", "SPU", this.avion);

    this.pos.inicijalizirajLetZaDatum(novi, LocalDate.of(2026, 5, 2));

    assertEquals(prije + 1, dohvatiMapu(this.pos, "letovi").size());
  }

  @Test
  void testInicijalizirajLetZaDatumNull() throws Exception {
    int prije = dohvatiMapu(this.pos, "letovi").size();
    this.pos.inicijalizirajLetZaDatum(null, LocalDate.now());
    this.pos.inicijalizirajLetZaDatum(this.letPodaci, null);
    assertEquals(prije, dohvatiMapu(this.pos, "letovi").size());
  }

  @Test
  void testUcitajAvioTvrtke(@TempDir Path tempDir) throws Exception {
    Path dat = tempDir.resolve("avio.csv");
    Files.write(dat, List.of("id,naziv", "OU,Croatia Airlines", "LH,Lufthansa", "OU,Duplikat"),
        StandardCharsets.UTF_8);

    postaviPrivatnoPolje(this.pos, "datotekaAvioTvrtke", dat.toString());
    dohvatiMapu(this.pos, "avioTvrtke").clear();

    this.pos.ucitajAvioTvrtke();

    assertEquals(2, dohvatiMapu(this.pos, "avioTvrtke").size());
  }

  @Test
  void testUcitajAvione(@TempDir Path tempDir) throws Exception {
    Path dat = tempDir.resolve("avioni.csv");
    Files.write(dat,
        List.of("id,datoteka,naziv,spec", "A1,sjedala.csv,Airbus A220,\"doseg:1000, brzina:900\""),
        StandardCharsets.UTF_8);

    postaviPrivatnoPolje(this.pos, "datotekaAvioni", dat.toString());
    dohvatiMapu(this.pos, "avioni").clear();

    this.pos.ucitajAvione();

    assertEquals(1, dohvatiMapu(this.pos, "avioni").size());
  }

  @Test
  void testUcitajLetovePodaci(@TempDir Path tempDir) throws Exception {
    Path dat = tempDir.resolve("letovi.csv");
    Files.write(dat, List.of("oznaka,avio,polazni,odredisni,dani,vrijeme,avion",
        "OU 4436,OU,ZAG,MUC,0123456,10:15,A1"), StandardCharsets.UTF_8);

    postaviPrivatnoPolje(this.pos, "datotekaLetovi", dat.toString());
    dohvatiMapu(this.pos, "letoviPodaci").clear();

    this.pos.ucitajLetovePodaci();

    assertEquals(1, dohvatiMapu(this.pos, "letoviPodaci").size());
  }

  @Test
  void testInicijalizirajLetove() throws Exception {
    postaviPrivatnoPolje(this.pos, "datumOd", LocalDate.of(2026, 5, 1));
    postaviPrivatnoPolje(this.pos, "datumDo", LocalDate.of(2026, 5, 3));
    dohvatiMapu(this.pos, "letovi").clear();

    this.pos.inicijalizirajLetove();

    assertEquals(3, dohvatiMapu(this.pos, "letovi").size());
  }

  @Test
  void testInicijalizirajRezervacijeSjedala() throws Exception {
    dohvatiMapuLista(this.pos, "rezervacijeSjedala").clear();
    dohvatiMapuMapa(this.pos, "rasporediRedovaPoLetu").clear();

    this.pos.inicijalizirajRezervacijeSjedala();

    assertNotNull(dohvatiMapuLista(this.pos, "rezervacijeSjedala"));
    assertNotNull(dohvatiMapuMapa(this.pos, "rasporediRedovaPoLetu"));
  }



  @Test
  void testUcitajPodatke() {
    this.pos.ucitajPodatke();
    assertTrue(true);
  }

  @Test
  void testUcitajOdredivanjeSjedalaPrazno() throws Exception {
    postaviPrivatnoPolje(this.pos, "odredivanjeSjedalaKlasa", "");
    this.pos.ucitajOdredivanjeSjedala();
    assertNull(dohvatiPrivatnoPolje(this.pos, "odredivanjeSjedala", OdredivanjeSjedala.class));
  }

  @Test
  void testUcitajOdredivanjeSjedalaNeispravnaKlasa() throws Exception {
    postaviPrivatnoPolje(this.pos, "odredivanjeSjedalaKlasa", "ne.postoji.Klasa");
    this.pos.ucitajOdredivanjeSjedala();
    assertNull(dohvatiPrivatnoPolje(this.pos, "odredivanjeSjedala", OdredivanjeSjedala.class));
  }

  @Test
  void testProvjeriUlaznicuBezServeraVracaFalse() throws Exception {
    postaviPrivatnoPolje(this.pos, "adresaKontrola", "localhost");
    postaviPrivatnoPolje(this.pos, "mreznaVrataKontrola", 6553);

    assertFalse(this.pos.provjeriUlaznicu("abc"));
  }

  @Test
  void testProvjeriUlaznicuSMinimalnimLokalnimServerom() throws Exception {
    try (ServerSocket server = new ServerSocket(0)) {
      int vrata = server.getLocalPort();

      Thread dretva = new Thread(() -> {
        try (Socket s = server.accept()) {
          s.getOutputStream().write("OK\n".getBytes(StandardCharsets.UTF_8));
          s.getOutputStream().flush();
        } catch (Exception e) {

        }
      });
      dretva.start();

      postaviPrivatnoPolje(this.pos, "adresaKontrola", "localhost");
      postaviPrivatnoPolje(this.pos, "mreznaVrataKontrola", vrata);

      assertTrue(this.pos.provjeriUlaznicu("ulaznica"));
      dretva.join();
    }
  }

  @Test
  void testPronadiRezervacijuPoId() {
    RezervacijaSjedala r = this.pos.pronadiRezervacijuPoId(this.slobodnoA1.id());
    assertNotNull(r);
    assertNull(this.pos.pronadiRezervacijuPoId("ne-postoji"));
  }

  @Test
  void testObradiKomanduRezervirajNeispravnaUlaznica() {
    String odgovor =
        this.pos.obradiKomanduRezerviraj("REZERVIRAJ x " + this.let.id() + " pero@foi.hr P");
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduRezervirajNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduRezerviraj("REZERVIRAJ");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduPotvrdiNeispravnaUlaznica() {
    String odgovor = this.pos.obradiKomanduPotvrdi("POTVRDI x rez123 pero@foi.hr");
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduPotvrdiNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduPotvrdi("POTVRDI");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduRezervirajGrupaNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduRezervirajGrupa("REZERVIRAJ GRUPA x y [] P");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisOznakaDatumNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduAvioIspisOznakaDatum("ISPIS");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisOznakaDatumNeispravnaUlaznica() {
    String odgovor = this.pos.obradiKomanduAvioIspisOznakaDatum("ISPIS x \"OU 4436\" 2026-05-01");
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisOznakaDatumUspjeh() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioIspisOznakaDatum("ISPIS x \"OU 4436\" 2026-05-01");
    assertEquals(PosluziteljRezervacije.OK + " " + this.let.id(), odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisOznakaDatumNePostojePodaci() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioIspisOznakaDatum("ISPIS x \"OU 9999\" 2026-05-01");
    assertEquals(PosluziteljRezervacije.ERROR_27, odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduAvioIspis("ISPIS");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisNeispravnaUlaznica() {
    String odgovor = this.pos.obradiKomanduAvioIspis("ISPIS x " + this.let.id());
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisNePostojiLet() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioIspis("ISPIS x nepostoji");
    assertEquals(PosluziteljRezervacije.ERROR_22, odgovor);
  }

  @Test
  void testObradiKomanduAvioIspisUspjeh() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioIspis("ISPIS x " + this.let.id());
    assertTrue(odgovor.startsWith(PosluziteljRezervacije.OK + " ["));
  }

  @Test
  void testObradiKomanduAvioZatvoriRedoveNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduAvioZatvoriRedove("ZATVORI");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriRedoveOdVeciOdDo() {
    String odgovor =
        this.pos.obradiKomanduAvioZatvoriRedove("ZATVORI x OU " + this.let.id() + " 5 1");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriRedoveNeispravnaUlaznica() {
    String odgovor =
        this.pos.obradiKomanduAvioZatvoriRedove("ZATVORI x OU " + this.let.id() + " 1 1");
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriRedoveNePostojiLetIliTvrtka() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioZatvoriRedove("ZATVORI x LH " + this.let.id() + " 1 1");
    assertEquals(PosluziteljRezervacije.ERROR_22, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriRedoveZauzetaSjedala() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    cilj.azurirajRezervaciju(this.let.id(), this.kreiranaA1);

    String odgovor = cilj.obradiKomanduAvioZatvoriRedove("ZATVORI x OU " + this.let.id() + " 1 1");
    assertEquals(PosluziteljRezervacije.ERROR_26, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriRedoveUspjeh() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioZatvoriRedove("ZATVORI x OU " + this.let.id() + " 1 1");
    assertEquals(PosluziteljRezervacije.OK, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriDatumeNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduAvioZatvoriDatume("ZATVORI");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriDatumeNeispravanInterval() {
    String odgovor = this.pos.obradiKomanduAvioZatvoriDatume("ZATVORI x OU 2026-06-01 2026-05-01");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriDatumeNeispravnaUlaznica() {
    String odgovor = this.pos.obradiKomanduAvioZatvoriDatume("ZATVORI x OU 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduAvioZatvoriDatumeUspjeh() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioZatvoriDatume("ZATVORI x OU 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.OK, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajLetNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduAvioInicijalizirajLet("INICIJALIZIRAJ");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajLetNeispravanInterval() {
    String odgovor = this.pos.obradiKomanduAvioInicijalizirajLet(
        "INICIJALIZIRAJ x OU \"OU 4436\" 2026-06-01 2026-05-01");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajLetNeispravnaUlaznica() {
    String odgovor = this.pos.obradiKomanduAvioInicijalizirajLet(
        "INICIJALIZIRAJ x OU \"OU 4436\" 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajLetNePostojePodaciLeta() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioInicijalizirajLet(
        "INICIJALIZIRAJ x OU \"OU 9999\" 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.ERROR_27, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajLetUspjeh() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor = cilj.obradiKomanduAvioInicijalizirajLet(
        "INICIJALIZIRAJ x OU \"OU 4436\" 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.OK, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajTvrtkuNeispravanFormat() {
    String odgovor = this.pos.obradiKomanduAvioInicijalizirajTvrtku("INICIJALIZIRAJ");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajTvrtkuNeispravanInterval() {
    String odgovor =
        this.pos.obradiKomanduAvioInicijalizirajTvrtku("INICIJALIZIRAJ x OU 2026-06-01 2026-05-01");
    assertEquals(PosluziteljRezervacije.ERROR_20, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajTvrtkuNeispravnaUlaznica() {
    String odgovor =
        this.pos.obradiKomanduAvioInicijalizirajTvrtku("INICIJALIZIRAJ x OU 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.ERROR_21, odgovor);
  }

  @Test
  void testObradiKomanduAvioInicijalizirajTvrtkuUspjeh() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();
    String odgovor =
        cilj.obradiKomanduAvioInicijalizirajTvrtku("INICIJALIZIRAJ x OU 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.OK, odgovor);
  }

  @Test
  void testObradiAvioKomanduZaPojedineKomande() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();

    String odgovor1 =
        cilj.obradiAvioKomandu("ISPIS x \"" + this.letPodaci.oznakaLeta() + "\" 2026-05-01");
    assertTrue(odgovor1.startsWith("OK ") || odgovor1.startsWith("ERROR"));

    String odgovor2 = cilj.obradiAvioKomandu("ISPIS x " + this.let.id());
    assertTrue(odgovor2.startsWith("OK ") || odgovor2.startsWith("ERROR"));

    String odgovor3 = cilj.obradiAvioKomandu("ZATVORI x OU 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.OK, odgovor3);

    String odgovor4 =
        cilj.obradiAvioKomandu("INICIJALIZIRAJ x OU \"OU 4436\" 2026-05-01 2026-05-02");
    assertEquals(PosluziteljRezervacije.OK, odgovor4);
  }

  @Test
  void testObradiAdministracijskuKomanduPonisti() {
    String odgovor = this.pos.obradiAdministracijskuKomandu("REZERVACIJE PONIŠTI");
    assertEquals(PosluziteljRezervacije.OK, odgovor);
  }

  @Test
  void testUcitajMreznaVrataAvioKompanijeIzPrimarnePostavke() throws Exception {
    postaviPrivatnoPolje(this.pos, "konfig", new KonfiguracijaTest(
        Map.of("mreznaVrataAvioKompanije", "9001", "mreznaVrataAvioTvrtke", "9002")));
    assertEquals(9001, this.pos.ucitajMreznaVrataAvioKompanije());
  }

  @Test
  void testUcitajMreznaVrataAvioKompanijeIzRezervnePostavke() throws Exception {
    postaviPrivatnoPolje(this.pos, "konfig",
        new KonfiguracijaTest(Map.of("mreznaVrataAvioTvrtke", "9002")));
    assertEquals(9002, this.pos.ucitajMreznaVrataAvioKompanije());
  }

  @Test
  void testObradiKorisnici() throws Exception {
    try (ServerSocket server = new ServerSocket(0)) {
      int vrata = server.getLocalPort();

      Thread dretva = new Thread(() -> {
        try (Socket s = server.accept()) {
          this.pos.obradiKorisnici(s);
        } catch (Exception e) {
          // ignoriraj
        }
      });
      dretva.start();

      try (Socket klijent = new Socket("localhost", vrata)) {
        klijent.getOutputStream().write("PING\n".getBytes(StandardCharsets.UTF_8));
        klijent.getOutputStream().flush();
        byte[] buffer = klijent.getInputStream().readNBytes(16);
        String odgovor = new String(buffer, StandardCharsets.UTF_8).trim();
        assertEquals("OK", odgovor);
      }

      dretva.join();
    }
  }

  @Test
  void testObradiAvioTvrtke() throws Exception {
    PosluziteljRezervacije cilj = kreirajPosluziteljSUvijekVazecomUlaznicom();

    try (ServerSocket server = new ServerSocket(0)) {
      int vrata = server.getLocalPort();

      Thread dretva = new Thread(() -> {
        try (Socket s = server.accept()) {
          cilj.obradiAvioTvrtke(s);
        } catch (Exception e) {

        }
      });
      dretva.start();

      try (Socket klijent = new Socket("localhost", vrata)) {
        klijent.getOutputStream().write("PING\n".getBytes(StandardCharsets.UTF_8));
        klijent.getOutputStream().flush();
        byte[] buffer = klijent.getInputStream().readNBytes(16);
        String odgovor = new String(buffer, StandardCharsets.UTF_8).trim();
        assertEquals("OK", odgovor);
      }

      dretva.join();
    }
  }

  @Test
  void testObradiAdministracija() throws Exception {
    postaviPrivatnoPolje(this.pos, "kodZaKraj", "tajni");
    postaviPrivatnoPolje(this.pos, "pauzaDretve", 1);

    try (ServerSocket server = new ServerSocket(0)) {
      int vrata = server.getLocalPort();

      Thread dretva = new Thread(() -> {
        try (Socket s = server.accept()) {
          this.pos.obradiAdministracija(s);
        } catch (Exception e) {

        }
      });
      dretva.start();

      try (Socket klijent = new Socket("localhost", vrata)) {
        klijent.getOutputStream().write("PING\n".getBytes(StandardCharsets.UTF_8));
        klijent.getOutputStream().flush();
        byte[] buffer = klijent.getInputStream().readNBytes(16);
        String odgovor = new String(buffer, StandardCharsets.UTF_8).trim();
        assertEquals("OK", odgovor);
      }

      dretva.join();
    }
  }

  @Test
  void testZatvoriUticnice() throws Exception {
    ServerSocket k = new ServerSocket(0);
    ServerSocket a = new ServerSocket(0);
    ServerSocket ad = new ServerSocket(0);

    postaviPrivatnoPolje(this.pos, "korisnickaUticnica", k);
    postaviPrivatnoPolje(this.pos, "avioUticnica", a);
    postaviPrivatnoPolje(this.pos, "administracijskaUticnica", ad);

    this.pos.zatvoriUticnice();

    assertTrue(k.isClosed());
    assertTrue(a.isClosed());
    assertTrue(ad.isClosed());
  }

  private void postaviPrivatnoPolje(PosluziteljRezervacije cilj, String nazivPolja,
      Object vrijednost) throws Exception {
    Field polje = PosluziteljRezervacije.class.getDeclaredField(nazivPolja);
    polje.setAccessible(true);
    polje.set(cilj, vrijednost);
  }

  @SuppressWarnings("unchecked")
  private <T> T dohvatiPrivatnoPolje(PosluziteljRezervacije cilj, String nazivPolja, Class<T> tip)
      throws Exception {
    Field polje = PosluziteljRezervacije.class.getDeclaredField(nazivPolja);
    polje.setAccessible(true);
    return (T) polje.get(cilj);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> dohvatiMapu(PosluziteljRezervacije cilj, String nazivPolja)
      throws Exception {
    Field polje = PosluziteljRezervacije.class.getDeclaredField(nazivPolja);
    polje.setAccessible(true);
    return (Map<String, Object>) polje.get(cilj);
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<RezervacijaSjedala>> dohvatiMapuLista(PosluziteljRezervacije cilj,
      String nazivPolja) throws Exception {
    Field polje = PosluziteljRezervacije.class.getDeclaredField(nazivPolja);
    polje.setAccessible(true);
    return (Map<String, List<RezervacijaSjedala>>) polje.get(cilj);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<Integer, String>> dohvatiMapuMapa(PosluziteljRezervacije cilj,
      String nazivPolja) throws Exception {
    Field polje = PosluziteljRezervacije.class.getDeclaredField(nazivPolja);
    polje.setAccessible(true);
    return (Map<String, Map<Integer, String>>) polje.get(cilj);
  }

  private static class KonfiguracijaTest
      implements edu.unizg.foi.nwtis.konfiguracije.Konfiguracija {

    private final java.util.Properties podaci = new java.util.Properties();

    KonfiguracijaTest(Map<String, String> pocetniPodaci) {
      if (pocetniPodaci != null) {
        for (Map.Entry<String, String> zapis : pocetniPodaci.entrySet()) {
          this.podaci.setProperty(zapis.getKey(), zapis.getValue());
        }
      }
    }

    @Override
    public void ucitajKonfiguraciju() {

    }

    @Override
    public void spremiKonfiguraciju() {

    }

    @Override
    public void spremiKonfiguraciju(String datoteka) {

    }

    @Override
    public java.util.Properties dajSvePostavke() {
      java.util.Properties kopija = new java.util.Properties();
      kopija.putAll(this.podaci);
      return kopija;
    }

    @Override
    public boolean obrisiSvePostavke() {
      boolean nijePrazno = !this.podaci.isEmpty();
      this.podaci.clear();
      return nijePrazno;
    }

    @Override
    public String dajPostavku(String kljuc) {
      return this.podaci.getProperty(kljuc);
    }

    @Override
    public String dajPostavkuOsnovno(String kljuc, String osnovnaVrijednost) {
      return this.podaci.getProperty(kljuc, osnovnaVrijednost);
    }

    @Override
    public boolean spremiPostavku(String kljuc, String vrijednost) {
      if (this.podaci.containsKey(kljuc)) {
        return false;
      }
      this.podaci.setProperty(kljuc, vrijednost);
      return true;
    }

    @Override
    public boolean azurirajPostavku(String kljuc, String vrijednost) {
      if (!this.podaci.containsKey(kljuc)) {
        return false;
      }
      this.podaci.setProperty(kljuc, vrijednost);
      return true;
    }

    @Override
    public boolean postojiPostavka(String kljuc) {
      return this.podaci.containsKey(kljuc);
    }

    @Override
    public boolean obrisiPostavku(String kljuc) {
      if (!this.podaci.containsKey(kljuc)) {
        return false;
      }
      this.podaci.remove(kljuc);
      return true;
    }
  }
  public static class TestniAlgoritam implements OdredivanjeSjedala {

    @Override
    public boolean inicijaliziraj() {
      return true;
    }

    @Override
    public RezervacijaSjedala rezervirajSjedalo(Let let, String putnik, RazredSjedala razred)
        throws ProblemKodRezervacije {
      LetSjedalo sjedalo = new LetSjedalo(let, razred, 1, "A");
      return RezervacijaSjedala.inicijalizirajRezervacijuSjedala(sjedalo, LocalDateTime.now())
          .kreirajRezervacijuSjedala(putnik, LocalDateTime.now());
    }

    @Override
    public RezervacijaSjedala potvrdiRezervacijuSjedala(RezervacijaSjedala rezervacija)
        throws ProblemKodRezervacije {
      return rezervacija.potvrdiRezervacijuSjedala();
    }

    @Override
    public List<RezervacijaSjedala> rezervirajSjedalaZaGrupu(Let let, List<String> putnici,
        RazredSjedala razred) throws ProblemKodRezervacije {
      List<RezervacijaSjedala> rezultat = new ArrayList<>();
      int oznaka = 0;
      for (String putnik : putnici) {
        LetSjedalo sjedalo = new LetSjedalo(let, razred, 1, String.valueOf((char) ('A' + oznaka)));
        rezultat
            .add(RezervacijaSjedala.inicijalizirajRezervacijuSjedala(sjedalo, LocalDateTime.now())
                .kreirajRezervacijuSjedala(putnik, LocalDateTime.now()));
        oznaka++;
      }
      return rezultat;
    }

    @Override
    public List<RezervacijaSjedala> potvrdiRezervacijuSjedalaGrupe(
        List<RezervacijaSjedala> rezervacije) throws ProblemKodRezervacije {
      return rezervacije;
    }
  }
}

package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.List;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.OdredivanjeSjedala;
import edu.unizg.foi.nwtis.ProblemKodRezervacije;
import edu.unizg.foi.nwtis.RazredSjedala;
import edu.unizg.foi.nwtis.RezervacijaSjedala;

public class PopuniRavnomjernoOdPocetka implements OdredivanjeSjedala {

  @Override
  public boolean inicijaliziraj() {
    return true;
  }

  @Override
  public RezervacijaSjedala rezervirajSjedalo(Let let, String putnik,
      RazredSjedala razred) throws ProblemKodRezervacije {
    PosluziteljRezervacije posluzitelj =
        PosluziteljRezervacije.dohvatiInstancu();

    if (posluzitelj == null || let == null || putnik == null || razred == null) {
      throw new ProblemKodRezervacije();
    }

    List<Integer> redovi = posluzitelj.dohvatiRedoveRazreda(let, razred);

    for (Integer red : redovi) {
      List<RezervacijaSjedala> slobodna =
          posluzitelj.dohvatiSlobodnaSjedalaReda(let, razred, red);

      if (slobodna.isEmpty()) {
        continue;
      }

      RezervacijaSjedala odabrano =
          this.odaberiLijevoDesno(let, red, slobodna);

      if (odabrano != null) {
        return posluzitelj.kreirajRezervaciju(odabrano, putnik);
      }
    }

    throw new ProblemKodRezervacije();
  }

  @Override
  public RezervacijaSjedala potvrdiRezervacijuSjedala(
      RezervacijaSjedala rezervacija) throws ProblemKodRezervacije {
    if (rezervacija == null) {
      throw new ProblemKodRezervacije();
    }

    return rezervacija.potvrdiRezervacijuSjedala();
  }

  @Override
  public List<RezervacijaSjedala> rezervirajSjedalaZaGrupu(
      Let let, List<String> putnici, RazredSjedala razred)
      throws ProblemKodRezervacije {
    PosluziteljRezervacije posluzitelj =
        PosluziteljRezervacije.dohvatiInstancu();

    if (posluzitelj == null || let == null || putnici == null
        || putnici.isEmpty() || razred == null) {
      throw new ProblemKodRezervacije();
    }

    int maxUDijelu = posluzitelj.dohvatiNajveciBrojSjedalaUDijelu(let, razred);
    List<Integer> redovi = posluzitelj.dohvatiRedoveRazreda(let, razred);
    List<RezervacijaSjedala> rezultat = new ArrayList<>();

    try {
      if (putnici.size() <= maxUDijelu) {
        for (Integer red : redovi) {
          List<String> dijelovi = posluzitelj.dohvatiDijeloveReda(let, red);

          for (String dio : dijelovi) {
            List<RezervacijaSjedala> slobodna =
                posluzitelj.dohvatiSlobodnaSjedalaDijelaReda(
                    let, razred, red, dio);

            if (slobodna.size() >= putnici.size()) {
              List<RezervacijaSjedala> sortirana =
                  this.sortirajLijevoDesno(let, red, slobodna);

              for (int i = 0; i < putnici.size(); i++) {
                rezultat.add(posluzitelj.kreirajRezervaciju(
                    sortirana.get(i), putnici.get(i)));
              }
              return rezultat;
            }
          }
        }
      }

      int indeksPutnika = 0;

      for (String dioKandidat :
          this.dohvatiDijeloveSNajviseSjedala(let, razred)) {
        for (Integer red : redovi) {
          if (indeksPutnika >= putnici.size()) {
            break;
          }

          List<String> dijelovi = posluzitelj.dohvatiDijeloveReda(let, red);
          String odabraniDio = null;

          for (String dio : dijelovi) {
            if (dio.length() == dioKandidat.length()) {
              odabraniDio = dio;
              break;
            }
          }

          if (odabraniDio == null) {
            continue;
          }

          List<RezervacijaSjedala> slobodna =
              posluzitelj.dohvatiSlobodnaSjedalaDijelaReda(
                  let, razred, red, odabraniDio);

          if (slobodna.isEmpty()) {
            continue;
          }

          List<RezervacijaSjedala> sortirana =
              this.sortirajLijevoDesno(let, red, slobodna);

          for (RezervacijaSjedala sjedalo : sortirana) {
            if (indeksPutnika >= putnici.size()) {
              break;
            }

            rezultat.add(posluzitelj.kreirajRezervaciju(
                sjedalo, putnici.get(indeksPutnika)));
            indeksPutnika++;
          }
        }

        if (indeksPutnika >= putnici.size()) {
          return rezultat;
        }
      }

      posluzitelj.vratiRezervacijeGrupeNaSlobodna(let.id(), rezultat);
      throw new ProblemKodRezervacije();
    } catch (ProblemKodRezervacije e) {
      posluzitelj.vratiRezervacijeGrupeNaSlobodna(let.id(), rezultat);
      throw e;
    } catch (Exception e) {
      posluzitelj.vratiRezervacijeGrupeNaSlobodna(let.id(), rezultat);
      throw new ProblemKodRezervacije();
    }
  }

  @Override
  public List<RezervacijaSjedala> potvrdiRezervacijuSjedalaGrupe(
      List<RezervacijaSjedala> rezervacije) throws ProblemKodRezervacije {
    if (rezervacije == null || rezervacije.isEmpty()) {
      throw new ProblemKodRezervacije();
    }

    List<RezervacijaSjedala> rezultat = new ArrayList<>();

    for (RezervacijaSjedala rezervacija : rezervacije) {
      if (rezervacija == null) {
        throw new ProblemKodRezervacije();
      }

      rezultat.add(rezervacija.potvrdiRezervacijuSjedala());
    }

    return rezultat;
  }

  public RezervacijaSjedala odaberiLijevoDesno(
      Let let, int red, List<RezervacijaSjedala> slobodna) {
    List<RezervacijaSjedala> sortirana =
        this.sortirajLijevoDesno(let, red, slobodna);
    if (sortirana.isEmpty()) {
      return null;
    }
    return sortirana.get(0);
  }

  public List<RezervacijaSjedala> sortirajLijevoDesno(
      Let let, int red, List<RezervacijaSjedala> slobodna) {
    PosluziteljRezervacije posluzitelj =
        PosluziteljRezervacije.dohvatiInstancu();
    List<RezervacijaSjedala> rezultat = new ArrayList<>();

    if (posluzitelj == null || slobodna == null || slobodna.isEmpty()) {
      return rezultat;
    }

    String raspored = posluzitelj.dohvatiRasporedReda(let, red);
    if (raspored == null || raspored.isBlank()) {
      rezultat.addAll(slobodna);
      rezultat.sort(Comparator.comparing(r -> r.sjedalo().oznakaSjedala()));
      return rezultat;
    }

    List<String> redoslijed = this.izracunajRedoslijedLijevoDesno(raspored);

    for (String oznaka : redoslijed) {
      for (RezervacijaSjedala rezervacija : slobodna) {
        if (rezervacija.sjedalo().oznakaSjedala().equals(oznaka)) {
          rezultat.add(rezervacija);
          break;
        }
      }
    }

    return rezultat;
  }

  public List<String> izracunajRedoslijedLijevoDesno(String raspored) {
    List<String> sjedala = new ArrayList<>();

    for (int i = 0; i < raspored.length(); i++) {
      char znak = raspored.charAt(i);
      if (znak != '_' && znak != '-') {
        sjedala.add(String.valueOf(znak));
      }
    }

    List<String> rezultat = new ArrayList<>();
    int lijevo = 0;
    int desno = sjedala.size() - 1;
    boolean uzmiLijevo = true;

    while (lijevo <= desno) {
      if (uzmiLijevo) {
        rezultat.add(sjedala.get(lijevo));
        lijevo++;
      } else {
        rezultat.add(sjedala.get(desno));
        desno--;
      }
      uzmiLijevo = !uzmiLijevo;
    }

    return rezultat;
  }

  public List<String> dohvatiDijeloveSNajviseSjedala(
      Let let, RazredSjedala razred) {
    PosluziteljRezervacije posluzitelj =
        PosluziteljRezervacije.dohvatiInstancu();
    List<String> rezultat = new ArrayList<>();

    if (posluzitelj == null) {
      return rezultat;
    }

    int max = posluzitelj.dohvatiNajveciBrojSjedalaUDijelu(let, razred);
    List<Integer> redovi = posluzitelj.dohvatiRedoveRazreda(let, razred);

    for (Integer red : redovi) {
      List<String> dijelovi = posluzitelj.dohvatiDijeloveReda(let, red);

      for (String dio : dijelovi) {
        if (dio.length() == max && !rezultat.contains(dio)) {
          rezultat.add(dio);
        }
      }
    }

    return rezultat;
  }
}
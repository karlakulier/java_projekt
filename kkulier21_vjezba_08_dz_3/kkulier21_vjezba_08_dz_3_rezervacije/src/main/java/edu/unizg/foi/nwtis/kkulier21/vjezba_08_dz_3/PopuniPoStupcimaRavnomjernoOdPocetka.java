package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3;

import java.util.ArrayList;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import edu.unizg.foi.nwtis.Let;
import edu.unizg.foi.nwtis.OdredivanjeSjedala;
import edu.unizg.foi.nwtis.ProblemKodRezervacije;
import edu.unizg.foi.nwtis.RazredSjedala;
import edu.unizg.foi.nwtis.RezervacijaSjedala;

public class PopuniPoStupcimaRavnomjernoOdPocetka implements OdredivanjeSjedala {

  @Override
  public boolean inicijaliziraj() {
    return true;
  }

  @Override
  public RezervacijaSjedala rezervirajSjedalo(Let let, String putnik, RazredSjedala razred)
      throws ProblemKodRezervacije {
    PosluziteljRezervacije posluzitelj = PosluziteljRezervacije.dohvatiInstancu();

    if (posluzitelj == null || let == null || putnik == null || razred == null) {
      throw new ProblemKodRezervacije();
    }

    List<RezervacijaSjedala> slobodna = posluzitelj.dohvatiSlobodnaSjedala(let, razred);

    if (slobodna.isEmpty()) {
      throw new ProblemKodRezervacije();
    }

    RezervacijaSjedala odabrano = this.odaberiPoStupcima(let, slobodna);
    if (odabrano == null) {
      throw new ProblemKodRezervacije();
    }

    return posluzitelj.kreirajRezervaciju(odabrano, putnik);
  }

  @Override
  public RezervacijaSjedala potvrdiRezervacijuSjedala(RezervacijaSjedala rezervacija)
      throws ProblemKodRezervacije {
    if (rezervacija == null) {
      throw new ProblemKodRezervacije();
    }

    return rezervacija.potvrdiRezervacijuSjedala();
  }

  @Override
  public List<RezervacijaSjedala> rezervirajSjedalaZaGrupu(Let let, List<String> putnici,
      RazredSjedala razred) throws ProblemKodRezervacije {
    PosluziteljRezervacije posluzitelj = PosluziteljRezervacije.dohvatiInstancu();

    if (posluzitelj == null || let == null || putnici == null || putnici.isEmpty()
        || razred == null) {
      throw new ProblemKodRezervacije();
    }

    List<RezervacijaSjedala> slobodna = posluzitelj.dohvatiSlobodnaSjedala(let, razred);

    if (slobodna.size() < putnici.size()) {
      throw new ProblemKodRezervacije();
    }

    List<Integer> redovi = posluzitelj.dohvatiRedoveRazreda(let, razred);
    Map<Integer, List<RezervacijaSjedala>> poRedovima =
        this.grupirajSlobodnaSjedalaPoRedovima(let, razred, redovi);

    int maxUredu = this.dohvatiMaksBrojSjedalaURedu(poRedovima);
    List<RezervacijaSjedala> rezultat = new ArrayList<>();

    try {
      if (putnici.size() <= maxUredu) {
        for (Integer red : redovi) {
          List<RezervacijaSjedala> sjedala = poRedovima.get(red);
          if (sjedala == null || sjedala.size() < putnici.size()) {
            continue;
          }

          List<RezervacijaSjedala> sortirana = this.sortirajPoLijevoDesno(let, red, sjedala);

          for (int i = 0; i < putnici.size(); i++) {
            rezultat.add(posluzitelj.kreirajRezervaciju(sortirana.get(i), putnici.get(i)));
          }

          return rezultat;
        }
      }

      int indeksPutnika = 0;

      for (Integer red : redovi) {
        List<RezervacijaSjedala> sjedala = poRedovima.get(red);
        if (sjedala == null || sjedala.isEmpty()) {
          continue;
        }

        List<RezervacijaSjedala> sortirana = this.sortirajPoLijevoDesno(let, red, sjedala);

        for (RezervacijaSjedala sjedalo : sortirana) {
          if (indeksPutnika >= putnici.size()) {
            break;
          }

          rezultat.add(posluzitelj.kreirajRezervaciju(sjedalo, putnici.get(indeksPutnika)));
          indeksPutnika++;
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

  public RezervacijaSjedala odaberiPoStupcima(Let let, List<RezervacijaSjedala> slobodna) {
    Map<Integer, List<RezervacijaSjedala>> poRedovima = new LinkedHashMap<>();

    for (RezervacijaSjedala rezervacija : slobodna) {
      int red = rezervacija.sjedalo().red();
      poRedovima.computeIfAbsent(red, k -> new ArrayList<>()).add(rezervacija);
    }

    int najboljiRed = -1;
    int najvise = -1;

    for (Map.Entry<Integer, List<RezervacijaSjedala>> zapis : poRedovima.entrySet()) {
      int red = zapis.getKey();
      int broj = zapis.getValue().size();

      if (broj > najvise) {
        najvise = broj;
        najboljiRed = red;
      } else if (broj == najvise && red < najboljiRed) {
        najboljiRed = red;
      }
    }

    if (najboljiRed < 0) {
      return null;
    }

    List<RezervacijaSjedala> sjedala =
        this.sortirajPoLijevoDesno(let, najboljiRed, poRedovima.get(najboljiRed));

    if (sjedala.isEmpty()) {
      return null;
    }

    return sjedala.get(0);
  }

  public Map<Integer, List<RezervacijaSjedala>> grupirajSlobodnaSjedalaPoRedovima(Let let,
      RazredSjedala razred, List<Integer> redovi) {
    PosluziteljRezervacije posluzitelj = PosluziteljRezervacije.dohvatiInstancu();
    Map<Integer, List<RezervacijaSjedala>> rezultat = new LinkedHashMap<>();

    for (Integer red : redovi) {
      List<RezervacijaSjedala> slobodna = posluzitelj.dohvatiSlobodnaSjedalaReda(let, razred, red);
      rezultat.put(red, slobodna);
    }

    return rezultat;
  }

  public int dohvatiMaksBrojSjedalaURedu(Map<Integer, List<RezervacijaSjedala>> poRedovima) {
    int max = 0;

    for (List<RezervacijaSjedala> sjedala : poRedovima.values()) {
      if (sjedala != null && sjedala.size() > max) {
        max = sjedala.size();
      }
    }

    return max;
  }

  public List<RezervacijaSjedala> sortirajPoLijevoDesno(Let let, int red,
      List<RezervacijaSjedala> slobodna) {
    PosluziteljRezervacije posluzitelj = PosluziteljRezervacije.dohvatiInstancu();
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
}

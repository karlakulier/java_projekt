package edu.unizg.foi.nwtis.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import edu.unizg.foi.nwtis.AvioTvrtka;
import edu.unizg.foi.nwtis.Avion;
import edu.unizg.foi.nwtis.Grupa;
import edu.unizg.foi.nwtis.Korisnik;

public class KorisnikDAO {

  private final Connection vezaBP;

  public KorisnikDAO(Connection vezaBP) {
    this.vezaBP = vezaBP;
  }

  public List<Korisnik> dohvatiSve() {
    String upit = "SELECT korisnik, lozinka, prezime, ime, email FROM korisnici";
    return this.dohvatiListuKorisnika(upit);
  }

  public List<Korisnik> dohvatiSve(int odStranice, int stranica) {
    String upit = "SELECT korisnik, lozinka, prezime, ime, email FROM korisnici "
        + "ORDER BY korisnik OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setInt(1, odStranice * stranica);
      s.setInt(2, stranica);
      return this.procitajKorisnike(s);
    } catch (SQLException e) {
      return List.of();
    }
  }

  public List<Korisnik> dohvatiImePrezime(String ime, String prezime, int odStranice,
      int stranica) {
    String upit = "SELECT korisnik, lozinka, prezime, ime, email FROM korisnici "
        + "WHERE ime LIKE ? AND prezime LIKE ? ORDER BY korisnik "
        + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, "%" + ime + "%");
      s.setString(2, "%" + prezime + "%");
      s.setInt(3, odStranice * stranica);
      s.setInt(4, stranica);
      return this.procitajKorisnike(s);
    } catch (SQLException e) {
      return List.of();
    }
  }

  public Korisnik dohvati(String korisnik, String lozinka, boolean prijava) {
    String upit =
        "SELECT korisnik, lozinka, prezime, ime, email FROM korisnici " + "WHERE korisnik = ?";

    if (prijava) {
      upit += " AND lozinka = ?";
    }

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);

      if (prijava) {
        s.setString(2, lozinka);
      }

      List<Korisnik> korisnici = this.procitajKorisnike(s);
      if (korisnici.isEmpty()) {
        return null;
      }

      return prijava ? korisnici.get(0) : korisnici.get(0).korisnikBezLozinke();
    } catch (SQLException e) {
      return null;
    }
  }

  public Korisnik dohvati(String korisnik, String lozinka) {
    return this.dohvati(korisnik, lozinka, true);
  }

  public Korisnik dohvatiEmail(String email) {
    String upit =
        "SELECT korisnik, lozinka, prezime, ime, email FROM korisnici " + "WHERE email = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, email);
      List<Korisnik> korisnici = this.procitajKorisnike(s);

      if (korisnici.isEmpty()) {
        return null;
      }

      return korisnici.get(0).korisnikBezLozinke();
    } catch (SQLException e) {
      return null;
    }
  }

  public boolean postoji(String korisnik) {
    return this.dohvati(korisnik, null, false) != null;
  }

  public boolean dodaj(Korisnik k) {
    String upit = "INSERT INTO korisnici "
        + "(korisnik, ime, prezime, lozinka, email) VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, k.korisnik());
      s.setString(2, k.ime());
      s.setString(3, k.prezime());
      s.setString(4, k.lozinka());
      s.setString(5, k.email());
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean azuriraj(Korisnik k) {
    String upit =
        "UPDATE korisnici SET ime = ?, prezime = ?, lozinka = ?, " + "email = ? WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.lozinka());
      s.setString(4, k.email());
      s.setString(5, k.korisnik());
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean obrisi(String korisnik) {
    String upit = "DELETE FROM korisnici WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean korisnikImaVeze(String korisnik) {
    return this.postojiZapis("SELECT korisnik FROM uloge WHERE korisnik = ?", korisnik)
        || this.postojiZapis("SELECT korisnik FROM zastupnici WHERE korisnik = ?", korisnik);
  }

  public List<Grupa> dohvatiGrupeKorisnika(String korisnik) {
    String upit = "SELECT g.grupa, g.naziv FROM grupe g "
        + "JOIN uloge u ON g.grupa = u.grupa WHERE u.korisnik = ?";
    List<Grupa> grupe = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);

      try (ResultSet rs = s.executeQuery()) {
        while (rs.next()) {
          grupe.add(new Grupa(rs.getString("grupa"), rs.getString("naziv")));
        }
      }
    } catch (SQLException e) {
      return List.of();
    }

    return grupe;
  }

  public boolean dodajKorisnikaUGrupu(String korisnik, String grupa) {
    String upit = "INSERT INTO uloge (korisnik, grupa) VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, grupa);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean obrisiKorisnikaIzGrupe(String korisnik, String grupa) {
    String upit = "DELETE FROM uloge WHERE korisnik = ? AND grupa = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, grupa);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public AvioTvrtka dohvatiAvioTvrtkuKorisnika(String korisnik) {
    String upit = "SELECT a.tvrtka, a.naziv FROM aviotvrtke a "
        + "JOIN zastupnici z ON a.tvrtka = z.tvrtka WHERE z.korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);

      try (ResultSet rs = s.executeQuery()) {
        if (rs.next()) {
          return new AvioTvrtka(rs.getString("tvrtka"), rs.getString("naziv"),
              new ConcurrentHashMap<String, Avion>());
        }
      }
    } catch (SQLException e) {
      return null;
    }

    return null;
  }

  public AvioTvrtka dohvatiAvioTvrtkuPoEmail(String email) {
    String upit =
        "SELECT a.tvrtka, a.naziv FROM aviotvrtke a " + "JOIN zastupnici z ON a.tvrtka = z.tvrtka "
            + "JOIN korisnici k ON z.korisnik = k.korisnik WHERE k.email = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, email);

      try (ResultSet rs = s.executeQuery()) {
        if (rs.next()) {
          return new AvioTvrtka(rs.getString("tvrtka"), rs.getString("naziv"),
              new ConcurrentHashMap<String, Avion>());
        }
      }
    } catch (SQLException e) {
      return null;
    }

    return null;
  }

  public boolean dodajZastupnika(String korisnik, String avioTvrtka) {
    String upit = "INSERT INTO zastupnici (korisnik, tvrtka) VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, avioTvrtka);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean obrisiZastupnika(String korisnik, String avioTvrtka) {
    String upit = "DELETE FROM zastupnici WHERE korisnik = ? AND tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, avioTvrtka);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  private List<Korisnik> dohvatiListuKorisnika(String upit) {
    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      return this.procitajKorisnike(s);
    } catch (SQLException e) {
      return List.of();
    }
  }

  private List<Korisnik> procitajKorisnike(PreparedStatement s) throws SQLException {
    List<Korisnik> korisnici = new ArrayList<>();

    try (ResultSet rs = s.executeQuery()) {
      while (rs.next()) {
        Korisnik korisnik = new Korisnik(rs.getString("korisnik"), rs.getString("lozinka"),
            rs.getString("prezime"), rs.getString("ime"), rs.getString("email"));

        korisnici.add(korisnik);
      }
    }

    return korisnici;
  }

  private boolean postojiZapis(String upit, String vrijednost) {
    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, vrijednost);

      try (ResultSet rs = s.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      return false;
    }
  }
}

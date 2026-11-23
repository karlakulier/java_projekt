package edu.unizg.foi.nwtis.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.Grupa;
import edu.unizg.foi.nwtis.Korisnik;

public class GrupaDAO {

  private final Connection vezaBP;

  public GrupaDAO(Connection vezaBP) {
    this.vezaBP = vezaBP;
  }

  public List<Grupa> dohvatiSve() {
    String upit = "SELECT grupa, naziv FROM grupe ORDER BY grupa";
    List<Grupa> grupe = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit);
        ResultSet rs = s.executeQuery()) {
      while (rs.next()) {
        grupe.add(new Grupa(rs.getString("grupa"), rs.getString("naziv")));
      }
    } catch (SQLException e) {
      return List.of();
    }

    return grupe;
  }

  public Grupa dohvati(String grupa) {
    String upit = "SELECT grupa, naziv FROM grupe WHERE grupa = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, grupa);

      try (ResultSet rs = s.executeQuery()) {
        if (rs.next()) {
          return new Grupa(rs.getString("grupa"), rs.getString("naziv"));
        }
      }
    } catch (SQLException e) {
      return null;
    }

    return null;
  }

  public boolean dodaj(Grupa grupa) {
    String upit = "INSERT INTO grupe (grupa, naziv) VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, grupa.grupa());
      s.setString(2, grupa.naziv());
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean azuriraj(Grupa grupa) {
    String upit = "UPDATE grupe SET naziv = ? WHERE grupa = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, grupa.naziv());
      s.setString(2, grupa.grupa());
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean obrisi(String grupa) {
    String upit = "DELETE FROM grupe WHERE grupa = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, grupa);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean grupaImaKorisnike(String grupa) {
    String upit = "SELECT grupa FROM uloge WHERE grupa = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, grupa);

      try (ResultSet rs = s.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      return true;
    }
  }

  public List<Korisnik> dohvatiKorisnikeGrupe(String grupa) {
    String upit = "SELECT k.korisnik, k.lozinka, k.prezime, k.ime, k.email "
        + "FROM korisnici k JOIN uloge u ON k.korisnik = u.korisnik "
        + "WHERE u.grupa = ? ORDER BY k.korisnik";

    List<Korisnik> korisnici = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, grupa);

      try (ResultSet rs = s.executeQuery()) {
        while (rs.next()) {
          Korisnik korisnik = new Korisnik(rs.getString("korisnik"), "******",
              rs.getString("prezime"), rs.getString("ime"), rs.getString("email"));
          korisnici.add(korisnik);
        }
      }
    } catch (SQLException e) {
      return List.of();
    }

    return korisnici;
  }

  public boolean dodajKorisnika(String grupa, String korisnik) {
    String upit = "INSERT INTO uloge (korisnik, grupa) VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, grupa);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean obrisiKorisnika(String grupa, String korisnik) {
    String upit = "DELETE FROM uloge WHERE korisnik = ? AND grupa = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, grupa);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }
}

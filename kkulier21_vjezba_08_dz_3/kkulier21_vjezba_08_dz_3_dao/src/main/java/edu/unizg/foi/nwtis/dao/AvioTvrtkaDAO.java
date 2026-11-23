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
import edu.unizg.foi.nwtis.Korisnik;

public class AvioTvrtkaDAO {

  private final Connection vezaBP;

  public AvioTvrtkaDAO(Connection vezaBP) {
    this.vezaBP = vezaBP;
  }

  public List<AvioTvrtka> dohvatiSve() {
    String upit = "SELECT tvrtka, naziv FROM aviotvrtke ORDER BY tvrtka";
    List<AvioTvrtka> tvrtke = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit);
        ResultSet rs = s.executeQuery()) {
      while (rs.next()) {
        tvrtke.add(this.kreirajAvioTvrtku(rs));
      }
    } catch (SQLException e) {
      return List.of();
    }

    return tvrtke;
  }

  public AvioTvrtka dohvati(String tvrtka) {
    String upit = "SELECT tvrtka, naziv FROM aviotvrtke WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, tvrtka);

      try (ResultSet rs = s.executeQuery()) {
        if (rs.next()) {
          return this.kreirajAvioTvrtku(rs);
        }
      }
    } catch (SQLException e) {
      return null;
    }

    return null;
  }

  public boolean dodaj(AvioTvrtka tvrtka) {
    String upit = "INSERT INTO aviotvrtke (tvrtka, naziv) VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, tvrtka.id());
      s.setString(2, tvrtka.naziv());
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean azuriraj(AvioTvrtka tvrtka) {
    String upit = "UPDATE aviotvrtke SET naziv = ? WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, tvrtka.naziv());
      s.setString(2, tvrtka.id());
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean obrisi(String tvrtka) {
    String upit = "DELETE FROM aviotvrtke WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, tvrtka);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean tvrtkaImaZastupnike(String tvrtka) {
    String upit = "SELECT tvrtka FROM zastupnici WHERE tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, tvrtka);

      try (ResultSet rs = s.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      return true;
    }
  }

  public List<Korisnik> dohvatiZastupnike(String tvrtka) {
    String upit = "SELECT k.korisnik, k.lozinka, k.prezime, k.ime, k.email "
        + "FROM korisnici k JOIN zastupnici z ON k.korisnik = z.korisnik "
        + "WHERE z.tvrtka = ? ORDER BY k.korisnik";

    List<Korisnik> korisnici = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, tvrtka);

      try (ResultSet rs = s.executeQuery()) {
        while (rs.next()) {
          korisnici.add(new Korisnik(rs.getString("korisnik"), "******", rs.getString("prezime"),
              rs.getString("ime"), rs.getString("email")));
        }
      }
    } catch (SQLException e) {
      return List.of();
    }

    return korisnici;
  }

  public boolean dodajZastupnika(String tvrtka, String korisnik) {
    String upit = "INSERT INTO zastupnici (korisnik, tvrtka) VALUES (?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, tvrtka);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean obrisiZastupnika(String tvrtka, String korisnik) {
    String upit = "DELETE FROM zastupnici WHERE korisnik = ? AND tvrtka = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setString(1, korisnik);
      s.setString(2, tvrtka);
      return s.executeUpdate() == 1;
    } catch (SQLException e) {
      return false;
    }
  }

  private AvioTvrtka kreirajAvioTvrtku(ResultSet rs) throws SQLException {
    return new AvioTvrtka(rs.getString("tvrtka"), rs.getString("naziv"),
        new ConcurrentHashMap<String, Avion>());
  }
}

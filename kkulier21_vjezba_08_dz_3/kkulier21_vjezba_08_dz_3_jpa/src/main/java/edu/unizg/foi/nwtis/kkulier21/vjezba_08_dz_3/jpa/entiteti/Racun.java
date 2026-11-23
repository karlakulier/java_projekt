package edu.unizg.foi.nwtis.kkulier21.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the RACUN database table.
 * 
 */
@Entity
@NamedQuery(name="Racun.findAll", query="SELECT r FROM Racun r")
public class Racun implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="RACUN_ID_GENERATOR", sequenceName="RACUN_ID", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="RACUN_ID_GENERATOR")
	private int id;

	private int brojstavki;

	private double iznos;

	private Timestamp vrijeme;

	//bi-directional many-to-one association to Korisnici
	@ManyToOne
@JoinColumn(name="KORISNIK")
	private Korisnici korisnici;

	//bi-directional many-to-one association to Racunstavka
	@OneToMany(mappedBy="racun")
	private List<Racunstavka> racunstavkas;

	public Racun() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBrojstavki() {
		return this.brojstavki;
	}

	public void setBrojstavki(int brojstavki) {
		this.brojstavki = brojstavki;
	}

	public double getIznos() {
		return this.iznos;
	}

	public void setIznos(double iznos) {
		this.iznos = iznos;
	}

	public Timestamp getVrijeme() {
		return this.vrijeme;
	}

	public void setVrijeme(Timestamp vrijeme) {
		this.vrijeme = vrijeme;
	}

	public Korisnici getKorisnici() {
		return this.korisnici;
	}

	public void setKorisnici(Korisnici korisnici) {
		this.korisnici = korisnici;
	}

	public List<Racunstavka> getRacunstavkas() {
		return this.racunstavkas;
	}

	public void setRacunstavkas(List<Racunstavka> racunstavkas) {
		this.racunstavkas = racunstavkas;
	}

	public Racunstavka addRacunstavka(Racunstavka racunstavka) {
		getRacunstavkas().add(racunstavka);
		racunstavka.setRacun(this);

		return racunstavka;
	}

	public Racunstavka removeRacunstavka(Racunstavka racunstavka) {
		getRacunstavkas().remove(racunstavka);
		racunstavka.setRacun(null);

		return racunstavka;
	}

}
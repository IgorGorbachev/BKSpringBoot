package com.igorgorbachev.SpringBootBK.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;



import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "sail")
public class Sail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "klient_name")
    private String klientName;

    @Column(name = "data")
    private String toDay;

    @Column(name = "name_detail")
    private String nameDetail;

    @Column(name = "number_articul")
    private String numberArticul;

    @NotNull
    @Min(value = 0, message = "Закупка должна быть >= 0")
    @Column(name = "zakupka", precision = 10, scale = 2)
    private BigDecimal zakupka;

    @Column(name = "nacenka", precision = 10, scale = 2)
    private BigDecimal nacenka = BigDecimal.valueOf(0.2);

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Min(value = 1)
    @Column(name = "kolichestvo")
    private int kolichestvo;

    @Column(name = "summa", precision = 10, scale = 2)
    private BigDecimal summa;

    @Column(name = "nds", precision = 10, scale = 2)
    private BigDecimal nds = BigDecimal.valueOf(0.2);

    @Column(name = "nalog", precision = 10, scale = 2)
    private BigDecimal nalog;

    @Column(name = "pribil", precision = 10, scale = 2)
    private BigDecimal pribil;

    @Column(name = "zarplata", precision = 10, scale = 2)
    private BigDecimal zarplata;

//    @ManyToOne
//    @JoinColumn(name = "klient_id", referencedColumnName = "id")
//    private Klient klient;

    public Sail() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKlientName() {
        return klientName;
    }

    public void setKlientName(String klientName) {
        this.klientName = klientName;
    }

    public String getToDay() {
        return toDay;
    }

    public void setToDay(String toDay) {
        this.toDay = toDay;
    }

    public String getNameDetail() {
        return nameDetail;
    }

    public void setNameDetail(String nameDetail) {
        this.nameDetail = nameDetail;
    }

    public String getNumberArticul() {
        return numberArticul;
    }

    public void setNumberArticul(String numberArticul) {
        this.numberArticul = numberArticul;
    }

    public BigDecimal getZakupka() {
        return zakupka;
    }

    public void setZakupka(BigDecimal zakupka) {
        this.zakupka = zakupka;
    }

    public BigDecimal getNacenka() {
        return nacenka;
    }

    public void setNacenka(BigDecimal nacenka) {
        this.nacenka = nacenka;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getKolichestvo() {
        return kolichestvo;
    }

    public void setKolichestvo(int kolichestvo) {
        this.kolichestvo = kolichestvo;
    }

    public BigDecimal getSumma() {
        return price.multiply(BigDecimal.valueOf(kolichestvo));
    }

    public void setSumma(BigDecimal summa) {
        this.summa = summa;
    }

    public BigDecimal getNds() {
        return nds;
    }

    public void setNds(BigDecimal nds) {
        this.nds = nds;
    }

    public BigDecimal getNalog() {
        return summa.subtract(zakupka.multiply(BigDecimal.valueOf(kolichestvo))).multiply(nds);
    }

    public void setNalog(BigDecimal nalog) {
        this.nalog = nalog;
    }

    public BigDecimal getPribil() {
        return summa.subtract(zakupka.multiply(BigDecimal.valueOf(kolichestvo))).subtract(nalog);
    }

    public void setPribil(BigDecimal pribil) {
        this.pribil = pribil;
    }

    public BigDecimal getZarplata() {
        return pribil.multiply((BigDecimal.valueOf(0.5)));
    }

    public void setZarplata(BigDecimal zarplata) {
        this.zarplata = zarplata;
    }

//    public Klient getKlient() {
//        return klient;
//    }
//
//    public void setKlient(Klient klient) {
//        this.klient = klient;
//    }

    public BigDecimal calculatePriceFromZakupkaAndNacenka() {
        return zakupka.multiply(nacenka).add(zakupka);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sail sail = (Sail) o;
        return kolichestvo == sail.kolichestvo && Objects.equals(id, sail.id) && Objects.equals(klientName, sail.klientName) && Objects.equals(toDay, sail.toDay) && Objects.equals(nameDetail, sail.nameDetail) && Objects.equals(numberArticul, sail.numberArticul) && Objects.equals(zakupka, sail.zakupka) && Objects.equals(nacenka, sail.nacenka) && Objects.equals(price, sail.price) && Objects.equals(summa, sail.summa) && Objects.equals(nds, sail.nds) && Objects.equals(nalog, sail.nalog) && Objects.equals(pribil, sail.pribil) && Objects.equals(zarplata, sail.zarplata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, klientName, toDay, nameDetail, numberArticul, zakupka, nacenka, price, kolichestvo, summa, nds, nalog, pribil, zarplata);
    }

    @Override
    public String toString() {
        return "Sail{" +
               "id=" + id +
               ", klientName='" + klientName + '\'' +
               ", toDay='" + toDay + '\'' +
               ", nameDetail='" + nameDetail + '\'' +
               ", numberArticul='" + numberArticul + '\'' +
               ", zakupka=" + zakupka +
               ", nacenka=" + nacenka +
               ", price=" + price +
               ", kolichestvo=" + kolichestvo +
               ", summa=" + summa +
               ", nds=" + nds +
               ", nalog=" + nalog +
               ", pribil=" + pribil +
               ", zarplata=" + zarplata +
               '}';
    }
}

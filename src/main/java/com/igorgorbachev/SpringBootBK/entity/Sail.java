package com.igorgorbachev.SpringBootBK.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "sail")
public class Sail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data")
    private String toDay;

    @Column(name = "name_detail")
    private String nameDetail;

    @Column(name = "number_articul")
    private String numberArticul;

    @Column(name = "zakupka")
    private BigDecimal zakupka;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "kolichestvo")
    private BigDecimal kolichestvo;

    @Column(name = "summa")
    private BigDecimal summa;

    @Column(name = "nds")
    private BigDecimal nds = BigDecimal.valueOf(0.2);

    @Column(name = "nalog")
    private BigDecimal nalog;

    @Column(name = "pribil")
    private BigDecimal pribil;

    @Column(name = "zarplata")
    private BigDecimal zarplata;

//    @ManyToOne
//    @JoinColumn(name = "klient_id")
//    private Klient klient;
//
//    public Klient getKlient() {
//        return klient;
//    }
//
//    public void setKlient(Klient klient) {
//        this.klient = klient;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

        if (zakupka == null || zakupka.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Некорректная цена закупки");
        }
        this.zakupka = zakupka;
    }


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getKolichestvo() {
        return kolichestvo;
    }

    public void setKolichestvo(BigDecimal kolichestvo) {
        this.kolichestvo = kolichestvo;
    }

    public BigDecimal getSumma() {
        return price.multiply(kolichestvo);
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
        return summa.subtract(zakupka.multiply(kolichestvo)).multiply(nds);
    }

    public void setNalog(BigDecimal nalog) {
        this.nalog = nalog;
    }

    public BigDecimal getPribil() {
        return summa.subtract(zakupka.multiply(kolichestvo)).subtract(nalog);
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


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sail sail = (Sail) o;
        return kolichestvo == sail.kolichestvo && Objects.equals(id, sail.id)  && Objects.equals(toDay, sail.toDay) && Objects.equals(nameDetail, sail.nameDetail) && Objects.equals(numberArticul, sail.numberArticul) && Objects.equals(zakupka, sail.zakupka) && Objects.equals(price, sail.price) && Objects.equals(summa, sail.summa) && Objects.equals(nds, sail.nds) && Objects.equals(nalog, sail.nalog) && Objects.equals(pribil, sail.pribil) && Objects.equals(zarplata, sail.zarplata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,  toDay, nameDetail, numberArticul, zakupka, price, kolichestvo, summa, nds, nalog, pribil, zarplata);
    }

    @Override
    public String toString() {
        return "Sail{" +
               "id=" + id +
               ", toDay='" + toDay + '\'' +
               ", nameDetail='" + nameDetail + '\'' +
               ", numberArticul='" + numberArticul + '\'' +
               ", zakupka=" + zakupka +
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

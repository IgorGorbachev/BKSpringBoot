package com.igorgorbachev.SpringBootBK.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "sail")
public class Sail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data")
    private LocalDate toDay;

    @Transient
    public String getFormattedDate() {
        if (toDay == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EE, dd.MM.yy", new Locale("ru"));
        return toDay.format(formatter).toUpperCase();
    }

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "oplata_id")
    private Oplata oplata;

    @Column(name = "nameSail")
    private String nameSail;

    @Column(name = "articul")
    private String articul;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "klient_id", referencedColumnName = "id")
    private Klient klient;

    public Klient getKlient() {
        return klient;
    }

    public void setKlient(Klient klient) {
        this.klient = klient;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getToDay() {
        return toDay;
    }

    public void setToDay(LocalDate toDay) {
        this.toDay = toDay;
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
        return this.zarplata;
    }

    public void setZarplata(BigDecimal zarplata) {
        this.zarplata = zarplata;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Oplata getOplata() {
        return oplata;
    }

    public void setOplata(Oplata oplata) {
        this.oplata = oplata;
    }

    public String getNameSail() {
        return nameSail;
    }

    public void setNameSail(String nameSail) {
        this.nameSail = nameSail;
    }

    public String getArticul() {
        return articul;
    }

    public void setArticul(String articul) {
        this.articul = articul;
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Sail sail = (Sail) o;
        return Objects.equals(id, sail.id) && Objects.equals(toDay, sail.toDay) && Objects.equals(status, sail.status) && Objects.equals(oplata, sail.oplata) && Objects.equals(nameSail, sail.nameSail) && Objects.equals(articul, sail.articul) && Objects.equals(zakupka, sail.zakupka) && Objects.equals(price, sail.price) && Objects.equals(kolichestvo, sail.kolichestvo) && Objects.equals(summa, sail.summa) && Objects.equals(nds, sail.nds) && Objects.equals(nalog, sail.nalog) && Objects.equals(pribil, sail.pribil) && Objects.equals(zarplata, sail.zarplata) && Objects.equals(klient, sail.klient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, toDay, status, oplata, nameSail, articul, zakupka, price, kolichestvo, summa, nds, nalog, pribil, zarplata, klient);
    }

    @Override
    public String toString() {
        return "Sail{" +
               "id=" + id +
               ", toDay='" + toDay + '\'' +
               ", status=" + status +
               ", oplata=" + oplata +
               ", nameSail='" + nameSail + '\'' +
               ", articul='" + articul + '\'' +
               ", zakupka=" + zakupka +
               ", price=" + price +
               ", kolichestvo=" + kolichestvo +
               ", summa=" + summa +
               ", nds=" + nds +
               ", nalog=" + nalog +
               ", pribil=" + pribil +
               ", zarplata=" + zarplata +
               ", klient=" + klient +
               '}';
    }
}

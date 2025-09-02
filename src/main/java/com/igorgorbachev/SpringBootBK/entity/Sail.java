package com.igorgorbachev.SpringBootBK.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.EAGER)
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

    public void setZakupka(BigDecimal zakupka) {

        if (zakupka == null || zakupka.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Некорректная цена закупки");
        }
        this.zakupka = zakupka;
    }




    public BigDecimal getSumma() {
        return price.multiply(kolichestvo);
    }

    public BigDecimal getNalog() {
        return summa.subtract(zakupka.multiply(kolichestvo)).multiply(nds);
    }

    public BigDecimal getPribil() {
        return summa.subtract(zakupka.multiply(kolichestvo)).subtract(nalog);
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sail sail = (Sail) o;
        return id != null && Objects.equals(id, sail.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sail{" +
                "id=" + id +
                ", toDay=" + toDay +
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
                '}';
    }
}

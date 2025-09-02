package com.igorgorbachev.SpringBootBK.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "vin")
    private String vin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klient_id")
    private Klient klient;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "car_detail",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "detail_id")
    )
    private List<Detail> details = new ArrayList<>();

    public Car(String name, String vin) {
        this.name = name;
        this.vin = vin;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(vin, car.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin);
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", vin='" + vin + '\'' +
                '}';
    }
}

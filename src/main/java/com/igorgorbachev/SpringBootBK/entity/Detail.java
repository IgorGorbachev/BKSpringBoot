package com.igorgorbachev.SpringBootBK.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;
@Entity
@Table(name = "details")
public class Detail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "origin_articul")
    private String originArticul;

    @Column(name = "analog_articul")
    private String analogArticul;

    @ManyToOne
    private Car car;

    public Detail() {
    }

    public Detail(String name, String originArticul, String analogArticul) {
        this.name = name;
        this.originArticul = originArticul;
        this.analogArticul = analogArticul;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginArticul() {
        return originArticul;
    }

    public void setOriginArticul(String originArticul) {
        this.originArticul = originArticul;
    }

    public String getAnalogArticul() {
        return analogArticul;
    }

    public void setAnalogArticul(String analogArticul) {
        this.analogArticul = analogArticul;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Detail details = (Detail) o;
        return Objects.equals(id, details.id) && Objects.equals(name, details.name) && Objects.equals(originArticul, details.originArticul) && Objects.equals(analogArticul, details.analogArticul);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, originArticul, analogArticul);
    }

    @Override
    public String toString() {
        return "Details{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", originArticul='" + originArticul + '\'' +
               ", analogArticul='" + analogArticul + '\'' +
               '}';
    }
}

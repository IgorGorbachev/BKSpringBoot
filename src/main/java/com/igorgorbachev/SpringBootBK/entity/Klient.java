package com.igorgorbachev.SpringBootBK.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Entity
@Table(name = "klients")
public class Klient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

//    @OneToMany(mappedBy = "klient", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Sail> sails = new ArrayList<>();
//
//    public List<Sail> getSails() {
//        return sails;
//    }
//
//    public void setSails(List<Sail> sails) {
//        this.sails = sails;
//    }

    @OneToMany(mappedBy = "klient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Car> car = new ArrayList<>();

    public List<Car> getCar() {
        return car;
    }

    public void setCar(List<Car> car) {
        this.car = car;
    }

    public Klient() {
    }

    public Klient(String name, String phone) {
        this.name = name;
        this.phone = phone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
//
//    public List<Sail> getSails() {
//        return sails;
//    }
//
//    public void setSails(List<Sail> sails) {
//        this.sails = sails;
//    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Klient klient = (Klient) o;
        return Objects.equals(id, klient.id) && Objects.equals(name, klient.name) && Objects.equals(phone, klient.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phone);
    }

    @Override
    public String toString() {
        return "Klient{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", phone='" + phone + '\'' +
               '}';
    }
}

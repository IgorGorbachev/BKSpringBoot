package com.igorgorbachev.SpringBootBK.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Oplata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String nameOplata;

    public Oplata() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameOplata() {
        return nameOplata;
    }

    public void setNameOplata(String nameOplata) {
        this.nameOplata = nameOplata;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Oplata oplata = (Oplata) o;
        return Objects.equals(id, oplata.id) && Objects.equals(nameOplata, oplata.nameOplata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameOplata);
    }

    @Override
    public String toString() {
        return "Oplata{" +
               "id=" + id +
               ", nameOplata='" + nameOplata + '\'' +
               '}';
    }
}

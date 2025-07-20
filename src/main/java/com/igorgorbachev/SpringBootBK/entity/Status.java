package com.igorgorbachev.SpringBootBK.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String nameStatus;

    public Status() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameStatus() {
        return nameStatus;
    }

    public void setNameStatus(String nameStatus) {
        this.nameStatus = nameStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return Objects.equals(id, status.id) && Objects.equals(nameStatus, status.nameStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameStatus);
    }

    @Override
    public String toString() {
        return "Status{" +
               "id=" + id +
               ", nameStatus='" + nameStatus + '\'' +
               '}';
    }
}

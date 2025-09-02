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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    public Detail(String name, String originArticul, String analogArticul) {
        this.name = name;
        this.originArticul = originArticul;
        this.analogArticul = analogArticul;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Detail detail = (Detail) o;
        return Objects.equals(analogArticul, detail.analogArticul);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(analogArticul);
    }

    @Override
    public String toString() {
        return "Detail{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", originArticul='" + originArticul + '\'' +
                ", analogArticul='" + analogArticul + '\'' +
                '}';
    }
}

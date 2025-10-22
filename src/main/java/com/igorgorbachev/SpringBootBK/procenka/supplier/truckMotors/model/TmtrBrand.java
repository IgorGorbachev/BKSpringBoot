package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmtrBrand {
    @JsonProperty("article")
    private String article;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("raiting")
    private Integer rating;

    @JsonProperty("name")
    private String name;
}

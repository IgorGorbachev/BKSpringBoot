package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EtspSearchRequest {
    @JsonProperty("HashSession")
    private String HashSession;
    @JsonProperty("Text")
    private String Text;
    @JsonProperty("WithAnalogsSearch")
    private Boolean WithAnalogsSearch;

    public EtspSearchRequest(String hashSession, String text, Boolean withAnalogsSearch) {
        this.HashSession = hashSession;
        this.Text = text;
        this.WithAnalogsSearch = withAnalogsSearch;
    }
}

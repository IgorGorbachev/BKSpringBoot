package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogonRequest {
    @JsonProperty("Login")
    private String Login;
    @JsonProperty("Password")
    private String Password;
}

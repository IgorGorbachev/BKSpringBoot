package com.igorgorbachev.SpringBootBK.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO Klient для ответа")
public record KlientResponseDto(

        @Schema(description = "Id клиента", example = "1")
        Long id,

        @Schema(description = "Имя клиента", example = "Name")
        String name,

        @Schema(description = "Все продажи данного клиента", example = "список продаж")
        List<SailResponseDto> sails,

        @Schema(description = "Все автомобили клиента", example = "список авто")
        List<CarResponseDto> cars) {

}

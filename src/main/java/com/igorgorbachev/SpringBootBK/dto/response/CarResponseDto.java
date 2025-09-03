package com.igorgorbachev.SpringBootBK.dto.response;

import com.igorgorbachev.SpringBootBK.entity.Detail;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO Car для ответа")
public record CarResponseDto(
    @Schema(description = "Id авто", example = "1")
    Long id,

    @Schema(description = "Название авто", example = "carName")
    String name,

    @Schema(description = "Вин номер", example = "vin")
    String vin,

    @Schema(description = "Клиент которму принадлежит авто", example = "klientName")
    Klient klient,

    @Schema(description = "Детали для этого авто", example = "detailForCar")
    List<Detail> details
) {
}

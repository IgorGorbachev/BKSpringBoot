package com.igorgorbachev.SpringBootBK.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dto Oplata для ответа")
public record OplataResponseDto(

        @Schema(description = "Id оплаты", example = "1")
        Long id,

        @Schema(description = "Название оплаты", example = "наличные")
        String nameOplata

        ) {
}

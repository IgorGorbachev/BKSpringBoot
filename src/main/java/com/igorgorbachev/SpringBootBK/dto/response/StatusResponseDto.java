package com.igorgorbachev.SpringBootBK.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dto Status для ответа")
public record StatusResponseDto(

        @Schema(description = "Id статуса", example = "1")
        Long id,

        @Schema(description = "Название статуса", example = "В пути")
        String nameStatus
) {
}

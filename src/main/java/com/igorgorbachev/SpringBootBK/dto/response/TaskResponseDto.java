package com.igorgorbachev.SpringBootBK.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dto Task для ответа")
public record TaskResponseDto(

        @Schema(description = "Id задачи", example = "1")
        Long id,

        @Schema(description = "Текст задачи", example = "Найти фильтр")
        String text

) {
}

package com.igorgorbachev.SpringBootBK.dto.response;

import com.igorgorbachev.SpringBootBK.entity.Car;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO Detail для ответа")
public record DetailResponseDto(

        @Schema(description = "Id детали", example = "1")
        Long id,

        @Schema(description = "Название детали", example = "nameDetail")
        String name,

        @Schema(description = "Оригинальный артикул", example = "1")
        String originArticul,

        @Schema(description = "Артикул аналога", example = "1")
        String analogArticul,

        @Schema(description = "Какому авто принадлжеат", example = "carId")
        Car car

        ) {
}

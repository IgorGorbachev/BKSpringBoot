package com.igorgorbachev.SpringBootBK.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dto Sail для ответа")
public record SailResponseDto(
        @Schema(description = "ID продажи", example = "1")
        Long id,

        @Schema(description = "Дата продажи", example = "2024-01-15")
        LocalDate date,

        @Schema(description = "Форматированная дата", example = "ПН, 15.01.24")
        String formattedDate,

        @Schema(description = "Статус продажи")
        StatusResponseDto status,

        @Schema(description = "Способ оплаты")
        OplataResponseDto oplata,

        @Schema(description = "Название продажи", example = "Продажа товара X")
        String nameSail,

        @Schema(description = "Артикул товара", example = "ART-12345")
        String articul,

        @Schema(description = "Цена закупки", example = "100.50")
        BigDecimal zakupka,

        @Schema(description = "Цена продажи", example = "150.75")
        BigDecimal price,

        @Schema(description = "Количество", example = "2.0")
        BigDecimal kolichestvo,

        @Schema(description = "Сумма продажи", example = "301.50")
        BigDecimal summa,

        @Schema(description = "НДС ставка", example = "0.20")
        BigDecimal nds,

        @Schema(description = "Налог", example = "40.20")
        BigDecimal nalog,

        @Schema(description = "Прибыль", example = "160.80")
        BigDecimal pribil,

        @Schema(description = "Зарплата", example = "50.25")
        BigDecimal zarplata,

        @Schema(description = "Клиент")
        KlientResponseDto klient


) {
}

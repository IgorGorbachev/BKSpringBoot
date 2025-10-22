package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponse {
    @JsonProperty("goods")
    private List<FavoritePartsGoods> goods;

    @JsonProperty("error")
    private String error;

    // Добавляем метод hasError()
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    // Дополнительные полезные методы
    public boolean isEmpty() {
        return (goods == null || goods.isEmpty()) && !hasError();
    }

    public boolean isSuccess() {
        return !hasError() && goods != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApiResponse that = (ApiResponse) o;
        return Objects.equals(goods, that.goods) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goods, error);
    }
}

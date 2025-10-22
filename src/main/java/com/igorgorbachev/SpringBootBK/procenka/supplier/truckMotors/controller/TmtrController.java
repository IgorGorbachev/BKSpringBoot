package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.controller;

import com.igorgorbachev.SpringBootBK.exception.TmtrException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tmtr")
@RequiredArgsConstructor
public class TmtrController {
    private final TmtrService tmtrService;

    @PostMapping("/search")
    public ResponseEntity<?> searchParts(
            @RequestParam String article,
            @RequestParam(required = false) String brand) {

        try {
            List<TmtrGoods> goods = tmtrService.searchTmtrParts(article, brand); // Используем переименованный метод
            return ResponseEntity.ok(goods);
        } catch (TmtrException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/search/reactive")
    public Mono<ResponseEntity<List<TmtrGoods>>> searchPartsReactive(
            @RequestParam String article,
            @RequestParam(required = false) String brand) {

        return tmtrService.searchPartsReactive(article, brand)
                .map(ResponseEntity::ok)
                .onErrorResume(TmtrException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    // DTO для ошибок
    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}

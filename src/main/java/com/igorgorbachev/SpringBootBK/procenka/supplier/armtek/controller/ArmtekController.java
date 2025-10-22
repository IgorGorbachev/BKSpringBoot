package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.controller;


import com.igorgorbachev.SpringBootBK.exception.ArmtekException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.ArmtekService;
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
@RequestMapping("/api/armtek")
@RequiredArgsConstructor
public class ArmtekController {
    private final ArmtekService armtekService;

    @PostMapping("/search")
    public ResponseEntity<?> searchParts(
            @RequestParam String pin,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String queryType,
            @RequestParam(required = false) String program) {

        try {
            List<ArmtekGoods> goods = armtekService.searchParts(pin, brand, queryType, program);
            return ResponseEntity.ok(goods);
        } catch (ArmtekException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/search/reactive")
    public Mono<ResponseEntity<List<ArmtekGoods>>> searchPartsReactive(
            @RequestParam String pin,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String queryType,
            @RequestParam(required = false) String program) {

        return armtekService.searchPartsReactive(pin, brand, queryType, program)
                .map(ResponseEntity::ok)
                .onErrorResume(ArmtekException.class,
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

package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.controller;

import com.igorgorbachev.SpringBootBK.exception.ForumAutoException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.service.ForumAutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


import java.util.List;

@RestController
@RequestMapping("/api/forum-auto")
public class ForumAutoController {
    private final ForumAutoService forumAutoService;

    public ForumAutoController(ForumAutoService forumAutoService) {
        this.forumAutoService = forumAutoService;
    }

    @GetMapping("/goods")
    public ResponseEntity<?> listGoods(
            @RequestParam String art,
            @RequestParam(required = false) String br,
            @RequestParam(required = false, defaultValue = "true") Boolean cross,
            @RequestParam(required = false) String gid) {

        try {
            List<ForumAutoGoods> goods = forumAutoService.listGoods(art, br, cross, gid);
            return ResponseEntity.ok(goods);
        } catch (ForumAutoException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/goods/reactive")
    public Mono<ResponseEntity<List<ForumAutoGoods>>> listGoodsReactive(
            @RequestParam String art,
            @RequestParam(required = false) String br,
            @RequestParam(required = false, defaultValue = "true") Boolean cross,
            @RequestParam(required = false) String gid) {

        return forumAutoService.listGoodsReactive(art, br, cross, gid)
                .map(ResponseEntity::ok)
                .onErrorResume(ForumAutoException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @GetMapping("/client-info")
    public Mono<ResponseEntity<String>> getClientInfo() {
        return forumAutoService.getClientInfo()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("Error: " + e.getMessage())));
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

package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.service.impl;

import com.igorgorbachev.SpringBootBK.exception.FavoritePartsException;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.config.FavoritePartsConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.ApiResponse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.FavoritePartsGoods;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.service.FavoriteService;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.netty.http.client.HttpClient;


import java.io.IOException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FavoritePartsServiceImpl implements FavoriteService, SupplierService {
    private final WebClient webClient;
    private final FavoritePartsConfig favoritePartsConfig;
    private final RetryTemplate retryTemplate;

    public FavoritePartsServiceImpl(WebClient.Builder webClientBuilder, FavoritePartsConfig favoritePartsConfig) {
        this.favoritePartsConfig = favoritePartsConfig;

        // Создаем HttpClient с улучшенными настройками
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 5 секунд на подключение
                .responseTimeout(Duration.ofSeconds(30)) // 15 секунд на ответ
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                )
                // Важно: настройка Keep-Alive
                .keepAlive(true)
                .compress(true);

        this.webClient = webClientBuilder
                .baseUrl(favoritePartsConfig.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        this.retryTemplate = createRetryTemplate();
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        // Создаем политику повторных попыток с указанием повторяемых исключений
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(FavoritePartsException.class, true);
        retryableExceptions.put(SocketException.class, true);
        retryableExceptions.put(IOException.class, true);
        retryableExceptions.put(TimeoutException.class, true);
        retryableExceptions.put(org.springframework.web.reactive.function.client.WebClientRequestException.class, true);
        retryableExceptions.put(io.netty.handler.timeout.ReadTimeoutException.class, true);

        // Используем конструктор с картой повторяемых исключений
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        template.setRetryPolicy(retryPolicy);

        // Экспоненциальная backoff политика
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000L); // Начальная задержка 2 секунды
        backOffPolicy.setMultiplier(1.5); // Увеличиваем задержку в 1.5 раза
        backOffPolicy.setMaxInterval(10000L); // Максимальная задержка 10 секунд

        template.setBackOffPolicy(backOffPolicy);

        // Логирование повторных попыток
        template.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                Throwable rootCause = getRootCause(throwable);
                log.info("Retry attempt {} for {}: {}",
                        context.getRetryCount(),
                        rootCause.getClass().getSimpleName(),
                        rootCause.getMessage());
            }
        });

        return template;
    }

    /**
     * Нормализует артикул: удаляет все нецифровые символы кроме букв
     * Например: "096.032/1" -> "0960321"
     */
    private String normalizeArticle(String article) {
        if (article == null) {
            return null;
        }

        // Удаляем все символы, которые не являются цифрами или буквами
        String normalized = article.replaceAll("[^a-zA-Z0-9]", "");

        log.debug("Нормализация артикула: '{}' -> '{}'", article, normalized);
        return normalized;
    }

    @Override
    public List<FavoritePartsGoods> getPrice(String number, String brand, Boolean analogues, Boolean info) {
        try {
            // Нормализуем артикул перед выполнением запроса
            String normalizedNumber = normalizeArticle(number);

            return retryTemplate.execute(context -> {
                try {
                    log.info("Отправляем запрос: number={} (нормализованный: {}), brand={}, analogues={}, info={}",
                            number, normalizedNumber, brand, analogues, info);

                    ApiResponse response = webClient.get()
                            .uri(uriBuilder -> {
                                UriBuilder builder = uriBuilder
                                        .path("/hs/hsprice/")
                                        .queryParam("key", favoritePartsConfig.getApiKey())
                                        .queryParam("number", encodeValue(normalizedNumber));

                                if (brand != null && !brand.trim().isEmpty()) {
                                    builder.queryParam("brand", encodeValue(brand));
                                }
                                if (Boolean.TRUE.equals(analogues)) {
                                    builder.queryParam("analogues", "on");
                                }
                                if (Boolean.TRUE.equals(info)) {
                                    builder.queryParam("info", "on");
                                }

                                return builder.build();
                            })
                            .retrieve()
                            .bodyToMono(ApiResponse.class)
                            .timeout(Duration.ofSeconds(30))
                            .block();

                    // ЛОГИРУЕМ ОТВЕТ
                    if (response != null) {
                        if (response.hasError()) {
                            log.error("API Error: {}", response.getError());
                            throw new FavoritePartsException("API Error: " + response.getError());
                        } else {
                            log.info("Успешный ответ: найдено {} товаров",
                                    response.getGoods() != null ? response.getGoods().size() : 0);

                            // Детальная информация о каждом товаре
                            if (response.getGoods() != null) {
                                for (int i = 0; i < response.getGoods().size(); i++) {
                                    FavoritePartsGoods goods = response.getGoods().get(i);
                                    log.info("Товар {}: {} {}, warehouses: {}, analogues: {}",
                                            i, goods.getBrand(), goods.getNumber(),
                                            goods.getWarehouses() != null ? goods.getWarehouses().size() : 0,
                                            goods.getAnalogues() != null ? goods.getAnalogues().size() : 0);
                                }
                            }
                        }
                    }

                    return response != null ? response.getGoods() : List.of();

                } catch (WebClientResponseException e) {
                    log.error("HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw new FavoritePartsException("HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
                } catch (Exception e) {
                    // Извлекаем корневую причину исключения
                    Throwable rootCause = getRootCause(e);
                    log.error("Service error: {} - {}", rootCause.getClass().getSimpleName(), rootCause.getMessage());

                    // Пробрасываем оригинальное исключение или его причину для правильной обработки RetryTemplate
                    if (rootCause instanceof SocketException || rootCause instanceof IOException || rootCause instanceof TimeoutException) {
                        throw (RuntimeException) rootCause;
                    }
                    throw new FavoritePartsException("Service error: " + rootCause.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Все попытки подключения к Favorite-Parts провалились: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return List.of();
        }
    }

    // Вспомогательный метод для получения корневой причины исключения
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }


    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


    @Override
    public String getSupplierName() {
        return "Favorite Parts";
    }


    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            List<FavoritePartsGoods> goods = this.getPriceWithAnaloguesOptimized(article, brand);
            return goods.stream()
                    .map(PartOfferDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при поиске в Favorite-Parts: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    public List<String> findBrandsByArticle(String number) {
        try {
            // Нормализуем артикул перед поиском брендов
            String normalizedNumber = normalizeArticle(number);
            List<FavoritePartsGoods> goods = getPrice(normalizedNumber, null, false, false);
            return goods.stream()
                    .map(FavoritePartsGoods::getBrand)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при поиске брендов: {}", e.getMessage());
            return List.of();
        }
    }

    public List<FavoritePartsGoods> getPriceWithAnalogues(String number, String brand) {
        // Нормализуем артикул
        String normalizedNumber = normalizeArticle(number);
        return getPrice(normalizedNumber, brand, true, true);
    }

    public List<FavoritePartsGoods> getPriceWithAnaloguesOptimized(String number, String brand) {
        // Нормализуем артикул
        String normalizedNumber = normalizeArticle(number);

        // Получаем основной товар с аналогами (один запрос)
        List<FavoritePartsGoods> mainGoods = getPrice(normalizedNumber, brand, true, true);

        if (mainGoods == null || mainGoods.isEmpty()) {
            return mainGoods;
        }

        // Обрабатываем каждый основной товар
        for (FavoritePartsGoods mainItem : mainGoods) {
            // Сортируем склады основного товара
            if (mainItem.getWarehouses() != null) {
                sortWarehouses(mainItem.getWarehouses());
            }

            // Фильтруем и сортируем аналоги
            if (mainItem.getAnalogues() != null && !mainItem.getAnalogues().isEmpty()) {
                List<FavoritePartsGoods> optimizedAnalogues = filterAndSortAnalogues(mainItem.getAnalogues());
                mainItem.setAnalogues(optimizedAnalogues);
            }
        }

        return mainGoods;
    }

    /**
     * Фильтрация и сортировка аналогов без дополнительных запросов
     */
    private List<FavoritePartsGoods> filterAndSortAnalogues(List<FavoritePartsGoods> analogues) {
        return analogues.stream()
                // ФИЛЬТРАЦИЯ: убираем аналоги с нулевым количеством
                .filter(analogue -> analogue.getCount() != null && analogue.getCount() > 0)
                // ФИЛЬТРАЦИЯ: убираем аналоги без цены
                .filter(analogue -> analogue.getPrice() != null && analogue.getPrice() > 0)
                // СОРТИРОВКА: по цене (от меньшей к большей)
                .sorted((a1, a2) -> {
                    if (a1.getPrice() == null && a2.getPrice() == null) return 0;
                    if (a1.getPrice() == null) return 1;
                    if (a2.getPrice() == null) return -1;
                    return Double.compare(a1.getPrice(), a2.getPrice());
                })
                .collect(Collectors.toList());
    }

    /**
     * Сортировка складов по приоритету (для основного товара)
     */
    public void sortWarehouses(List<Warehouse> warehouses) {
        if (warehouses == null || warehouses.size() <= 1) {
            return;
        }

        warehouses.sort((w1, w2) -> {
            // 1. Приоритет: свои склады над внешними
            if (Boolean.TRUE.equals(w1.getOwn()) && !Boolean.TRUE.equals(w2.getOwn())) {
                return -1;
            }
            if (!Boolean.TRUE.equals(w1.getOwn()) && Boolean.TRUE.equals(w2.getOwn())) {
                return 1;
            }

            // 2. Приоритет: более низкая цена
            int priceComparison = Double.compare(
                    w1.getPrice() != null ? w1.getPrice() : Double.MAX_VALUE,
                    w2.getPrice() != null ? w2.getPrice() : Double.MAX_VALUE
            );
            if (priceComparison != 0) {
                return priceComparison;
            }

            // 3. Приоритет: более ранняя дата отгрузки
            if (w1.getShipmentDate() != null && w2.getShipmentDate() != null) {
                return w1.getShipmentDate().compareTo(w2.getShipmentDate());
            }
            if (w1.getShipmentDate() != null) {
                return -1;
            }
            if (w2.getShipmentDate() != null) {
                return 1;
            }

            return 0;
        });
    }
}
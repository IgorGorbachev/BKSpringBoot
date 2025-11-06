package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.FavoritePartsGoods;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class FavoritePartsClientConfig {

    private final FavoritePartsConfig favoritePartsConfig;

    @Bean
    public WebClient favoritePartsWebClient() {
        int timeoutSeconds = favoritePartsConfig.getTimeout() / 1000;

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, favoritePartsConfig.getTimeout())
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                )
                .keepAlive(true)
                .compress(true);

        return WebClient.builder()
                .baseUrl(favoritePartsConfig.getBaseUrl())
                .defaultHeader("User-Agent", "SpringBootBK/1.0")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public Cache<String, String> normalizedArticlesCache() {
        return Caffeine.newBuilder()
                .maximumSize(favoritePartsConfig.getCacheSize())
                .expireAfterAccess(favoritePartsConfig.getCacheTtl().toMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, List<FavoritePartsGoods>> searchResultsCache() {
        return Caffeine.newBuilder()
                .maximumSize(favoritePartsConfig.getCacheSize())
                .expireAfterWrite(10, TimeUnit.MINUTES) // Кэш результатов на 10 минут
                .recordStats()
                .build();
    }

    @Bean
    @Scope("prototype")
    public RetryTemplate favoritePartsRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = Map.of(
                java.net.SocketException.class, true,
                java.io.IOException.class, true,
                java.util.concurrent.TimeoutException.class, true,
                org.springframework.web.reactive.function.client.WebClientRequestException.class, true,
                io.netty.handler.timeout.ReadTimeoutException.class, true,
                org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests.class, true
        );

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                favoritePartsConfig.getMaxAttempts(),
                retryableExceptions,
                true // traverseCauses
        );
        template.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(favoritePartsConfig.getInitialInterval());
        backOffPolicy.setMultiplier(favoritePartsConfig.getMultiplier());
        backOffPolicy.setMaxInterval(favoritePartsConfig.getMaxInterval());

        template.setBackOffPolicy(backOffPolicy);
        return template;
    }
}
package com.globe.gcash.packaging.config;

import com.globe.gcash.packaging.exception.ApiClientException;
import com.globe.gcash.packaging.exception.InvalidVoucherException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class WebClients {
    @Value("${app.api.voucher.host}")
    private String voucherApiHost;

    @Bean
    public WebClient voucherApiWebClient(WebClient.Builder builder) {
        return builder.baseUrl(voucherApiHost)
                .filter(errorHandler())
                .build();
    }

    public ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return Mono.error(new ApiClientException("Client Error: Unable to validate voucher"));
            } else if (clientResponse.statusCode().is4xxClientError()) {
                return Mono.error(new InvalidVoucherException("Invalid Voucher"));
            } else {
                return Mono.just(clientResponse);
            }
        });
    }
}

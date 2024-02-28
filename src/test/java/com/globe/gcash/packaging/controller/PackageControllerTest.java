package com.globe.gcash.packaging.controller;

import com.globe.gcash.packaging.model.response.PackageCostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class PackageControllerTest {
    private static final double DEFAULT_DISCOUNT = 0.20;

    @Autowired
    PackageController packageController;

    @Autowired
    WebTestClient client;


    @Value("${app.parcel.weight.heavy.price}")
    private double heavyParcelPrice;
    @Value("${app.parcel.volume.small.price}")
    private double smallParcelPrice;
    @Value("${app.parcel.volume.medium.price}")
    private double mediumParcelPrice;
    @Value("${app.parcel.volume.large.price}")
    private double largeParcelPrice;

    @Test
    @DisplayName("simple test with valid voucher")
    void simpleTestWithValidVoucher() {
        double multiplier = (1.00-DEFAULT_DISCOUNT);
        double expectedCost = smallParcelPrice*10*11*12*multiplier;
        BigDecimal bd = new BigDecimal(expectedCost).setScale(2, RoundingMode.HALF_UP);
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", 10)
                        .queryParam("width", 11)
                        .queryParam("height", 12)
                        .queryParam("weight", 5)
                        .queryParam("voucher", "valid")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length").isEqualTo("10.0")
                .jsonPath("$.width").isEqualTo("11.0")
                .jsonPath("$.height").isEqualTo("12.0")
                .jsonPath("$.weight").isEqualTo("5.0")
                .jsonPath("$.discount").isEqualTo("0.2")
                .jsonPath("$.cost").isEqualTo(bd.doubleValue());
    }

    @Test
    @DisplayName("simple test with invalid voucher")
    void simpleTestWithInValidVoucher() {
        double expectedCost = smallParcelPrice*10*11*12;
        BigDecimal bd = new BigDecimal(expectedCost).setScale(2, RoundingMode.HALF_UP);
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", 10)
                        .queryParam("width", 11)
                        .queryParam("height", 12)
                        .queryParam("weight", 5)
                        .queryParam("voucher", "invalid")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length").isEqualTo("10.0")
                .jsonPath("$.width").isEqualTo("11.0")
                .jsonPath("$.height").isEqualTo("12.0")
                .jsonPath("$.weight").isEqualTo("5.0")
                .jsonPath("$.discount").isEqualTo("0.0")
                .jsonPath("$.error").isEqualTo("Invalid Voucher")
                .jsonPath("$.cost").isEqualTo(bd.doubleValue());
    }

    @Test
    @DisplayName("simple test with no voucher")
    void simpleTestWithNoVoucher() {
        double expectedCost = smallParcelPrice*10*11*12;
        BigDecimal bd = new BigDecimal(expectedCost).setScale(2, RoundingMode.HALF_UP);
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", 10)
                        .queryParam("width", 11)
                        .queryParam("height", 12)
                        .queryParam("weight", 5)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length").isEqualTo("10.0")
                .jsonPath("$.width").isEqualTo("11.0")
                .jsonPath("$.height").isEqualTo("12.0")
                .jsonPath("$.weight").isEqualTo("5.0")
                .jsonPath("$.discount").isEqualTo("0.0")
                .jsonPath("$.cost").isEqualTo(bd.doubleValue());
    }
    @Test
    @DisplayName("Test expired voucher")
    void testExpiredVoucher() {
        double expectedCost = smallParcelPrice*10*11*12;
        BigDecimal bd = new BigDecimal(expectedCost).setScale(2, RoundingMode.HALF_UP);
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", 10)
                        .queryParam("width", 11)
                        .queryParam("height", 12)
                        .queryParam("weight", 5)
                        .queryParam("voucher", "expired")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Voucher is expired")
                .jsonPath("$.discount").isEqualTo("0.0")
                .jsonPath("$.cost").isEqualTo(bd.doubleValue());
    }

    @Test
    @DisplayName("Test missing discount on voucher response: Assumption is that discount will not be applied")
    void testMissingDiscountInfo() {
        double expectedCost = smallParcelPrice*10*11*12;
        BigDecimal bd = new BigDecimal(expectedCost).setScale(2, RoundingMode.HALF_UP);
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", 10)
                        .queryParam("width", 11)
                        .queryParam("height", 12)
                        .queryParam("weight", 5)
                        .queryParam("voucher", "missing")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.discount").isEqualTo("0.0")
                .jsonPath("$.cost").isEqualTo(bd.doubleValue());
    }

    @DisplayName("Test Different package configuration")
    @ParameterizedTest
    @CsvSource({
            "10,11,12,5,valid,small",
            "10,20,10,5,valid,medium",
            "10,40,10,5,valid,large",
            "10,40,10,20,valid,heavy"
    })
    void testDifferentConfigurationCost(double l, double w, double h, double weight, String voucher, String packageEvaluation) {
        double parcelPrice = switch (packageEvaluation) {
            case "small" -> smallParcelPrice;
            case "medium" -> mediumParcelPrice;
            case "large" -> largeParcelPrice;
            case "heavy" -> heavyParcelPrice;
            default -> 0.0;
        };
        double multiplier = (1.00-DEFAULT_DISCOUNT);
        double expectedCost = "heavy".equals(packageEvaluation)? parcelPrice*weight*multiplier:parcelPrice*l*w*h*multiplier;
        BigDecimal bd = new BigDecimal(expectedCost).setScale(2, RoundingMode.HALF_UP);
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", l)
                        .queryParam("width", w)
                        .queryParam("height", h)
                        .queryParam("weight", weight)
                        .queryParam("voucher", voucher)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.cost").isEqualTo(bd.doubleValue());
    }

    @DisplayName("Test overweight package")
    @Test
    void overWeightPackage() {
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", "10")
                        .queryParam("width", "10")
                        .queryParam("height", "10")
                        .queryParam("weight", "52")
                        .queryParam("voucher", "invalid")
                        .build())
                .exchange()
                .expectStatus().is4xxClientError();
    }
    @DisplayName("Test 500 response from voucher API")
    @Test
    void serverErrorVoucherAPI() {
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", "10")
                        .queryParam("width", "10")
                        .queryParam("height", "10")
                        .queryParam("weight", "22")
                        .queryParam("voucher", "error")
                        .build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Client Error: Unable to validate voucher");
    }

    @DisplayName("Test Invalid Request")
    @Test
    void testInvalidRequest() {
        client.get().uri(uriBuilder -> uriBuilder
                        .path("/v1/package/cost")
                        .queryParam("length", "0")
                        .queryParam("width", "10")
                        .queryParam("height", "10")
                        .queryParam("weight", "22")
                        .build())
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.errorMessage").isEqualTo("Invalid request, please refer to API documentation.");
    }



}

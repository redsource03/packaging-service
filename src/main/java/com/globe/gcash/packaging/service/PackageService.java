package com.globe.gcash.packaging.service;

import com.globe.gcash.packaging.exception.InvalidVoucherException;
import com.globe.gcash.packaging.exception.PackageRejectedException;
import com.globe.gcash.packaging.model.PackageInfo;
import com.globe.gcash.packaging.model.response.PackageCostResponse;
import com.globe.gcash.packaging.model.response.VoucherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PackageService {

    @Value("${app.api.voucher.api-key}")
    private String voucherApiKey;
    @Value("${app.parcel.weight.reject.value}")
    private double rejectParcelWeight;
    @Value("${app.parcel.weight.heavy.value}")
    private double heavyParcelWeight;
    @Value("${app.parcel.weight.heavy.price}")
    private double heavyParcelPrice;
    @Value("${app.parcel.volume.small.value}")
    private double smallParcelVolume;
    @Value("${app.parcel.volume.small.price}")
    private double smallParcelPrice;

    @Value("${app.parcel.volume.medium.value}")
    private double mediumParcelVolume;
    @Value("${app.parcel.volume.medium.price}")
    private double mediumParcelPrice;
    @Value("${app.parcel.volume.large.price}")
    private double largeParcelPrice;

    private final WebClient voucherApiWebClient;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    /**
     * @param packageInfo Package Information on which cost will be calculated
     * @param voucherCode voucher code for discounts
     * @return Calculated Package cost, given the parameters.
     */
    public Mono<PackageCostResponse> calculatePackageCost(PackageInfo packageInfo, String voucherCode) {
        return buildCostResponseWithDiscount(packageInfo, voucherCode)
                .map(costResponse -> {
                    double cost = calculateCostByWeight(packageInfo);
                    if (cost <= 0.0) {
                        cost = calculateCostByVolume(packageInfo);
                    }
                    double discountMultiplier = 1.00 - costResponse.getDiscount();
                    BigDecimal bd = new BigDecimal(cost * discountMultiplier).setScale(2, RoundingMode.HALF_UP);
                    costResponse.setCost(bd.doubleValue());
                    return costResponse;
                });
    }

    /**
     * @param packageInfo Information on which cost will be calculated
     * @return 0.0 if package does not exceed 10kg
     * @throws PackageRejectedException if package is over weight
     */
    private double calculateCostByWeight(PackageInfo packageInfo) throws PackageRejectedException {
        if (packageInfo.getWeight() > heavyParcelWeight) {
            return heavyParcelPrice * packageInfo.getWeight();
        }
        return 0.0;
    }

    /**
     * @param packageInfo Information on which cost will be calculated
     * @return price per volume
     */
    private double calculateCostByVolume(PackageInfo packageInfo) {
        if (packageInfo.getVolume() < smallParcelVolume) {
            return smallParcelPrice * packageInfo.getVolume();
        } else if (packageInfo.getVolume() < mediumParcelVolume) {
            return mediumParcelPrice * packageInfo.getVolume();
        } else {
            return largeParcelPrice * packageInfo.getVolume();
        }
    }

    /**
     * Initially builds the Cost Response with discounts.
     * if there is an error with Voucher API discount is returned as 0.0 with error message
     *
     * @param packageInfo Package information
     * @param voucherCode Voucher code to be used
     * @return PackageCostResponse with discount
     */
    private Mono<PackageCostResponse> buildCostResponseWithDiscount(PackageInfo packageInfo, String voucherCode) {
        PackageCostResponse costResponse = new PackageCostResponse(packageInfo);
        if (!StringUtils.hasLength(voucherCode)) {
            return Mono.just(costResponse);
        }
        return voucherApiWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/voucher/{code}")
                        /*
                         * according to the API Swagger, key is passed as a queryParameter
                         * This should be changed on the Voucher API side, key should be passed in the header instead.
                         */
                        .queryParam("key", voucherApiKey)
                        .build(voucherCode)
                )
                .retrieve()
                .bodyToMono(VoucherApiResponse.class)
                .map(voucherResponse -> {
                    if (voucherResponse.isExpired()) {
                        throw new InvalidVoucherException("Voucher is expired");
                    }
                    costResponse.setDiscount(voucherResponse.getDiscount());
                    return costResponse;
                })
                .onErrorResume(throwable -> {
                            costResponse.setDiscount(0.0);
                            costResponse.setError(throwable.getMessage());
                            return Mono.just(costResponse);
                        }
                );
    }

}


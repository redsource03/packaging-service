package com.globe.gcash.packaging.controller;

import com.globe.gcash.packaging.exception.RequestValidationException;
import com.globe.gcash.packaging.model.PackageInfo;
import com.globe.gcash.packaging.model.response.PackageCostResponse;
import com.globe.gcash.packaging.service.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/package")
@RequiredArgsConstructor
public class PackageController {
    private final PackageService packageService;

    @GetMapping(path = "/cost")
    public Mono<PackageCostResponse> getCostEstimate(@Validated PackageInfo packageInfo,
                                                     @RequestParam(value = "voucher", required = false) String voucher) {
        if(!packageInfo.isValid()) { //Simple check for volume, if parameters for L W H is not present, default value is 0.0
            throw new RequestValidationException("Invalid request, please refer to API documentation.");
        }
        return packageService.calculatePackageCost(packageInfo, voucher);
    }
}

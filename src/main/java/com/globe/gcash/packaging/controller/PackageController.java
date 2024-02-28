package com.globe.gcash.packaging.controller;

import com.globe.gcash.packaging.model.PackageInfo;
import com.globe.gcash.packaging.model.response.GenericErrorResponse;
import com.globe.gcash.packaging.model.response.PackageCostResponse;
import com.globe.gcash.packaging.service.PackageService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content =
                    {@Content(mediaType = "application/json", schema =
                    @Schema(implementation = PackageInfo.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid Request parameters, if L,W,H, Weight is Zero", content =
                    {@Content(mediaType = "application/json", schema =
                    @Schema(implementation = GenericErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content =
                    {@Content(mediaType = "application/json", schema =
                    @Schema(implementation = GenericErrorResponse.class))})})

    @GetMapping(path = "/cost")
    public Mono<PackageCostResponse> getCostEstimate(@Validated PackageInfo packageInfo,
                                                     @RequestParam(value = "voucher", required = false) String voucher) {
        return packageService.calculatePackageCost(packageInfo, voucher);
    }
}

package com.globe.gcash.packaging.model.response;

import com.globe.gcash.packaging.exception.InvalidVoucherException;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Data
public class VoucherApiResponse {
    private String code;
    private String expiry;
    private double discount;

    public boolean isExpired() {
        Optional.ofNullable(expiry).orElseThrow(() -> new InvalidVoucherException("Invalid voucher expiry date"));
        return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(expiry))
                .isBefore(LocalDate.now());
    }

    /**
     * Assumption API will return values i.e (if its 20% discount -> .20)
     * @return Multiplier for the discount;
     */
    public double getDiscountMultiplier() {
        return 1.00-discount;
    }
}

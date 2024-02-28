package com.globe.gcash.packaging.model.response;

import com.globe.gcash.packaging.model.PackageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PackageCostResponse extends PackageInfo {
    public PackageCostResponse() {
        super();
    }
    public PackageCostResponse(PackageInfo info) {
        this.setLength(info.getLength());
        this.setWidth(info.getWidth());
        this.setHeight(info.getHeight());
        this.setWeight(info.getWeight());
    }
    private double cost;
    private String error;
    private double discount;

}

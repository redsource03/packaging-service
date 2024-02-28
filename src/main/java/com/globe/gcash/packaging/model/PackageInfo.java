package com.globe.gcash.packaging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PackageInfo {
    public PackageInfo () {}
    public PackageInfo (double l, double w,double h, double weight) {
        this.length = l;
        this.width = w;
        this.height = h;
        this.weight = weight;
    }
    @Min(1)
    private double length;
    @Min(1)
    private double width;
    @Min(1)
    private double height;
    @Max(50)
    @Min(1)
    private double weight;
    @JsonIgnore
    public double getVolume() {
        return length*width*height;
    }
    @JsonIgnore
    public boolean isValid() {
        return getVolume() != 0.0 ;
    }
}

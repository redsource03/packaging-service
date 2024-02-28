package com.globe.gcash.packaging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
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
    private double length;
    private double width;
    private double height;
    @Max(50)
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

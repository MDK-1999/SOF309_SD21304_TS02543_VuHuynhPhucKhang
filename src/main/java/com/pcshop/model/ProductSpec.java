package com.pcshop.model;

public final class ProductSpec {
    private final String label;
    private final String value;

    public ProductSpec(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public String getValue() { return value; }
}

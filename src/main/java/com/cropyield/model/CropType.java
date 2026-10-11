package com.cropyield.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable domain model representing a type of crop.
 */
public final class CropType {
    private final int cropId;
    private final String cropName;
    private final BigDecimal assumedYieldPerHectare;
    private final boolean active;

    public CropType(int cropId, String cropName, BigDecimal assumedYieldPerHectare, boolean active) {
        this.cropId = cropId;
        this.cropName = Objects.requireNonNull(cropName, "cropName must not be null");
        this.assumedYieldPerHectare = Objects.requireNonNull(assumedYieldPerHectare, "assumedYieldPerHectare must not be null");
        this.active = active;
    }

    public int getCropId() {
        return cropId;
    }

    public String getCropName() {
        return cropName;
    }

    public BigDecimal getAssumedYieldPerHectare() {
        return assumedYieldPerHectare;
    }

    public boolean isActive() {
        return active;
    }
}

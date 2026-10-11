package com.cropyield.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable domain model representing a single crop record (a farmer's input).
 */
public final class CropRecord {
    private final long id; // primary key, may be 0 for unsaved objects
    private final String farmerName;
    private final int cropId;
    private final String cropName; // denormalized for convenience
    private final BigDecimal landArea; // hectares, DECIMAL(12,3)
    private final BigDecimal rainfall; // mm, DECIMAL(10,2)
    private final BigDecimal temperature; // °C, DECIMAL(5,2)
    private final String soilType;
    private final BigDecimal fertilizer; // kg, DECIMAL(12,2)
    private final BigDecimal predictedYield; // kg, DECIMAL(14,2)
    private final LocalDateTime createdAt;

    public CropRecord(long id,
                      String farmerName,
                      int cropId,
                      String cropName,
                      BigDecimal landArea,
                      BigDecimal rainfall,
                      BigDecimal temperature,
                      String soilType,
                      BigDecimal fertilizer,
                      BigDecimal predictedYield,
                      LocalDateTime createdAt) {
        this.id = id;
        this.farmerName = Objects.requireNonNull(farmerName, "farmerName must not be null");
        this.cropId = cropId;
        this.cropName = Objects.requireNonNull(cropName, "cropName must not be null");
        this.landArea = Objects.requireNonNull(landArea, "landArea must not be null");
        this.rainfall = Objects.requireNonNull(rainfall, "rainfall must not be null");
        this.temperature = Objects.requireNonNull(temperature, "temperature must not be null");
        this.soilType = Objects.requireNonNull(soilType, "soilType must not be null");
        this.fertilizer = Objects.requireNonNull(fertilizer, "fertilizer must not be null");
        this.predictedYield = Objects.requireNonNull(predictedYield, "predictedYield must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public long getId() {
        return id;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public int getCropId() {
        return cropId;
    }

    public String getCropName() {
        return cropName;
    }

    public BigDecimal getLandArea() {
        return landArea;
    }

    public BigDecimal getRainfall() {
        return rainfall;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public String getSoilType() {
        return soilType;
    }

    public BigDecimal getFertilizer() {
        return fertilizer;
    }

    public BigDecimal getPredictedYield() {
        return predictedYield;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

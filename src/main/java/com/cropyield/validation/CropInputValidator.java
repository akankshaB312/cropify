package com.cropyield.validation;

import com.cropyield.exception.ValidationException;
import com.cropyield.model.CropRecord;
import java.math.BigDecimal;

/**
 * Utility class that validates incoming data before it is persisted.
 * All validation rules are defined in the project requirements.
 * Validation failures result in {@link ValidationException} with a user‑safe message.
 */
public final class CropInputValidator {

    /** Default temperature bounds for demonstration purposes (°C). */
    public static final BigDecimal MIN_TEMPERATURE = BigDecimal.valueOf(-10); // e.g., very cold
    public static final BigDecimal MAX_TEMPERATURE = BigDecimal.valueOf(50);  // e.g., very hot

    private CropInputValidator() {
        // utility class – no instances
    }

    /**
     * Validates a {@link CropRecord} instance. The record may be partially populated
     * (e.g., id = 0 for a new record) but all fields required for persistence must be non‑null
     * and satisfy the domain constraints.
     *
     * @param record the record to validate
     * @throws ValidationException if any rule is violated
     */
    public static void validate(CropRecord record) {
        if (record == null) {
            throw new ValidationException("Crop record must not be null.");
        }
        // farmer name – not blank, max length 100 (DB column)
        String farmer = record.getFarmerName();
        if (farmer == null || farmer.isBlank()) {
            throw new ValidationException("Farmer name must not be blank.");
        }
        if (farmer.length() > 100) {
            throw new ValidationException("Farmer name exceeds maximum length of 100 characters.");
        }

        // cropId – positive
        if (record.getCropId() <= 0) {
            throw new ValidationException("Crop ID must be a positive integer.");
        }

        // land area – > 0, precision 12,3 (we just check >0)
        BigDecimal land = record.getLandArea();
        if (land == null) {
            throw new ValidationException("Land area must not be null.");
        }
        if (land.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Land area must be greater than zero.");
        }
        // rainfall – non‑negative
        BigDecimal rain = record.getRainfall();
        if (rain == null) {
            throw new ValidationException("Rainfall must not be null.");
        }
        if (rain.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Rainfall must be non‑negative.");
        }
        // temperature – within range
        BigDecimal temp = record.getTemperature();
        if (temp == null) {
            throw new ValidationException("Temperature must not be null.");
        }
        if (temp.compareTo(MIN_TEMPERATURE) < 0 || temp.compareTo(MAX_TEMPERATURE) > 0) {
            throw new ValidationException(String.format("Temperature %.2f°C is out of plausible range (%.2f‑%.2f°C).",
                    temp, MIN_TEMPERATURE, MAX_TEMPERATURE));
        }
        // soil type – not blank, max length 50
        String soil = record.getSoilType();
        if (soil == null || soil.isBlank()) {
            throw new ValidationException("Soil type must not be blank.");
        }
        if (soil.length() > 50) {
            throw new ValidationException("Soil type exceeds maximum length of 50 characters.");
        }
        // fertilizer – non‑negative
        BigDecimal fert = record.getFertilizer();
        if (fert == null) {
            throw new ValidationException("Fertilizer amount must not be null.");
        }
        if (fert.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Fertilizer amount must be non‑negative.");
        }
        // predictedYield – non‑negative (the algorithm will compute it, but we still protect)
        BigDecimal yield = record.getPredictedYield();
        if (yield == null) {
            throw new ValidationException("Predicted yield must not be null.");
        }
        if (yield.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Predicted yield must be non‑negative.");
        }
        // No further checks – IDs, timestamps handled elsewhere.
    }
}

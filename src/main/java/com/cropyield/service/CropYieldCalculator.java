package com.cropyield.service;

import com.cropyield.exception.ValidationException;
import com.cropyield.model.CropRecord;
import com.cropyield.model.CropType;
import com.cropyield.validation.CropInputValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simple deterministic calculator for predicted crop yield.
 *
 * <p>Formula (educational, not agronomically accurate):
 * <pre>
 * predictedYieldKg = landAreaHectares × assumedYieldPerHectareKg
 * </pre>
 *
 * The method validates the supplied {@link CropRecord} using the existing
 * {@link CropInputValidator} and also checks the {@link CropType} for activity
 * and a positive assumed yield.
 *
 * All arithmetic is performed with {@link BigDecimal} to preserve the precision
 * defined by the database schema (DECIMAL columns). The result is rounded to
 * two decimal places (matching the {@code predicted_yield} column definition
 * DECIMAL(14,2)).
 */
public final class CropYieldCalculator {

    private static final int SCALE = 2; // matches DECIMAL(14,2)
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private CropYieldCalculator() {
        // utility class – prevent instantiation
    }

    /**
     * Calculates the predicted yield in kilograms using the baseline formula.
     *
     * @param record   the {@link CropRecord} containing the land area and other input data
     * @param cropType the {@link CropType} providing the assumed yield per hectare
     * @return predicted yield (kg) rounded to two decimal places
     * @throws ValidationException if any validation rule is violated
     */
    public static BigDecimal calculate(CropRecord record, CropType cropType) {
        // Validate the record according to existing rules
        CropInputValidator.validate(record);

        if (cropType == null) {
            throw new ValidationException("Crop type must not be null.");
        }
        if (!cropType.isActive()) {
            throw new ValidationException("Crop type is inactive and cannot be used for calculation.");
        }
        BigDecimal assumedYield = cropType.getAssumedYieldPerHectare();
        if (assumedYield == null) {
            throw new ValidationException("Crop type must have a non‑null assumed yield per hectare.");
        }
        if (assumedYield.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Assumed yield per hectare must be positive.");
        }

        BigDecimal landArea = record.getLandArea();
        // landArea validation already ensures >0 in CropInputValidator
        BigDecimal result = landArea.multiply(assumedYield);
        return result.setScale(SCALE, ROUNDING);
    }

    /**
     * Helper to convert kilograms to tonnes (1 tonne = 1000 kg).
     * The result is rounded to two decimal places for consistency with the
     * database column precision.
     */
    public static BigDecimal kilogramsToTonnes(BigDecimal kilograms) {
        if (kilograms == null) {
            throw new ValidationException("Kilograms value must not be null.");
        }
        return kilograms.divide(BigDecimal.valueOf(1000), SCALE, ROUNDING);
    }
}

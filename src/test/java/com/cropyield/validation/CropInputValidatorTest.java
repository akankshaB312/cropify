package com.cropyield.validation;

import com.cropyield.exception.ValidationException;
import com.cropyield.model.CropRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CropInputValidator}.
 * These tests verify that valid data passes and each validation rule
 * triggers a {@link ValidationException} with an appropriate message.
 */
public class CropInputValidatorTest {

    private CropRecord validRecord() {
        return new CropRecord(
                0L,
                "John Doe",
                1,
                "Wheat",
                new BigDecimal("2.5"),
                new BigDecimal("120.0"),
                new BigDecimal("22.5"),
                "Loam",
                new BigDecimal("50"),
                new BigDecimal("7500"),
                LocalDateTime.now()
        );
    }

    @Test
    public void testValidRecordPasses() {
        assertDoesNotThrow(() -> CropInputValidator.validate(validRecord()));
    }

    @Test
    public void testBlankFarmerName() {
        CropRecord rec = new CropRecord(
                0L, "   ", 1, "Wheat",
                new BigDecimal("1"), new BigDecimal("100"), new BigDecimal("20"),
                "Silt", new BigDecimal("10"), new BigDecimal("5000"), LocalDateTime.now());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Farmer name must not be blank"));
    }

    @Test
    public void testFarmerNameTooLong() {
        String longName = "A".repeat(101);
        CropRecord rec = new CropRecord(
                0L, longName, 1, "Wheat",
                new BigDecimal("1"), new BigDecimal("100"), new BigDecimal("20"),
                "Silt", new BigDecimal("10"), new BigDecimal("5000"), LocalDateTime.now());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("exceeds maximum length"));
    }

    @Test
    public void testNegativeCropId() {
        CropRecord base = validRecord();
        CropRecord rec = new CropRecord(
                base.getId(), base.getFarmerName(), -5, base.getCropName(),
                base.getLandArea(), base.getRainfall(), base.getTemperature(),
                base.getSoilType(), base.getFertilizer(), base.getPredictedYield(), base.getCreatedAt());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Crop ID must be a positive integer"));
    }

    @Test
    public void testZeroLandArea() {
        CropRecord base = validRecord();
        CropRecord rec = new CropRecord(
                base.getId(), base.getFarmerName(), base.getCropId(), base.getCropName(),
                BigDecimal.ZERO, base.getRainfall(), base.getTemperature(),
                base.getSoilType(), base.getFertilizer(), base.getPredictedYield(), base.getCreatedAt());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Land area must be greater than zero"));
    }

    @Test
    public void testNegativeRainfall() {
        CropRecord base = validRecord();
        CropRecord rec = new CropRecord(
                base.getId(), base.getFarmerName(), base.getCropId(), base.getCropName(),
                base.getLandArea(), new BigDecimal("-5"), base.getTemperature(),
                base.getSoilType(), base.getFertilizer(), base.getPredictedYield(), base.getCreatedAt());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Rainfall must be non‑negative"));
    }

    @Test
    public void testTemperatureOutOfRange() {
        CropRecord base = validRecord();
        CropRecord rec = new CropRecord(
                base.getId(), base.getFarmerName(), base.getCropId(), base.getCropName(),
                base.getLandArea(), base.getRainfall(), new BigDecimal("60"),
                base.getSoilType(), base.getFertilizer(), base.getPredictedYield(), base.getCreatedAt());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Temperature"));
    }

    @Test
    public void testBlankSoilType() {
        CropRecord rec = new CropRecord(
                0L, "John", 1, "Wheat",
                new BigDecimal("1"), new BigDecimal("100"), new BigDecimal("20"),
                "   ", new BigDecimal("10"), new BigDecimal("5000"), LocalDateTime.now());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Soil type must not be blank"));
    }

    @Test
    public void testNegativeFertilizer() {
        CropRecord base = validRecord();
        CropRecord rec = new CropRecord(
                base.getId(), base.getFarmerName(), base.getCropId(), base.getCropName(),
                base.getLandArea(), base.getRainfall(), base.getTemperature(),
                base.getSoilType(), new BigDecimal("-1"), base.getPredictedYield(), base.getCreatedAt());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Fertilizer amount must be non‑negative"));
    }

    @Test
    public void testNegativePredictedYield() {
        CropRecord base = validRecord();
        CropRecord rec = new CropRecord(
                base.getId(), base.getFarmerName(), base.getCropId(), base.getCropName(),
                base.getLandArea(), base.getRainfall(), base.getTemperature(),
                base.getSoilType(), base.getFertilizer(), new BigDecimal("-100"), base.getCreatedAt());
        ValidationException ex = assertThrows(ValidationException.class, () -> CropInputValidator.validate(rec));
        assertTrue(ex.getMessage().contains("Predicted yield must be non‑negative"));
    }
}

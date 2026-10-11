package com.cropyield.service;

import com.cropyield.exception.ValidationException;
import com.cropyield.model.CropRecord;
import com.cropyield.model.CropType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CropYieldCalculator}.
 *
 * The baseline formula is:
 * <pre>
 * predictedYieldKg = landAreaHectares × assumedYieldPerHectareKg
 * </pre>
 * Results are rounded to two decimal places (scale = 2, HALF_UP).
 */
public class CropYieldCalculatorTest {

    private CropRecord validRecord(BigDecimal landArea) {
        return new CropRecord(
                0L,
                "John Doe",
                1,
                "Wheat",
                landArea,
                new BigDecimal("100"), // rainfall (unused)
                new BigDecimal("20"),   // temperature (unused)
                "Loam",
                new BigDecimal("50"),   // fertilizer (unused)
                BigDecimal.ZERO,          // predictedYield placeholder (will be calculated)
                LocalDateTime.now()
        );
    }

    private CropType activeCrop(BigDecimal assumedYield) {
        return new CropType(1, "Wheat", assumedYield, true);
    }

    @Test
    public void testWheatTwoHectares() {
        CropRecord rec = validRecord(new BigDecimal("2"));
        CropType ct = activeCrop(new BigDecimal("3000"));
        BigDecimal result = CropYieldCalculator.calculate(rec, ct);
        assertEquals(new BigDecimal("6000.00"), result);
    }

    @Test
    public void testRiceDecimalLand() {
        CropRecord rec = validRecord(new BigDecimal("1.234"));
        CropType ct = new CropType(2, "Rice", new BigDecimal("4000"), true);
        BigDecimal result = CropYieldCalculator.calculate(rec, ct);
        // 1.234 * 4000 = 4936.0 -> rounded to 2 decimal places = 4936.00
        assertEquals(new BigDecimal("4936.00"), result);
    }

    @Test
    public void testMaizeRounding() {
        CropRecord rec = validRecord(new BigDecimal("0.3333"));
        CropType ct = new CropType(3, "Maize", new BigDecimal("3500"), true);
        BigDecimal result = CropYieldCalculator.calculate(rec, ct);
        // 0.3333 * 3500 = 1166.55 ; already two decimals, expect 1166.55
        assertEquals(new BigDecimal("1166.55"), result);
    }

    @Test
    public void testInactiveCrop() {
        CropRecord rec = validRecord(new BigDecimal("1"));
        CropType ct = new CropType(4, "Wheat", new BigDecimal("3000"), false);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CropYieldCalculator.calculate(rec, ct));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    public void testZeroYieldRate() {
        CropRecord rec = validRecord(new BigDecimal("1"));
        CropType ct = new CropType(5, "Wheat", BigDecimal.ZERO, true);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CropYieldCalculator.calculate(rec, ct));
        assertTrue(ex.getMessage().contains("positive"));
    }

    @Test
    public void testNegativeYieldRate() {
        CropRecord rec = validRecord(new BigDecimal("1"));
        CropType ct = new CropType(6, "Wheat", new BigDecimal("-100"), true);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CropYieldCalculator.calculate(rec, ct));
        assertTrue(ex.getMessage().contains("positive"));
    }

    @Test
    public void testNullCropType() {
        CropRecord rec = validRecord(new BigDecimal("1"));
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CropYieldCalculator.calculate(rec, null));
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    public void testKilogramsToTonnesConversion() {
        BigDecimal kg = new BigDecimal("2500");
        BigDecimal tonnes = CropYieldCalculator.kilogramsToTonnes(kg);
        assertEquals(new BigDecimal("2.50"), tonnes);
    }

    @Test
    public void testKilogramsToTonnesNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> CropYieldCalculator.kilogramsToTonnes(null));
        assertTrue(ex.getMessage().contains("must not be null"));
    }
}

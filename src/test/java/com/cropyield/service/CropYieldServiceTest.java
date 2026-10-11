package com.cropyield.service;

import com.cropyield.dao.CropRecordDAO;
import com.cropyield.dao.CropTypeDAO;
import com.cropyield.exception.ServiceException;
import com.cropyield.exception.ValidationException;
import com.cropyield.model.CropRecord;
import com.cropyield.model.CropType;
import com.cropyield.validation.CropInputValidator;
import com.cropyield.service.CropYieldCalculator; // ensure import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit tests for {@link CropYieldService}. All database interactions are mocked
 * using {@code Mockito.mockStatic} (requires the {@code mockito-inline} dependency).
 */
@ExtendWith(MockitoExtension.class)
public class CropYieldServiceTest {

    private static final CropRecord VALID_INPUT = new CropRecord(
            0L,
            "John Farmer",
            1,
            null, // cropName will be set after calculation
            new BigDecimal("2.0"),
            new BigDecimal("100"),
            new BigDecimal("22"),
            "Loam",
            new BigDecimal("30"),
            null, // predictedYield will be calculated
            LocalDateTime.now()
    );

    private static final CropType ACTIVE_CROP = new CropType(
            1,
            "Wheat",
            new BigDecimal("3000"), // kg/ha
            true
    );

    @BeforeEach
    void resetMocks() {
        // Ensure any previous static mocks are cleared
        Mockito.clearAllCaches();
    }

    @Test
    void calculateAndSave_successful() throws Exception {
        // Mock validator – does nothing (no exception)
        try (MockedStatic<CropInputValidator> validatorMock = Mockito.mockStatic(CropInputValidator.class);
             MockedStatic<CropTypeDAO> typeDaoMock = Mockito.mockStatic(CropTypeDAO.class);
             MockedStatic<CropYieldCalculator> calcMock = Mockito.mockStatic(CropYieldCalculator.class);
             MockedStatic<CropRecordDAO> recordDaoMock = Mockito.mockStatic(CropRecordDAO.class)) {

            validatorMock.when(() -> CropInputValidator.validate(any(CropRecord.class))).thenAnswer(invocation -> null);
            typeDaoMock.when(() -> CropTypeDAO.findById(eq(1))).thenReturn(Optional.of(ACTIVE_CROP));
            // Expected yield = landArea * assumedYield = 2.0 * 3000 = 6000 kg, rounded scale 2
            BigDecimal expectedYield = new BigDecimal("6000.00");
            calcMock.when(() -> CropYieldCalculator.calculate(any(CropRecord.class), eq(ACTIVE_CROP))).thenReturn(expectedYield);
            recordDaoMock.when(() -> CropRecordDAO.insert(any(CropRecord.class))).thenReturn(42L);

            CropRecord result = CropYieldService.calculateAndSave(VALID_INPUT);

            assertEquals(42L, result.getId());
            assertEquals(expectedYield, result.getPredictedYield());
            assertEquals("Wheat", result.getCropName());
        }
    }

    @Test
    void calculateAndSave_invalidInput() {
        try (MockedStatic<CropInputValidator> validatorMock = Mockito.mockStatic(CropInputValidator.class)) {
            validatorMock.when(() -> CropInputValidator.validate(any(CropRecord.class)))
                    .thenThrow(new ValidationException("Farmer name blank"));

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> CropYieldService.calculateAndSave(VALID_INPUT));
            assertEquals("Farmer name blank", ex.getMessage());
        }
    }

    @Test
    void calculateAndSave_cropNotFound() {
        try (MockedStatic<CropInputValidator> validatorMock = Mockito.mockStatic(CropInputValidator.class);
             MockedStatic<CropTypeDAO> typeDaoMock = Mockito.mockStatic(CropTypeDAO.class)) {
            validatorMock.when(() -> CropInputValidator.validate(any(CropRecord.class))).thenAnswer(i -> null);
            typeDaoMock.when(() -> CropTypeDAO.findById(eq(1))).thenReturn(Optional.empty());

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> CropYieldService.calculateAndSave(VALID_INPUT));
            assertTrue(ex.getMessage().contains("Crop type not found"));
        }
    }

    @Test
    void calculateAndSave_inactiveCrop() {
        CropType inactive = new CropType(1, "Wheat", new BigDecimal("3000"), false);
        try (MockedStatic<CropInputValidator> validatorMock = Mockito.mockStatic(CropInputValidator.class);
             MockedStatic<CropTypeDAO> typeDaoMock = Mockito.mockStatic(CropTypeDAO.class)) {
            validatorMock.when(() -> CropInputValidator.validate(any(CropRecord.class))).thenAnswer(i -> null);
            typeDaoMock.when(() -> CropTypeDAO.findById(eq(1))).thenReturn(Optional.of(inactive));

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> CropYieldService.calculateAndSave(VALID_INPUT));
            assertTrue(ex.getMessage().contains("inactive"));
        }
    }

    @Test
    void calculateAndSave_dbFailure() {
        try (MockedStatic<CropInputValidator> validatorMock = Mockito.mockStatic(CropInputValidator.class);
             MockedStatic<CropTypeDAO> typeDaoMock = Mockito.mockStatic(CropTypeDAO.class);
             MockedStatic<CropYieldCalculator> calcMock = Mockito.mockStatic(CropYieldCalculator.class);
             MockedStatic<CropRecordDAO> recordDaoMock = Mockito.mockStatic(CropRecordDAO.class)) {

            validatorMock.when(() -> CropInputValidator.validate(any(CropRecord.class))).thenAnswer(i -> null);
            typeDaoMock.when(() -> CropTypeDAO.findById(eq(1))).thenReturn(Optional.of(ACTIVE_CROP));
            BigDecimal expectedYield = new BigDecimal("6000.00");
            calcMock.when(() -> CropYieldCalculator.calculate(any(CropRecord.class), eq(ACTIVE_CROP))).thenReturn(expectedYield);
            recordDaoMock.when(() -> CropRecordDAO.insert(any(CropRecord.class)))
                    .thenThrow(new java.sql.SQLException("DB down"));

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> CropYieldService.calculateAndSave(VALID_INPUT));
            assertTrue(ex.getMessage().contains("Failed to save crop prediction"));
            assertNotNull(ex.getCause());
        }
    }
}

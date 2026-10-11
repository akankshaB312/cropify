package com.cropyield.service;

import com.cropyield.dao.CropRecordDAO;
import com.cropyield.dao.CropTypeDAO;
import com.cropyield.exception.ServiceException;
import com.cropyield.exception.ValidationException;
import com.cropyield.model.CropRecord;
import com.cropyield.model.CropType;
import com.cropyield.validation.CropInputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Business service layer that coordinates validation, calculation and persistence.
 *
 * This class provides a small, stable API for the future UI layer. All methods are
 * static for simplicity in this educational project, but could be refactored to
 * instance methods with dependency injection in a real application.
 */
public final class CropYieldService {

    private CropYieldService() {
        // utility class – prevent instantiation
    }

    /**
     * Returns all active crop types.
     *
     * @return list of {@link CropType} where {@code active} is {@code true}
     * @throws ServiceException if a database error occurs
     */
    public static List<CropType> listActiveCropTypes() {
        try {
            return CropTypeDAO.listActive();
        } catch (Exception e) {
            throw new ServiceException("Failed to list active crop types", e);
        }
    }

    /**
     * Retrieves a crop type by its identifier.
     *
     * @param id crop type primary key
     * @return {@link Optional} containing the {@link CropType} if found
     * @throws ServiceException on database failure
     */
    public static Optional<CropType> getCropTypeById(int id) {
        try {
            return CropTypeDAO.findById(id);
        } catch (Exception e) {
            throw new ServiceException("Failed to load crop type with id=" + id, e);
        }
    }

    /**
     * Retrieves all saved {@link CropRecord}s, newest first.
     */
    public static List<CropRecord> getAllRecords() {
        try {
            return CropRecordDAO.listAll();
        } catch (Exception e) {
            throw new ServiceException("Failed to retrieve crop records", e);
        }
    }

    /**
     * Retrieves a single record by its generated id.
     */
    public static Optional<CropRecord> getRecordById(long id) {
        try {
            return CropRecordDAO.findById(id);
        } catch (Exception e) {
            throw new ServiceException("Failed to retrieve crop record id=" + id, e);
        }
    }

    /**
     * Searches records by farmer name (case‑insensitive, contains).
     */
    public static List<CropRecord> searchRecordsByFarmerName(String term) {
        try {
            return CropRecordDAO.searchByFarmerName(term);
        } catch (Exception e) {
            throw new ServiceException("Failed to search crop records by farmer name", e);
        }
    }

    /**
     * Searches records by crop identifier.
     */
    public static List<CropRecord> findRecordsByCropId(int cropId) {
        try {
            return CropRecordDAO.findByCropId(cropId);
        } catch (Exception e) {
            throw new ServiceException("Failed to find records for cropId=" + cropId, e);
        }
    }

    /**
     * Validates the input, loads the crop, calculates the predicted yield and
     * persists the {@link CropRecord}. The returned instance contains the generated
     * database id and the calculated {@code predictedYield}.
     *
     * @param input a {@link CropRecord} populated with user input. {@code id} and
     *              {@code createdAt} are ignored – they will be generated.
     * @return the persisted {@link CropRecord} with its generated id
     * @throws ValidationException if input data is invalid or the selected crop
     *                             does not exist / is inactive
     * @throws ServiceException    for any database‑related failure
     */
    public static CropRecord calculateAndSave(CropRecord input) {
        // 1. Validate incoming data (business rules)
        CropInputValidator.validate(input);

        // 2. Load the crop type from DB
        CropType crop = getCropTypeById(input.getCropId())
                .orElseThrow(() -> new ValidationException("Crop type not found for id=" + input.getCropId()));

        // 3. Reject inactive crops
        if (!crop.isActive()) {
            throw new ValidationException("Selected crop type is inactive: " + crop.getCropName());
        }

        // 4. Calculate predicted yield using the service‑level calculator
        BigDecimal predictedYield = CropYieldCalculator.calculate(input, crop);

        // 5. Build a new record that includes the calculated yield and timestamp
        CropRecord toPersist = new CropRecord(
                0L, // id will be generated
                input.getFarmerName(),
                input.getCropId(),
                crop.getCropName(),
                input.getLandArea(),
                input.getRainfall(),
                input.getTemperature(),
                input.getSoilType(),
                input.getFertilizer(),
                predictedYield,
                java.time.LocalDateTime.now()
        );

        // 6. Persist and obtain the generated id
        try {
            long generatedId = CropRecordDAO.insert(toPersist);
            // 7. Return a record that includes the generated id
            return new CropRecord(
                    generatedId,
                    toPersist.getFarmerName(),
                    toPersist.getCropId(),
                    toPersist.getCropName(),
                    toPersist.getLandArea(),
                    toPersist.getRainfall(),
                    toPersist.getTemperature(),
                    toPersist.getSoilType(),
                    toPersist.getFertilizer(),
                    toPersist.getPredictedYield(),
                    toPersist.getCreatedAt()
            );
        } catch (Exception e) {
            throw new ServiceException("Failed to save crop prediction", e);
        }
    }
}

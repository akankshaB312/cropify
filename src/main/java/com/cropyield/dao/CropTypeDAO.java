package com.cropyield.dao;

import com.cropyield.model.CropType;
import com.cropyield.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for the {@link CropType} table.
 *
 * All methods use {@link DBConnection#getConnection()} and close resources via
 * try‑with‑resources. Queries use explicit column lists that match the
 * {@code crop_types} schema.
 */
public final class CropTypeDAO {

    private CropTypeDAO() {
        // utility class – prevent instantiation
    }

    /** Returns all active crop types ordered by name. */
    public static List<CropType> listActive() throws SQLException {
        String sql = "SELECT crop_id, crop_name, assumed_yield_per_hectare, active " +
                     "FROM crop_types WHERE active = TRUE ORDER BY crop_name";
        List<CropType> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /** Finds a crop type by its primary key. */
    public static Optional<CropType> findById(int id) throws SQLException {
        String sql = "SELECT crop_id, crop_name, assumed_yield_per_hectare, active " +
                     "FROM crop_types WHERE crop_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    /** Finds a crop type by its unique name (case‑sensitive). */
    public static Optional<CropType> findByName(String name) throws SQLException {
        String sql = "SELECT crop_id, crop_name, assumed_yield_per_hectare, active " +
                     "FROM crop_types WHERE crop_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    private static CropType mapRow(ResultSet rs) throws SQLException {
        int cropId = rs.getInt("crop_id");
        String cropName = rs.getString("crop_name");
        java.math.BigDecimal assumedYield = rs.getBigDecimal("assumed_yield_per_hectare");
        boolean active = rs.getBoolean("active");
        return new CropType(cropId, cropName, assumedYield, active);
    }
}

import java.util.HashMap;
import java.util.Map;

public class CropYieldCalculator {

    // Configurable base yield rates (in kg per acre) for supported crops
    private static final Map<String, Double> BASE_YIELD_RATES = new HashMap<>();

    static {
        BASE_YIELD_RATES.put("wheat", 1200.0);
        BASE_YIELD_RATES.put("rice", 1600.0);
        BASE_YIELD_RATES.put("maize", 2200.0);
    }

    /**
     * Data class to encapsulate the complete calculation result.
     */
    public static class YieldResult {
        private final String cropName;
        private final double landAreaAcres;
        private final double soilMultiplier;
        private final double yieldKg;
        private final double yieldTonnes;

        public YieldResult(String cropName, double landAreaAcres, double soilMultiplier, double yieldKg, double yieldTonnes) {
            this.cropName = cropName;
            this.landAreaAcres = landAreaAcres;
            this.soilMultiplier = soilMultiplier;
            this.yieldKg = yieldKg;
            this.yieldTonnes = yieldTonnes;
        }

        public String getCropName() { return cropName; }
        public double getLandAreaAcres() { return landAreaAcres; }
        public double getSoilMultiplier() { return soilMultiplier; }
        public double getYieldKg() { return yieldKg; }
        public double getYieldTonnes() { return yieldTonnes; }

        @Override
        public String toString() {
            return String.format("Crop: %s | Area: %.2f acres | Soil Factor: %.2f | Yield: %.2f kg (%.3f tonnes)",
                    cropName, landAreaAcres, soilMultiplier, yieldKg, yieldTonnes);
        }
    }

    /**
     * Core validation logic to guard against invalid inputs.
     */
    private void validateInputs(String cropName, double landArea, double soilMultiplier) {
        if (cropName == null || cropName.trim().isEmpty()) {
            throw new IllegalArgumentException("Crop name cannot be null or empty.");
        }
        
        String normalizedCrop = cropName.toLowerCase().trim();
        if (!BASE_YIELD_RATES.containsKey(normalizedCrop)) {
            throw new IllegalArgumentException("Unsupported crop: '" + cropName + "'. Choose from: " + BASE_YIELD_RATES.keySet());
        }

        if (landArea <= 0) {
            throw new IllegalArgumentException("Land area must be greater than zero. Provided: " + landArea);
        }

        if (soilMultiplier < 0.1 || soilMultiplier > 2.0) {
            throw new IllegalArgumentException("Soil multiplier must be between 0.1 and 2.0. Provided: " + soilMultiplier);
        }
    }

    /**
     * Basic prediction method matching your required signature (returns yield in kg).
     */
    public double predictYield(String cropName, double landArea) {
        // Defaults soil multiplier to 1.0 for the basic signature
        YieldResult result = calculateDetailedYield(cropName, landArea, 1.0);
        return result.getYieldKg();
    }

    /**
     * Comprehensive prediction method taking environmental modifiers and returning a rich result object.
     */
    public YieldResult calculateDetailedYield(String cropName, double landArea, double soilMultiplier) {
        validateInputs(cropName, landArea, soilMultiplier);
        
        String normalizedCrop = cropName.toLowerCase().trim();
        double baseRate = BASE_YIELD_RATES.get(normalizedCrop);

        // Calculation formula: Area * Base Rate * Environmental Soil Modifier
        double totalKg = landArea * baseRate * soilMultiplier;
        double totalTonnes = totalKg / 1000.0;

        return new YieldResult(normalizedCrop, landArea, soilMultiplier, totalKg, totalTonnes);
    }

    /**
     * Allows updating or adding new base rates dynamically at runtime.
     */
    public static void updateBaseRate(String cropName, double newRate) {
        if (cropName != null && newRate > 0) {
            BASE_YIELD_RATES.put(cropName.toLowerCase().trim(), newRate);
        }
    }

    // ==========================================
    // Standalone Testing (Runs without MySQL)
    // ==========================================
    public static void main(String[] args) {
        CropYieldCalculator calculator = new CropYieldCalculator();

        System.out.println("--- Running Standalone Prediction Logic Tests ---\n");

        // Test 1: Basic method call
        try {
            double wheatKg = calculator.predictYield("Wheat", 5.0);
            System.out.println("Test 1 (Basic Wheat 5 acres): " + wheatKg + " kg");
        } catch (Exception e) {
            System.err.println("Test 1 Failed: " + e.getMessage());
        }

        // Test 2: Detailed method call with custom soil multiplier (rich soil)
        try {
            YieldResult riceResult = calculator.calculateDetailedYield("rice", 3.5, 1.2);
            System.out.println("Test 2 (Detailed Rice): " + riceResult);
        } catch (Exception e) {
            System.err.println("Test 2 Failed: " + e.getMessage());
        }

        // Test 3: Error handling for unsupported crop
        try {
        calculator.predictYield("barley", 2.0);        } catch (IllegalArgumentException e) {
            System.out.println("Test 3 (Caught Expected Error): " + e.getMessage());
        }

        // Test 4: Error handling for negative land area
        try {
            calculator.calculateDetailedYield("Maize", -1.0, 1.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Test 4 (Caught Expected Error): " + e.getMessage());
        }
    }
}
package flink.operators;

public class RuntimeStats {
    private double mean = 0.0;
    private double variance = 0.0;
    private long count = 0;
//    private long standardDeviation = 0;

    // Update statistics with a new observation
    public void update(long newValue) {
        count++;
        double delta = newValue - mean;
        mean += delta / count;
        double delta2 = newValue - mean;
        variance += delta * delta2; // This accumulates the sum of squares of differences from the current mean
    }

    // Check if a value is within 3 standard deviations of the mean
    public boolean isWithinBounds(long value) {
        if (count < 2) {
            // Not enough data to determine bounds
            return false;
        }
        double stdDev = Math.sqrt(variance / (count - 1));
        // System.out.println("stdDev " + stdDev);
        double lowerBound = mean - 3 * stdDev;
        double upperBound = mean + 3 * stdDev;

//        if (lowerBound > value || upperBound < value) {
//            System.out.println(value);
//            System.out.println(mean);
//            System.out.println(stdDev);
//            System.out.println(lowerBound + " " + upperBound);
//        }
        return !(value >= lowerBound) || !(value <= upperBound);
    }

//    // Getters and Setters
//    public double getMean() {
//        return mean;
//    }
//
//    public double getVariance() {
//        return variance;
//    }
//
//    public double getStandardDeviation() {
//        if (count < 2) return 0.0;
//        return Math.sqrt(variance / (count - 1));
//    }
//
//    public long getCount() {
//        return count;
//    }
}

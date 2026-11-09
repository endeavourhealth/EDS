package org.endeavourhealth.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class PercentageCalculator {

    private static final NumberFormat FORMATTER = new DecimalFormat("##.##");

    public static double calculatePercentage(double denominator, double numerator) {
        if (numerator > denominator) {
            throw new IllegalArgumentException("Numerator " + numerator + " cannot be larger that denominator " + denominator);
        }
        return (numerator * 100d) / denominator;
    }
    public static String calculatorPercentString(double denominator, double numerator) {

        double percentage = calculatePercentage(denominator, numerator);

        if (percentage == 0d) {
            return "0%";
        } else if (percentage == 100d) {
            return "100%";
        } else {

            //prevent any rounding to 0 or 100 by spotting these and adjusting the percentage to the next acceptable value
            if (percentage < 0.01d) {
                percentage = 0.01d;
            } else if (percentage > 99.99d) {
                percentage = 99.99d;
            }

            return FORMATTER.format(percentage) + "%";
        }

    }
}

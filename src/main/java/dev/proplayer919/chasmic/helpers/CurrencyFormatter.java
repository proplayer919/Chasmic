package dev.proplayer919.chasmic.helpers;

import lombok.experimental.UtilityClass;

/**
 * Helper class for formatting currency values with suffixes (k, m, b, t)
 */
@UtilityClass
public class CurrencyFormatter {

    /**
     * Formats a currency amount with appropriate suffix
     * Examples: 1234 -> "1.23k", 1234567 -> "1.23m", 1234567890 -> "1.23b"
     *
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(double amount) {
        if (amount < 1000) {
            // Less than 1k, show as is
            return String.format("%.0f", amount);
        } else if (amount < 1_000_000) {
            // 1k to 1m
            return String.format("%.2f", amount / 1000).replaceAll("0+$", "").replaceAll("\\.$", "") + "k";
        } else if (amount < 1_000_000_000) {
            // 1m to 1b
            return String.format("%.2f", amount / 1_000_000).replaceAll("0+$", "").replaceAll("\\.$", "") + "m";
        } else if (amount < 1_000_000_000_000L) {
            // 1b to 1t
            return String.format("%.2f", amount / 1_000_000_000).replaceAll("0+$", "").replaceAll("\\.$", "") + "b";
        } else {
            // 1t and above
            return String.format("%.2f", amount / 1_000_000_000_000L).replaceAll("0+$", "").replaceAll("\\.$", "") + "t";
        }
    }

    /**
     * Formats a currency amount with appropriate suffix (integer version)
     *
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(int amount) {
        return formatCurrency((double) amount);
    }

    /**
     * Formats a currency amount with appropriate suffix (long version)
     *
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(long amount) {
        return formatCurrency((double) amount);
    }
}


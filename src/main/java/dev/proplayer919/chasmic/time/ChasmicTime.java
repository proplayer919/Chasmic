package dev.proplayer919.chasmic.time;

import lombok.experimental.UtilityClass;

import java.time.Instant;

/**
 * Helper class for Chasmic Time calculations
 * Chasmic Time is based on real server time where:
 * - 1 real second = 1 Chasmic minute
 * - 1 Chasmic hour = 3,600 real seconds (60 minutes)
 * - 1 Chasmic day = 86,400 real seconds (1440 minutes / 24 hours)
 * - 1 Chasmic year = 8,640,000 real seconds (100 days) split into 4 seasons of 25 days each
 * Seasons: Spring (days 0-24), Summer (days 25-49), Autumn (days 50-74), Winter (days 75-99)
 * <p>
 * Time is calculated deterministically from a fixed epoch (January 1, 2024 00:00:00 UTC)
 */
@UtilityClass
public class ChasmicTime {

    // Fixed epoch: March 1, 2026 00:00:00 UTC
    private static final long EPOCH_SECONDS = Instant.parse("2026-03-01T00:00:00Z").getEpochSecond();

    private static final int CHASMIC_MINUTES_PER_HOUR = 60;
    private static final int CHASMIC_HOURS_PER_DAY = 24;
    private static final int CHASMIC_DAYS_PER_YEAR = 100;
    private static final int CHASMIC_DAYS_PER_SEASON = 25;

    /**
     * Gets the total elapsed Chasmic minutes since epoch
     */
    public static long getTotalChasmicMinutes() {
        long currentSeconds = Instant.now().getEpochSecond();
        return currentSeconds - EPOCH_SECONDS; // 1 real second = 1 Chasmic minute
    }

    /**
     * Gets the total elapsed Chasmic minutes since epoch with millisecond precision
     * Returns the value in milliseconds (divide by 1000 to get minutes)
     */
    public static long getTotalChasmicMinutesMillis() {
        long currentNanos = System.nanoTime();
        long epochNanos = EPOCH_SECONDS * 1_000_000_000L;
        return (currentNanos - epochNanos) / 1_000_000L; // Convert nanos to millis, 1 real ms = 1 Chasmic ms
    }

    /**
     * Gets the total elapsed Chasmic hours since epoch
     */
    public static long getTotalChasmicHours() {
        return getTotalChasmicMinutes() / CHASMIC_MINUTES_PER_HOUR;
    }

    /**
     * Gets the total elapsed Chasmic days since epoch
     */
    public static long getTotalChasmicDays() {
        return getTotalChasmicHours() / CHASMIC_HOURS_PER_DAY;
    }

    /**
     * Gets the current minute of the Chasmic day (0-59)
     */
    public static int getChasmicMinute() {
        long totalMinutes = getTotalChasmicMinutes();
        return (int) (totalMinutes % CHASMIC_MINUTES_PER_HOUR);
    }

    /**
     * Gets the current hour of the Chasmic day (0-23)
     */
    public static int getChasmicHour() {
        long totalHours = getTotalChasmicHours();
        return (int) (totalHours % CHASMIC_HOURS_PER_DAY);
    }

    /**
     * Gets the current day of the Chasmic year (0-99)
     */
    public static int getChasmicDayOfYear() {
        long totalDays = getTotalChasmicDays();
        return (int) (totalDays % CHASMIC_DAYS_PER_YEAR);
    }

    /**
     * Gets the current day of the Chasmic season (0-24)
     */
    public static int getChasmicDayOfSeason() {
        int dayOfYear = getChasmicDayOfYear();
        return dayOfYear % CHASMIC_DAYS_PER_SEASON;
    }

    /**
     * Gets the current Chasmic year (based on total days elapsed / 100)
     */
    public static long getChasmicYear() {
        long totalDays = getTotalChasmicDays();
        return totalDays / CHASMIC_DAYS_PER_YEAR;
    }

    /**
     * Gets the current season
     */
    public static ChasmicSeason getCurrentSeason() {
        return ChasmicSeason.fromDayOfYear(getChasmicDayOfYear());
    }

    /**
     * Gets the current season display name
     */
    public static String getCurrentSeasonName() {
        return getCurrentSeason().getDisplayName();
    }

    /**
     * Gets a formatted string of the current Chasmic time
     * Format: "HH:MM Year Y, Season Day D"
     */
    public static String getFormattedTime() {
        int hour = getChasmicHour();
        int minute = getChasmicMinute();
        int dayOfSeason = getChasmicDayOfSeason();
        long year = getChasmicYear();
        String seasonName = getCurrentSeasonName();

        return String.format("%02d:%02d Year %d, %s Day %d", hour, minute, year, seasonName, dayOfSeason + 1);
    }

    /**
     * Gets the current Minecraft instance time that smoothly interpolates with millisecond precision
     * Conversion: 1 Chasmic hour = 1000 Minecraft ticks
     * 1 Chasmic minute = 16.666... Minecraft ticks
     * 1 Chasmic millisecond = 0.016666... Minecraft ticks
     * <p>
     * Minecraft time is a 24000-tick cycle (0 = midnight, 6000 = noon, 12000 = midnight)
     * Chasmic time is 0-23 hours, 0-59 minutes, 0-999 milliseconds
     * <p>
     * Returns the interpolated Minecraft instance time
     */
    public static long getInterpolatedInstanceTime() {
        long totalChasmicMillis = getTotalChasmicMinutesMillis();

        // Extract hours, minutes, and milliseconds from total Chasmic time
        long totalChasmicSeconds = totalChasmicMillis / 1000;
        long chasmicHours = (totalChasmicSeconds / 3600) % 24;
        long chasmicMinutes = (totalChasmicSeconds / 60) % 60;
        long chasmicMillis = totalChasmicMillis % 1000;

        // Convert to Minecraft ticks with smooth interpolation
        // Each hour = 1000 ticks, each minute = 16.666... ticks, each millisecond = 0.016666... ticks
        double instanceTime = (chasmicHours * 1000.0) + (chasmicMinutes * 16.666666666) + (chasmicMillis * 0.016666666);

        // Clamp to 0-24000 range (24-hour Minecraft day cycle)
        return Math.round(instanceTime) % 24000;
    }

    /**
     * Gets a short formatted time string for display
     * Format: "HH:MM"
     */
    public static String getFormattedTimeShort() {
        int hour = getChasmicHour();
        int minute = getChasmicMinute();
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Gets the current season and day string
     * Format: "Season Day D, Year Y"
     */
    public static String getSeasonAndDayString() {
        int dayOfSeason = getChasmicDayOfSeason();
        long year = getChasmicYear();
        String seasonName = getCurrentSeasonName();
        return String.format("%s Day %d, Year %d", seasonName, dayOfSeason + 1, year);
    }
}


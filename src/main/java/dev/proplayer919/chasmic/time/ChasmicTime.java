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
    private static final long EPOCH_MILLISECONDS = Instant.parse("2026-03-01T00:00:00Z").getEpochSecond() * 1000;
    private static final long EPOCH_SECONDS = EPOCH_MILLISECONDS / 1000;

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
     * Gets the current Minecraft instance time from Chasmic time
     */
    public static long getInterpolatedInstanceTime() {
        long currentMilliseconds = Instant.now().toEpochMilli();
        double dayProgress = getChasmicDayProgress(currentMilliseconds);

        // Interpolate Minecraft time based on day progress
        // -1.0 (midnight) -> 18000 ticks
        // 0.0 (noon) -> 6000 ticks
        // 1.0 (next midnight) -> 18000 ticks

        // Interpolate between 18000 (midnight) to 6000 (noon) and back to 18000 (next midnight)
        long mcTime = (long) ((1 - dayProgress) * 6000 + dayProgress * 18000);
        return mcTime % 24000; // Ensure it wraps around at 24000 ticks
    }

    private static double getChasmicDayProgress(long currentMilliseconds) {
        long elapsedMilliseconds = currentMilliseconds - EPOCH_MILLISECONDS;

        // 24000 ticks / day in MC
        // 0/24000 = sunrise, 6000 = noon, 12000 = sunset, 18000 = midnight

        // What progress are we in the Chasmic day? (-1.0 to 1.0 where -1.0 is midnight, 0.0 is noon, and 1.0 is the next midnight)
        double millisPerChasmicDay = CHASMIC_HOURS_PER_DAY * CHASMIC_MINUTES_PER_HOUR * 1000; // 24 hours * 60 minutes * 1000 ms
        double dayProgress = (elapsedMilliseconds % millisPerChasmicDay) / millisPerChasmicDay;
        dayProgress = (dayProgress * 2) - 1; // Convert to range -1.0 to 1.0
        return dayProgress;
    }

    /**
     * Gets a short formatted time string for display
     * Format: "1:43pm"
     */
    public static String getShortFormattedTime() {
        int hour = getChasmicHour();
        int minute = getChasmicMinute();
        String amPm = hour >= 12 ? "pm" : "am";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12; // Convert 0 to 12 for 12-hour format
        return String.format("%d:%02d%s", displayHour, minute, amPm);
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


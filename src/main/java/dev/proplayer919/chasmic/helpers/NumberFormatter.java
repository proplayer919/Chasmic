package dev.proplayer919.chasmic.helpers;

import java.math.BigInteger;

public abstract class NumberFormatter {
    public static String formatExp(BigInteger exp) {
        String[] suffixes = {"", "K", "M", "B", "T", "P", "E", "Z", "Y"};
        BigInteger thousand = new BigInteger("1000");
        int suffixIndex = 0;

        while (exp.compareTo(thousand) >= 0 && suffixIndex < suffixes.length - 1) {
            exp = exp.divide(thousand);
            suffixIndex++;
        }

        return exp + suffixes[suffixIndex];
    }
}

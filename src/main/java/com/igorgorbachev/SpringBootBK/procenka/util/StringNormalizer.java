package com.igorgorbachev.SpringBootBK.procenka.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class StringNormalizer {

    public String normalizeArticle(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public String normalizeBrand(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase();
    }

    public String normalizeForSearch(String input) {
        if (input == null) return "";
        return input.replaceAll("\\s+", "").toLowerCase();
    }

    public String extractDigits(String input) {
        if (input == null) return "";
        return input.replaceAll("[^0-9]", "");
    }

    public boolean isDigitsMatch(String str1, String str2) {
        String digits1 = extractDigits(str1);
        String digits2 = extractDigits(str2);
        return !digits1.isEmpty() && !digits2.isEmpty() && digits1.equals(digits2);
    }
}
package com.hp.docmanager.utils;

public class PinyinUtil {
    public static final String[] Init = { "b", "p", "m", "f", "d", "t", "l", "n", "g", "k", "h", "j", "q", "x", "z",
            "c", "s", "zh", "ch", "sh", "r", "y", "w" };

    public static final String[] Vowel = { "a", "o", "e", "i", "u", "v", "ai", "ei", "ui", "ao", "ou", "iu", "ie", "ia",
            "ua", "ue", "uo", "ve", "er", "an", "en", "in", "un", "ang", "eng", "ing", "ong", "uan", "ian", "iao"};

    public boolean isInit(String str) {
        for (String i : Init)
            if (i.equalsIgnoreCase(str))
                return true;
        return false;
    }

    public boolean isInit(char c) {
        return isInit(String.valueOf(c));
    }

    public boolean isInit(char c1, char c2) {
        char[] chars = { c1, c2 };
        return isInit(String.valueOf(chars));
    }

    public boolean isVowel(String str) {
        for (String v : Vowel)
            if (v.equalsIgnoreCase(str))
                return true;
        return false;
    }

    public boolean isVowel(char c) {
        return isVowel(String.valueOf(c));
    }

    public boolean isVowel(char c1, char c2) {
        char[] chars = { c1, c2 };
        return isVowel(String.valueOf(chars));
    }

    public boolean isVowel(char c1, char c2, char c3) {
        char[] chars = { c1, c2, c3 };
        return isVowel(String.valueOf(chars));
    }
}

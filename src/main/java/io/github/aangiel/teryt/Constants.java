package io.github.aangiel.teryt;

import java.util.regex.Pattern;

public final class Constants {
    public static final String TERYT_URL = System.getProperty("TERYT_URL");
    public static final String TERYT_USER = "TestPubliczny";//System.getProperty("TERYT_USER");
    public static final String TERYT_PASS = "1234abcd"; //System.getProperty("TERYT_PASS");
    public static final String TERYT_CATALOG_TERC = "TERC";
    public static final String TERYT_CATALOG_NTS = "NTS";
    public static final String TERYT_CATALOG_SIMC = "SIMC";
    public static final String TERYT_CATALOG_ULIC = "ULIC";
    public static final String TERYT_DICTIONARY_UNIT = "UNIT";


    public static final Pattern COLON_PATTERN = Pattern.compile("\s*,\s*");
    public static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^\\d{2}-\\d{3}$");
}

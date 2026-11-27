package ru.rsreu.ryabinin0513.config;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static final String ELASTIC_HOST     = get("ELASTIC_HOST", "localhost");
    public static final int    ELASTIC_PORT     = Integer.parseInt(get("ELASTIC_PORT", "9200"));
    public static final String ELASTIC_SCHEME   = get("ELASTIC_SCHEME", "http");
    public static final String ELASTIC_USER     = get("ELASTIC_USER", "elastic");
    public static final String ELASTIC_PASSWORD = get("ELASTIC_PASSWORD", "");
    public static final String ELASTIC_INDEX    = get("ELASTIC_INDEX", "e05_13_ryabinin");
    public static final String CSV_FILE         = get("CSV_FILE", "e02_13_ryabinin.csv");

    private static String get(String key, String defaultValue) {
        String value = DOTENV.get(key);
        return value != null ? value : defaultValue;
    }

    private Config() {}
}

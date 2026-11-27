package ru.rsreu.ryabinin0513.config;

import io.github.cdimascio.dotenv.Dotenv;
import ru.rsreu.ryabinin0513.constant.TextConstants;

/**
 * Конфигурация приложения, загружаемая из файла {@code .env}.
 *
 * <p>Если переменная окружения отсутствует, используется значение по умолчанию
 * из {@link TextConstants}.</p>
 */
public final class Config {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static final String ELASTIC_HOST =
            getEnvironmentValue(TextConstants.ENV_ELASTIC_HOST, TextConstants.DEFAULT_ELASTIC_HOST);
    public static final int ELASTIC_PORT = Integer.parseInt(
            getEnvironmentValue(TextConstants.ENV_ELASTIC_PORT, TextConstants.DEFAULT_ELASTIC_PORT));
    public static final String ELASTIC_SCHEME =
            getEnvironmentValue(TextConstants.ENV_ELASTIC_SCHEME, TextConstants.DEFAULT_ELASTIC_SCHEME);
    public static final String ELASTIC_USER =
            getEnvironmentValue(TextConstants.ENV_ELASTIC_USER, TextConstants.DEFAULT_ELASTIC_USER);
    public static final String ELASTIC_PASSWORD =
            getEnvironmentValue(TextConstants.ENV_ELASTIC_PASSWORD, TextConstants.DEFAULT_ELASTIC_PASSWORD);
    public static final String ELASTIC_INDEX =
            getEnvironmentValue(TextConstants.ENV_ELASTIC_INDEX, TextConstants.DEFAULT_ELASTIC_INDEX);
    public static final String CSV_FILE =
            getEnvironmentValue(TextConstants.ENV_CSV_FILE, TextConstants.DEFAULT_CSV_FILE);

    /**
     * Возвращает значение переменной окружения или значение по умолчанию.
     *
     * @param key          ключ переменной окружения
     * @param defaultValue значение, используемое при отсутствии переменной
     * @return значение переменной окружения или {@code defaultValue}
     */
    private static String getEnvironmentValue(String key, String defaultValue) {
        String value = DOTENV.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private Config() {
    }
}

/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.error;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.zowe.commons.rest.response.ApiMessage;
import org.zowe.commons.rest.response.BasicApiMessage;
import org.zowe.commons.rest.response.BasicMessage;
import org.zowe.commons.rest.response.Message;
import org.zowe.commons.rest.response.MessageType;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link ErrorService} that loads messages from YAML
 * files.
 */
@Slf4j
public class ErrorServiceImpl implements ErrorService {
    private static final String COMMONS_MESSAGES_BASENAME = "commons-messages";
    private static final String DEFAULT_MESSAGES_BASENAME = "messages";
    private static final String YAML_EXTENSION = ".yml";
    private static final String INVALID_KEY_MESSAGE = "org.zowe.commons.error.invalidMessageKey";
    private static final String INVALID_MESSAGE_TEXT_FORMAT = "org.zowe.commons.error.invalidMessageTextFormat";
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorServiceImpl.class);
    private static final int STACK_TRACE_ELEMENT_ABOVE_CREATEAPIMESSAGE_METHOD = 4;

    private final ErrorMessageStorage messageStorage;
    private String defaultMessageSource;
    private final ErrorServiceControl control = new ErrorServiceControl();

    /**
     * Cache to hold loaded ResourceBundles. The key to this map is bundle basename
     * which holds a Map which has the locale as the key and in turn holds the
     * ResourceBundle instances.
     */
    private final Map<String, Map<Locale, ResourceBundle>> cachedResourceBundles = new ConcurrentHashMap<>();

    /**
     * List of base names that are used to load resource bundles for localized
     * texts.
     */
    private final List<String> baseNames = new ArrayList<>(2);

    /**
     * Recommended way how to get an instance of ErrorService for your application.
     *
     * @return Error service that uses common messages and messages from default
     *         resource file (messages.yml).
     */
    public static ErrorService getDefault() {
        ErrorServiceImpl errorService = new ErrorServiceImpl("/" + DEFAULT_MESSAGES_BASENAME + YAML_EXTENSION);
        errorService.addResourceBundleBaseName(DEFAULT_MESSAGES_BASENAME);
        CommonsErrorService.get().addResourceBundleBaseName(DEFAULT_MESSAGES_BASENAME);
        return errorService;
    }

    /**
     * @return Returns an instance of error service with messages for the Zowe REST
     *         API Commons.
     */
    public static ErrorService getCommonsDefault() {
        ErrorServiceImpl errorService = new ErrorServiceImpl();
        errorService.loadMessages("/" + COMMONS_MESSAGES_BASENAME + YAML_EXTENSION);
        errorService.addResourceBundleBaseName(COMMONS_MESSAGES_BASENAME);
        return errorService;
    }

    /**
     * Constructor that creates empty message storage.
     */
    public ErrorServiceImpl() {
        messageStorage = new ErrorMessageStorage();
    }

    /**
     * Constructor that creates common messages and messages from file.
     *
     * @param messagesFilePath path to file with messages.
     */
    public ErrorServiceImpl(String messagesFilePath) {
        this();
        loadMessages("/" + COMMONS_MESSAGES_BASENAME + YAML_EXTENSION);
        addResourceBundleBaseName(COMMONS_MESSAGES_BASENAME);
        loadMessages(messagesFilePath);
    }

    @Override
    public void addResourceBundleBaseName(String baseName) {
        if (!baseNames.contains(baseName)) {
            baseNames.add(baseName);
        }
    }

    private ResourceBundle getResourceBundle(String baseName, Locale locale) {
        Map<Locale, ResourceBundle> localeMap = this.cachedResourceBundles.get(baseName);
        if (localeMap != null) {
            ResourceBundle bundle = localeMap.get(locale);
            if (bundle != null) {
                return bundle;
            }
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, control);
            if (localeMap == null) {
                localeMap = new ConcurrentHashMap<>();
                Map<Locale, ResourceBundle> existing = this.cachedResourceBundles.putIfAbsent(baseName, localeMap);
                if (existing != null) {
                    localeMap = existing;
                }
            }
            localeMap.put(locale, bundle);
            return bundle;
        } catch (MissingResourceException ex) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("ResourceBundle '%s' not found: %s", baseName, ex.getMessage()));
            }
            return null;
        }
    }

    @Override
    public ApiMessage createApiMessage(String key, Object... parameters) {
        Message message = createMessage(null, key, parameters);
        return new BasicApiMessage(Collections.singletonList(message));
    }

    @Override
    public ApiMessage createApiMessage(Locale locale, String key, Object... parameters) {
        Message message = createMessage(locale, key, parameters);
        return new BasicApiMessage(Collections.singletonList(message));
    }

    @Override
    public ApiMessage createApiMessage(String key, List<Object[]> parameters) {
        List<Message> messageList = parameters.stream().filter(Objects::nonNull).map(ob -> createMessage(null, key, ob))
                .collect(Collectors.toList());
        return new BasicApiMessage(messageList);
    }

    @Override
    public ApiMessage createApiMessage(Locale locale, String key, List<Object[]> parameters) {
        List<Message> messageList = parameters.stream().filter(Objects::nonNull)
                .map(ob -> createMessage(locale, key, ob)).collect(Collectors.toList());
        return new BasicApiMessage(messageList);
    }

    @Override
    public void loadMessages(String messagesFilePath) {
        try (InputStream in = ErrorServiceImpl.class.getResourceAsStream(messagesFilePath)) {
            Yaml yaml = new Yaml();
            ErrorMessages applicationMessages = yaml.loadAs(in, ErrorMessages.class);
            messageStorage.addMessages(applicationMessages);
        } catch (YAMLException | IOException e) {
            throw new MessageLoadException(
                    "There is problem with reading application messages file: " + messagesFilePath, e);
        }
    }

    @Override
    public String getDefaultMessageSource() {
        return defaultMessageSource;
    }

    @Override
    public void setDefaultMessageSource(String defaultMessageSource) {
        this.defaultMessageSource = defaultMessageSource;
    }

    private Message createMessage(Locale locale, String key, Object... parameters) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        ErrorMessage message = messageStorage.getErrorMessage(key);
        message = validateMessage(message, key);
        Object[] messageParameters = validateParameters(message, key, parameters);

        String text;
        StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb, locale)) {
            formatter.format(localizedText(locale, key + ".text", message.getText()), messageParameters);
            text = sb.toString();
        } catch (IllegalFormatConversionException exception) {
            LOGGER.debug("Internal error: Invalid message format was used", exception);
            message = messageStorage.getErrorMessage(INVALID_MESSAGE_TEXT_FORMAT);
            message = validateMessage(message, key);
            messageParameters = validateParameters(message, key, parameters);
            text = String.format(message.getText(), messageParameters);
        }
        String component = getLocalizedComponentOrUseDefault(locale, key, message);
        List<Object> parameterList = null;
        if (parameters != null) {
            parameterList = Arrays.asList(parameters);
        }
        return new BasicMessage(message.getType(), message.getNumber(), text,
                localizedText(locale, key + ".reason", message.getReason()),
                localizedText(locale, key + ".action", message.getAction()), key, parameterList,
                BasicMessage.generateMessageInstanceId(), defaultMessageSource, component);
    }

    private String getLocalizedComponentOrUseDefault(Locale locale, String key, ErrorMessage message) {
        String component = message.getComponent();
        if (component == null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String className = stackTrace[STACK_TRACE_ELEMENT_ABOVE_CREATEAPIMESSAGE_METHOD].getClassName();
            component = className;
        } else {
            component = localizedText(locale, key + ".component", component);
        }
        return component;
    }

    private String localizedText(Locale locale, String key, String defaultText) {
        if (locale != null) {
            for (int i = baseNames.size() -1; i >= 0; i--) {
                String baseName = baseNames.get(i);
                ResourceBundle bundle = getResourceBundle(baseName, locale);
                if (bundle == null) {
                    continue;
                }
                try {
                    return bundle.getString("messages." + key);
                } catch (MissingResourceException ignored) {
                    return defaultText;
                }
            }
        }
        return defaultText;
    }

    private ErrorMessage validateMessage(ErrorMessage message, String key) {
        if (message == null) {
            LOGGER.debug("Invalid message key '{}' was used. Please resolve this problem.", key);
            message = messageStorage.getErrorMessage(INVALID_KEY_MESSAGE);
        }

        if (message == null) {
            String text = "Internal error: Invalid message key '%s' provided. No default message found. Please contact support of further assistance.";
            message = new ErrorMessage(INVALID_KEY_MESSAGE, "ZWEAS001", MessageType.ERROR, text, null, null, null);
        }

        return message;
    }

    @Override
    public String getReadableMessage(String key, Object... parameters) {
        return createApiMessage(key, parameters).toReadableText();
    }

    private Object[] validateParameters(ErrorMessage message, String key, Object... parameters) {
        if (message.getKey().equals(INVALID_KEY_MESSAGE)) {
            return new Object[] { key };
        } else {
            return parameters;
        }
    }
    /**
     * Custom implementation of {@code ResourceBundle.Control}, adding support for
     * UTF-8.
     */
    class ErrorServiceControl extends ResourceBundle.Control {
        protected ResourceBundle loadBundle(Reader reader) throws IOException {
            return new PropertyResourceBundle(reader);
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            if (format.equals("java.properties")) {
                return newJavaPropertiesBundle(baseName, locale, loader, reload);
            } else {
                return super.newBundle(baseName, locale, format, loader, reload);
            }
        }

        private ResourceBundle newJavaPropertiesBundle(String baseName, Locale locale, ClassLoader loader, boolean reload)
                throws IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ClassLoader classLoader = loader;
            InputStream inputStream;
            try {
                inputStream = AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>) () -> {
                    InputStream is = null;
                    is = classLoader.getResourceAsStream(resourceName);
                    return is;
                });
            } catch (PrivilegedActionException ex) {
                throw (IOException) ex.getException();
            }
            return loadBundleFromInputStream(inputStream);
        }

        private ResourceBundle loadBundleFromInputStream(InputStream inputStream) throws IOException, UnsupportedEncodingException {
            if (inputStream != null) {
                try (InputStreamReader bundleReader = new InputStreamReader(inputStream, "UTF-8")) {
                    return loadBundle(bundleReader);
                }
            } else {
                return null;
            }
        }
    }
}

package edu.sumdu.tss.elephant;

import edu.sumdu.tss.elephant.helper.enums.Lang;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import edu.sumdu.tss.elephant.utils.ContextUtils;

import java.io.File;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Preset {

    private Preset() { }

    public static final String BASE_URL = "http://localhost:7000/";
    public static final File PROPERTIES_FILE = new File("db.config");
    public static final long FILESYSTEM_WATCHER_THRESHOLD = 1000;
    public static final Supplier<String> EMAIL = () -> StringUtils.randomAlphaString(20) + "@example.com";
    public static final Supplier<String> PASSWORD = () -> "Password123#";
    public static final Supplier<String> PUBLIC_KEY = () -> StringUtils.randomAlphaString(15);
    public static final Supplier<String> PRIVATE_KEY = () -> StringUtils.randomAlphaString(15);
    public static final Supplier<String> UUID = StringUtils::uuid;
    public static final Supplier<String> SESSION_ID = () -> "node0" + StringUtils.randomAlphaString(25) + ".node0";

    public static final Supplier<String> VALID_EMAIL = Preset.EMAIL;
    public static final Supplier<String> INVALID_EMAIL = () -> "test@user@gmail.com";

    public static final Supplier<String> VALID_PASSWORD = () -> "Qa#12345";
    public static final Supplier<String> INVALID_PASSWORD = () -> "ppp";

    public static final Supplier<String> DB_NAME = () -> StringUtils.randomAlphaString(10);
    public static final Supplier<String> DB_USERNAME = () -> StringUtils.randomAlphaString(10);
    public static final Supplier<String> DB_PASSWORD = () -> StringUtils.randomAlphaString(10);

    public static final Supplier<String> BACKUP_NAME = () -> StringUtils.randomAlphaString(50);
    public static final Supplier<String> SCRIPT_NAME = () -> StringUtils.randomAlphaString(50);
    public static final Supplier<String> SCRIPT_DESCRIPTION = () -> StringUtils.randomAlphaString(128);

    public static final File SCREENSHOTS_DIRECTORY =new File("E:\\erlkonig\\Elephant\\src\\test\\resources\\Screenshots");
    
    public static final Function<String, String> UNEQUAL_STRING = str -> {
        if (str.isEmpty()) {
            return "a";
        }

        ThreadLocalRandom randomizer = ThreadLocalRandom.current();
        int randomPosition = randomizer.nextInt(0, str.length());
        char charToReplace = str.toCharArray()[randomPosition];
        char newChar;

        while ((newChar = (char) randomizer.nextInt(65, 123)) == charToReplace ||
                (newChar > 90 && newChar < 97)) {
        }

        char[] chars = str.toCharArray();
        chars[randomPosition] = newChar;
        return new String(chars);
    };

}
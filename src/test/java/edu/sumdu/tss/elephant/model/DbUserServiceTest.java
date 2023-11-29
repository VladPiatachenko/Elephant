package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DbUserService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import edu.sumdu.tss.elephant.Preset;
import edu.sumdu.tss.elephant.utils.*;
import org.junit.jupiter.api.*;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.io.File;

public class DbUserServiceTest {
    private static final String FIND_USER = "SELECT count(1) FROM pg_catalog.pg_user where usename = :username";
    private static final String FIND_SPACE = "SELECT count(1) FROM pg_catalog.pg_tablespace where spcname = :username;";
    private static final String FIND_DB = "SELECT count(1) from pg_catalog.pg_database join pg_authid on pg_database.datdba = pg_authid.oid  where rolname = :username;";

    Connection con;

    @BeforeAll
    public static void initialize() {
        Keys.loadParams(Preset.PROPERTIES_FILE);
    }

    @BeforeEach
    void setUp() {
        con = DBPool.getConnection().open();
    }

    @AfterEach
    void tearDown() {
        con.close();
    }

    @Test
    public void initUser_AllCorrect_NewUserWithEmptyTablespace() {
        String password = Preset.PASSWORD.get();
        User user = UserService.newDefaultUser();
        user.setLogin(Preset.EMAIL.get());
        user.setPassword(password);
        UserService.save(user);

        String username = user.getUsername();

        UserService.initUserStorage(username);
        DbUserService.initUser(username, password);
        int user_count = con.createQuery(FIND_USER).addParameter("username", username).executeScalar(Integer.class);
        int ts_count = con.createQuery(FIND_SPACE).addParameter("username", username).executeScalar(Integer.class);
        int db_count = con.createQuery(FIND_DB).addParameter("username", username).executeScalar(Integer.class);

        Assertions.assertAll(
                "Користувач може бути успішно ініціалізований",
                () -> Assertions.assertEquals(1, user_count, "Користувач був створений"),
                () -> Assertions.assertEquals(1, ts_count, "Tablespace був створений"),
                () -> Assertions.assertEquals(0, db_count, "Бази даних за замовчуванням не створюються")
        );
    }

    @Test
    public void dbUserPasswordReset_AllSuccess_PasswordChanged() {
        User user = UserUtils.registerUser(UserRole.BASIC_USER);
        Database database = DatabaseUtils.createDatabase(user);
        String dbName = database.getName();
        String username = user.getUsername();
        String oldDbUserPassword = user.getDbPassword();
        String newDbUserPassword = Preset.UNEQUAL_STRING.apply(oldDbUserPassword);

        DbUserService.dbUserPasswordReset(username, newDbUserPassword);
        String url = "jdbc:postgresql://" + Keys.get("DB.URL") + ":" + Keys.get("DB.PORT") + "/" + dbName;

        Assertions.assertAll(
                "Пароль користувача був успішно змінений",
                () -> Assertions.assertThrows(
                        Sql2oException.class,
                        () -> new Sql2o(url, username, oldDbUserPassword).open(),
                        "Використати старий пароль для встановлення з'єднання неможливо"
                ),
                () -> Assertions.assertDoesNotThrow(
                        () -> new Sql2o(url, username, newDbUserPassword).open(),
                        "З'єднання можна встановити за використання нового пароля"
                )
        );
    }

    @Test
    public void dropUser_UserWithDependencies_UserDataDeleted() {
        User user = UserUtils.registerUser(UserRole.BASIC_USER);
        Database database = DatabaseUtils.createDatabase(user);

        String username = user.getUsername();

        for (int i = 0; i < 2; i++) {
            BackupUtils.createBackup(database);
        }

        for (int i = 0; i < 3; i++) {
            ScriptUtils.createScript(database);
        }

        int userCount = con.createQuery(FIND_USER).addParameter("username", username).executeScalar(Integer.class);
        int tsCount = con.createQuery(FIND_SPACE).addParameter("username", username).executeScalar(Integer.class);
        int dbCount = con.createQuery(FIND_DB).addParameter("username", username).executeScalar(Integer.class);
        int backupCount = con.createQuery("""
            SELECT COUNT(*) FROM backups
            JOIN databases ON backups.database = databases.name
            JOIN users ON users.username = databases.owner
            WHERE users.username = :username
        """).addParameter("username", username).executeScalar(Integer.class);
        int scriptCount = con.createQuery("""
            SELECT COUNT(*) FROM scripts
            JOIN databases ON scripts.database = databases.name
            JOIN users ON users.username = databases.owner
            WHERE users.username = :username
        """).addParameter("username", username).executeScalar(Integer.class);
        DbUserService.dropUser(username);

        Assertions.assertAll(
                "Усі дані про користувача були видалені",
                () -> Assertions.assertEquals(0, userCount, "Запис про користувача видалений"),
                () -> Assertions.assertEquals(0, tsCount, "Tablespace користувача видалений"),
                () -> Assertions.assertEquals(0, dbCount, "Записи про бази даних користувача видалені"),
                () -> Assertions.assertEquals(0, backupCount, "Записи про бекапи видалені"),
                () -> Assertions.assertEquals(0, scriptCount, "Записи про сценарії видалені"),
                () -> Assertions.assertFalse(new File(UserService.userStoragePath(username)).exists(), "Папка з файлами користувача видалена")
        );
    }

    @Test
    public void dropUser_EmptyUser_UserDataDeleted() {
        User user = UserUtils.registerUser(UserRole.BASIC_USER);
        String username = user.getUsername();

        int userCount = con.createQuery(FIND_USER).addParameter("username", username).executeScalar(Integer.class);
        int tsCount = con.createQuery(FIND_SPACE).addParameter("username", username).executeScalar(Integer.class);
        int dbCount = con.createQuery(FIND_DB).addParameter("username", username).executeScalar(Integer.class);
        int backupCount = con.createQuery("""
            SELECT COUNT(*) FROM backups
            JOIN databases ON backups.database = databases.name
            JOIN users ON users.username = databases.owner
            WHERE users.username = :username
        """).addParameter("username", username).executeScalar(Integer.class);
        int scriptCount = con.createQuery("""
            SELECT COUNT(*) FROM scripts
            JOIN databases ON scripts.database = databases.name
            JOIN users ON users.username = databases.owner
            WHERE users.username = :username
        """).addParameter("username", username).executeScalar(Integer.class);
        DbUserService.dropUser(username);

        Assertions.assertAll(
                "Усі дані про користувача були видалені",
                () -> Assertions.assertEquals(1, userCount, "Запис про користувача видалений"),
                () -> Assertions.assertEquals(1, tsCount, "Tablespace користувача видалений"),
                () -> Assertions.assertEquals(0, dbCount, "Записи про бази даних користувача видалені"),
                () -> Assertions.assertEquals(0, backupCount, "Записи про бекапи видалені"),
                () -> Assertions.assertEquals(0, scriptCount, "Записи про сценарії видалені"),
                () -> Assertions.assertFalse(new File(UserService.userStoragePath(username)).exists(), "Папка з файлами користувача видалена")
        );
    }
}

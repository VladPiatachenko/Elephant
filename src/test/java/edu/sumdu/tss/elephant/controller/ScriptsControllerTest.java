package edu.sumdu.tss.elephant.controller;
import edu.sumdu.tss.elephant.controller.ScriptsController;
import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.exception.AccessRestrictedException;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.Script;
import edu.sumdu.tss.elephant.model.ScriptService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.Preset;
import edu.sumdu.tss.elephant.utils.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScriptsControllerTest {
    private static User user;
    private static Database database;
    private static Script script;

    @BeforeAll
    public static void initialize() throws NoSuchFieldException, IllegalAccessException {
        Keys.loadParams(Preset.PROPERTIES_FILE);

        user = UserUtils.registerUser(UserRole.BASIC_USER);
        database = DatabaseUtils.createDatabase(user);
        script = ScriptUtils.createScript(database);
    }

    @Test
    @Order(1)
    public void create_AllCorrect_Success() throws IOException {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE);
        ContextUtils.setCurrentUser(context, user);
        ContextUtils.setCurrentDatabase(context, database);
        ContextUtils.formParamAsClass(context, "description", Preset.SCRIPT_DESCRIPTION.get());

        File tempFile = File.createTempFile("prefix-", "-suffix");
        FileSystemUtils.appendToFile(tempFile, "SELECT 1;" + System.lineSeparator());
        Mockito.doReturn(new UploadedFile(new FileInputStream(tempFile), "text/plain", tempFile.getName(), FilenameUtils.getExtension(tempFile.getName()), tempFile.length())).when(context).uploadedFile(Mockito.anyString());

        ScriptsController.create(context);

        Mockito.verify(context).redirect(ScriptsController.BASIC_PAGE.replace("{database}", database.getName()));
        tempFile.deleteOnExit();
    }

    @Test
    @Order(2)
    public void create_ScriptsLimitExceeded_Fail() {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE);
        ContextUtils.setCurrentUser(context, user);
        ContextUtils.setCurrentDatabase(context, database);
        ScriptUtils.createScript(database);

        ScriptsController.create(context);

        Mockito.verify(context).sessionAttribute(Keys.ERROR_KEY, "You limit reached");
        Mockito.verify(context).redirect("/");
    }

    @Test
    @Order(3)
    public void create_NoFile_HttpError500() {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE);
        ContextUtils.setCurrentUser(context, user);
        ContextUtils.setCurrentDatabase(context, database);

        ContextUtils.formParamAsClass(context, "description", Preset.SCRIPT_DESCRIPTION.get());
        Mockito.doReturn(null).when(context).uploadedFile(Mockito.anyString());

        Assertions.assertThrows(
                HttpError500.class,
                () -> ScriptsController.create(context),
                "У разі відсутності файлу буде викинуте виключення HttpError500"
        );
    }

    @Test
    @Order(4)
    public void show_ScriptExists_Success() {
        Context context = ContextUtils.get(ScriptsController.BASIC_PAGE + "{script}");
        ContextUtils.setCurrentDatabase(context, database);
        ContextUtils.pathParam(context, "script", String.valueOf(script.getId()));

        Assertions.assertDoesNotThrow(
                () -> ScriptsController.show(context)
        );
    }

    @Test
    @Order(5)
    public void show_NotOwnScript_AccessRestrictedException() {
        User otherUser = UserUtils.registerUser(UserRole.BASIC_USER);
        Database otherDatabase = DatabaseUtils.createDatabase(otherUser);
        Script otherScript = ScriptUtils.createScript(otherDatabase);

        Context context = ContextUtils.get(ScriptsController.BASIC_PAGE + "{script}");
        ContextUtils.setCurrentDatabase(context, database);
        ContextUtils.pathParam(context, "script", String.valueOf(otherScript.getId()));

        Assertions.assertThrows(
                AccessRestrictedException.class,
                () -> ScriptsController.show(context),
                "У разі спроби отримання доступу до чужого сценарію - буде викинутий AccessRestrictedException"
        );
    }

    @Test
    @Order(6)
    public void show_ScriptNotExists_Fail() {
        Context context = ContextUtils.get(ScriptsController.BASIC_PAGE + "{script}");
        ContextUtils.setCurrentDatabase(context, database);
        ContextUtils.pathParam(context, "script", String.valueOf(Integer.MAX_VALUE));

        Assertions.assertThrows(
                HttpError500.class,
                () -> ScriptsController.show(context),
                "У разі відсутності сценарію - викидання помилки HTTP з кодом 500"
        );
    }

    @Test
    @Order(7)
    public void index() {
        Context context = ContextUtils.get(ScriptsController.BASIC_PAGE);
        Map<String, Object> model = ContextUtils.applyModel(context);
        ContextUtils.setCurrentDatabase(context, database);

        ScriptsController.index(context);

        Mockito.verify(context).sessionAttribute(Mockito.eq(Keys.BREADCRUMB_KEY), Mockito.any(List.class));
        Mockito.verify(context).render("/velocity/script/index.vm", model);
    }

    @Test
    @Order(8)
    public void run_AllCorrect_Success() {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE + "{script}");
        Map<String, Object> model = ContextUtils.applyModel(context);
        ContextUtils.pathParam(context, "database", database.getName());
        ContextUtils.pathParam(context, "script", String.valueOf(script.getId()));

        ScriptsController.run(context);

        Mockito.verify(context).render("/velocity/script/run.vm", model);
    }

    @Test
    @Order(9)
    public void run_SQLError_Success() {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE + "{script}");
        Map<String, Object> model = ContextUtils.applyModel(context);
        ContextUtils.pathParam(context, "database", database.getName());

        Script badScript = ScriptUtils.createScript(database, "bad-script.sql", "Bad script", "SELECT * FROM emp;");
        ContextUtils.pathParam(context, "script", String.valueOf(badScript.getId()));

        ScriptsController.run(context);

        Mockito.verify(context).render("/velocity/script/run.vm", model);
    }

    @Test
    @Order(10)
    public void run_ScriptFileNotExists_HttpError500() {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE + "{script}");
        ContextUtils.applyModel(context);
        ContextUtils.pathParam(context, "database", database.getName());

        Script badScript = ScriptUtils.createScript(database, "bad-script.sql", "Bad script", "SELECT * FROM emp;");
        badScript.setPath(badScript.getPath() + "A");
        ScriptService.save(badScript);
        Long key = DBPool.getConnection().createQuery("insert into scripts(database, filename, path, description) values (:database, :filename, :path, :description)", true).bind(badScript).executeUpdate().getKey(Long.class);
        badScript.setId(key);

        ContextUtils.pathParam(context, "script", String.valueOf(badScript.getId()));

        Assertions.assertThrows(
                HttpError500.class,
                () -> ScriptsController.run(context),
                "У разі відсутності збереженого файлу сценарію під час виконання буде викинуте виключення HttpError500"
        );
    }

    @Test
    @Order(11)
    public void delete_ScriptExists_NoScript() {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE + "{script}/delete");
        ContextUtils.setCurrentDatabase(context, database);
        ContextUtils.pathParam(context, "script", String.valueOf(script.getId()));

        ScriptsController.delete(context);

        Mockito.verify(context).redirect(ScriptsController.BASIC_PAGE.replace("{database}", database.getName()));
    }

    @Test
    @Order(12)
    public void delete_ScriptNotExists_NotFoundException() {
        Context context = ContextUtils.post(ScriptsController.BASIC_PAGE + "{script}/delete");
        ContextUtils.setCurrentDatabase(context, database);
        ContextUtils.pathParam(context, "script", String.valueOf(Integer.MAX_VALUE));

        Assertions.assertThrows(
                NotFoundException.class,
                () -> ScriptsController.delete(context),
                "У разі спроби видалення неіснуючого сценарію - викидається NotFoundException"
        );
    }

    @Test
    @Order(13)
    public void register() {
        Javalin app = Mockito.mock(Javalin.class);
        new ScriptsController(app).register(app);

        Mockito.inOrder(app).verify(app, VerificationModeFactory.calls(2)).get(Mockito.anyString(), Mockito.any(Handler.class));
        Mockito.inOrder(app).verify(app, VerificationModeFactory.calls(3)).post(Mockito.anyString(), Mockito.any(Handler.class));
    }
}
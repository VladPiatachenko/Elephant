package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class UserTest {
    @Test
    void cryptTest(){
        User usr=new User();
        usr.setLogin("Login");
        String str1="not encrypted string";
        String str2=usr.crypt(str1);
        Assertions.assertNotEquals(str1,str2);
        Assertions.assertNotNull(usr.getLogin());
    }
    @Test
    public void resetTokenTest() {
        User usr=new User();
        usr.setToken("old_token");
        usr.resetToken();
        Assertions.assertNotEquals("old_token",usr.getToken());
        Assertions.assertEquals(15,usr.getToken().length());
    }

    @Test
    public void pushTest() throws SQLException {
        //як і з запуском проекта - треба вказати файл налаштувань
        Keys.loadParams(new File("db.config"));

        //для роботи з БД треба юзер - створімо його
        User user = new User();
        //Оскільки тест виконуватиметься певну кількість раз ім'я рандомізоване, але краще опрацювати видалення юзера по тесту
        //зверніть увагу, що рандом примхлива штука і тест впаде, якщо рандомне значення повториться з попередніми тестами
        String dbUsername = "DB_NAME"+(int)Math.random()*100+1;
        String dbPassword = "DB_PASS";

        //для реєстрації юзера задамо все потрібне начиння юзеру - він буде в БД, ви зможете все це дослідити
        user.setRole(UserRole.BASIC_USER.getValue());
        user.setPrivateKey("privatkey"+(int)Math.random()*100+1);
        user.setPublicKey("publickey"+(int)Math.random()*100+1);
        user.setToken("token");
        user.setUsername(dbUsername);
        user.setDbPassword(dbPassword);

        user.setLogin((int)Math.random()*100+1+"test@test.ts");
        user.setPassword("password");
        UserService.save(user);
        UserService.initUserStorage(dbUsername);
        //завершили створювати юзера, цей процес мжна скопіювати з контролера регістрації
        DbUserService.initUser(dbUsername, dbPassword);
        DatabaseService.create(dbUsername, user.getUsername(), user.getUsername());
        //створили БД для нового юзера
        Database database = DatabaseService.byName(dbUsername);
        String message = "test";

        //ми не хочемо запускати весь проект, для роботи створимо заглушки які відпрацюють порібну нам логіку
        //Для роботи знадобиться шлях до БД, описаний в BackupController
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/database/"+dbUsername+"/point/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContent("".getBytes(StandardCharsets.UTF_8));

        //заглушка на Contex, аргументи описані відносно потреб javalin - перегляньте вміст ContextUtil
        Context ctx = Mockito.spy(io.javalin.http.util.ContextUtil.init(request, response, "/database/"+dbUsername+"/point/", Map.of(), HandlerType.POST, Map.of(io.javalin.http.util.ContextUtil.maxRequestSizeKey, 1_000_000L)));
        //опишемо правила що має повертати код, коли Contex питають про певні значення
        Mockito.doReturn(user).when(ctx).sessionAttribute(Keys.SESSION_CURRENT_USER_KEY);
        Mockito.doReturn("127.0.0.1").when(ctx).ip();
        Mockito.doReturn("point").when(ctx).pathParam("backupname");
        //нарешті викличимо на виконання метод
        LogService.push(ctx, dbUsername, message);

        //Запит відбувся, треба підготувати рещультати до перевірки. Зараз ви можете глянути вміт таблиці logger, де з'явився новий запис
        ResultSet resultSet = null;
        try (
                //стандартне звернення в таблицю через jdbc
                //ви можете глянути в SQLController як це описано в проекті
            Connection connection = DBPool.getConnection().open().getJdbcConnection()) {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM logger WHERE database = '" + dbUsername +
                    "' AND \"user\" = '" + user.getLogin() +
                    "' AND message = '" + message + "'");
        } catch (Throwable ignored) {
        }
//ми отримаємо 1 рядок з таблиці, перевіримо що відповідь БД не порожня, і час звернення зарєестровано
        Assertions.assertTrue(
                resultSet.next() && resultSet.getTimestamp("CREATED_AT") != null,
                "Створений під час логування запис може бути знайдений"
        );
        }

    }





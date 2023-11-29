package edu.sumdu.tss.elephant.middlewareunits;

import edu.sumdu.tss.elephant.Preset;
import edu.sumdu.tss.elephant.helper.exception.CheckTokenException;
import edu.sumdu.tss.elephant.middleware.CSRFFilter;
import edu.sumdu.tss.elephant.utils.ContextUtils;
import io.javalin.http.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CSRFFilterTest {
    @Test
    public void check_NotPost_Skipped() {
        Context context = ContextUtils.get("/");

        Assertions.assertDoesNotThrow(
                () -> CSRFFilter.check(context),
                "CSRF-токен не перевіряється для запитів, метод яких відмінній від POST"
        );
    }

    @Test
    public void check_PostTokenValid_Success() {
        String sessionId = Preset.SESSION_ID.get();
        Context context = ContextUtils.get("/");
        Mockito.doReturn(sessionId).when(context).sessionAttribute("SessionID");
        CSRFFilter.generate(context);

        String token = context.sessionAttribute("csrf");
        Context contextPost = ContextUtils.post("/");
        Mockito.doReturn(sessionId).when(contextPost).sessionAttribute("SessionID");
        Mockito.doReturn(token).when(contextPost).formParam("_csrf");

        Assertions.assertDoesNotThrow(
                () -> CSRFFilter.check(contextPost),
                "Перевірка токена CSRF виконується успішно"
        );
    }

    @Test
    public void check_PostTokenValidNotRootUrl_Success() {
        String sessionId = Preset.SESSION_ID.get();
        String url = "/registration";
        Context context = ContextUtils.get(url);
        Mockito.doReturn(sessionId).when(context).sessionAttribute("SessionID");
        CSRFFilter.generate(context);

        String token = context.sessionAttribute("csrf");
        Context contextPost = ContextUtils.post(url);
        Mockito.doReturn(sessionId).when(contextPost).sessionAttribute("SessionID");
        Mockito.doReturn(token).when(contextPost).formParam("_csrf");

        Assertions.assertDoesNotThrow(
                () -> CSRFFilter.check(contextPost),
                "Перевірка токена CSRF виконується успішно"
        );
    }

    @Test
    public void check_PostTokenInvalidNumber_CheckTokenException() {
        String sessionId = Preset.SESSION_ID.get();
        String token = "123";
        Context contextPost = ContextUtils.post("/");
        Mockito.doReturn(sessionId).when(contextPost).sessionAttribute("SessionID");
        Mockito.doReturn(token).when(contextPost).formParam("_csrf");

        Assertions.assertThrows(
                CheckTokenException.class,
                () -> CSRFFilter.check(contextPost),
                "Перевірка токена CSRF виконується успішно"
        );
    }

    @Test
    public void generate_NotGet_Skipped() {
        Context context = ContextUtils.post("/");

        CSRFFilter.generate(context);
        Assertions.assertNull(context.sessionAttribute("csrf"), "CSRF-токен не генерується для запитів, метод яких відмінній від GET");
    }

    @Test
    public void generate_Get_CSRFToken() {
        Context context = ContextUtils.get("/");
        Mockito.doReturn(Preset.SESSION_ID.get()).when(context).sessionAttribute("SessionID");

        CSRFFilter.generate(context);
        Assertions.assertNotNull(context.sessionAttribute("csrf"), "CSRF-токен успішно генерується");
    }
}
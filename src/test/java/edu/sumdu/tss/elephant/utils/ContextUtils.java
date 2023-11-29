package edu.sumdu.tss.elephant.utils;

import edu.sumdu.tss.elephant.controller.AbstractController;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ContextUtils {
    private ContextUtils() { }

    public static Context get(final String url) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContent("".getBytes(StandardCharsets.UTF_8));
        return Mockito.spy(io.javalin.http.util.ContextUtil.init(request, response, url, Map.of(), HandlerType.GET, Map.of(io.javalin.http.util.ContextUtil.maxRequestSizeKey, 1_000_000L)));
    }

    public static Context post(final String url) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContent("".getBytes(StandardCharsets.UTF_8));
        return Mockito.spy(io.javalin.http.util.ContextUtil.init(request, response, url, Map.of(), HandlerType.POST, Map.of(io.javalin.http.util.ContextUtil.maxRequestSizeKey, 1_000_000L)));
    }

    public static <T> void formParamAsClass(final Context context, final String name, final T value) {
        Class<T> clazz = (Class<T>) value.getClass();
        Mockito.doReturn(new Validator<T>(value.toString(), clazz, name)).when(context).formParamAsClass(name, clazz);
    }

    public static <T> void queryParamAsClass(final Context context, final String name, final T value) {
        Class<T> clazz = (Class<T>) value.getClass();
        Mockito.doReturn(new Validator<T>(value.toString(), clazz, name)).when(context).queryParamAsClass(name, clazz);
    }

    public static <T> void pathParamAsClass(final Context context, final String name, final T value) {
        Class<T> clazz = (Class<T>) value.getClass();
        Mockito.doReturn(new Validator<T>(value.toString(), clazz, name)).when(context).pathParamAsClass(name, clazz);
    }

    public static void pathParam(final Context context, final String fieldName, final String fieldValue) {
        Mockito.doReturn(fieldValue).when(context).pathParam(fieldName);
    }

    public static void formParam(final Context context, final String fieldName, final String fieldValue) {
        Mockito.doReturn(fieldValue).when(context).formParam(fieldName);
    }

    public static void queryParam(final Context context, final String fieldName, final String fieldValue) {
        Mockito.doReturn(fieldValue).when(context).queryParam(fieldName);
    }

    public static void sessionAttribute(final Context context, final String attributeName, final String attributeValue) {
        Mockito.doReturn(attributeValue).when(context).sessionAttribute(attributeName);
    }

    public static void header(final Context context, final String headerName, final String headerValue) {
        Mockito.doReturn(headerValue).when(context).header(headerName);
    }

    public static Map<String, Object> applyModel(final Context context) {
        ViewHelper.defaultVariables(context);
        return AbstractController.currentModel(context);
    }

    public static void setCurrentUser(final Context context, final User user) {
        context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY, user);
    }

    public static void setCurrentDatabase(final Context context, final Database database) {
        context.sessionAttribute(Keys.DB_KEY, database);
    }

    public static void setIP(final Context context, final String ip) {
        Mockito.doReturn(ip).when(context).ip();
    }
}
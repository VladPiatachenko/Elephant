package edu.sumdu.tss.elephant.utils;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.enums.Lang;
import edu.sumdu.tss.elephant.model.DbUserService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import edu.sumdu.tss.elephant.Preset;

public final class UserUtils {
    private UserUtils() { }

    public static User registerUser(final String email,
                                    final String password,
                                    final UserRole role) {
        User user = new User();
        String dbUsername = Preset.DB_USERNAME.get();
        String dbPassword = Preset.DB_PASSWORD.get();

        user.setRole(role.getValue());
        user.setPrivateKey(Preset.PRIVATE_KEY.get());
        user.setPublicKey(Preset.PUBLIC_KEY.get());
        user.setToken(Preset.UUID.get());
        user.setUsername(dbUsername);
        user.setDbPassword(dbPassword);

        user.setLogin(email);
        user.setPassword(password);
        user.setLanguage(Lang.EN.toString());
        UserService.save(user);

        UserService.initUserStorage(dbUsername);
        DbUserService.initUser(dbUsername, dbPassword);

        return user;
    }

    public static User registerUser() {
        return registerUser(
                Preset.EMAIL.get(),
                Preset.PASSWORD.get(),
                UserRole.UNCHEKED);
    }

    public static User registerUser(final UserRole role) {
        return registerUser(
                Preset.EMAIL.get(),
                Preset.PASSWORD.get(),
                role);
    }
}

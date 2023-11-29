package edu.sumdu.tss.elephant.helperunits;

import edu.sumdu.tss.elephant.helper.UserRole;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserRoleTest {

    private static void assertParams(
            final UserRole role,
            final long maxConnections,
            final long maxDB,
            final long maxStorage,
            final long maxBackupsPerDB,
            final long maxScriptsPerDB) {
        Assertions.assertAll(
                "Перевірка кількісних обмежень ролі на відповідність вимогам",
                () -> Assertions.assertEquals(role.maxConnections(), maxConnections, "Максимальна кількість з'єднань відповідає вимогам"),
                () -> Assertions.assertEquals(role.maxDB(), maxDB, "Максимальна кількість баз даних відповідає вимогам"),
                () -> Assertions.assertEquals(role.maxStorage(), maxStorage, "Максимальний дисковий простір відповідає вимогам"),
                () -> Assertions.assertEquals(role.maxBackupsPerDB(), maxBackupsPerDB, "Максимальна кількість бекапів відповідає вимогам"),
                () -> Assertions.assertEquals(role.maxScriptsPerDB(), maxScriptsPerDB, "Максимальна кількість сценаріїв відповідає вимогам")
        );
    }

    @Test
    public void roleAnyoneParams() {
        assertParams(UserRole.ANYONE, 0, 0, 0, 0, 0);
    }

    @Test
    public void roleUncheckedParams() {
        assertParams(UserRole.UNCHEKED, 0, 0, 0, 0, 0);
    }

    @Test
    public void roleBasicParams() {
        assertParams(UserRole.BASIC_USER, 5, 2, 20 * FileUtils.ONE_MB, 1, 2);
    }

    @Test
    public void roleProParams() {
        assertParams(UserRole.PROMOTED_USER, 5, 3, 50 * FileUtils.ONE_MB, 5, 5);
    }

    @Test
    public void roleAdminParams() {
        assertParams(UserRole.ADMIN, 5, 100, FileUtils.ONE_GB, 50, 50);
    }

    @Test
    public void byValue_AllCorrect_SuccessConversion() {
        long id;
        for (UserRole role : UserRole.values()) {
            id = role.getValue();
            Assertions.assertEquals(
                    role,
                    UserRole.byValue(id),
                    "Підтримується перетворення індекса в об'єкт ролі та навпаки"
            );
        }
    }

    @Test
    public void byValue_RoleNotExists_RuntimeException() {
        long id = -7;

        Assertions.assertThrows(
                RuntimeException.class,
                () -> {
                    UserRole.byValue(id);
                },
                "Якщо вказати неіснуючий id ролі - буде викинуте виключення RuntimeException"
        );
    }
}
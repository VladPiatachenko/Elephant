package edu.sumdu.tss.elephant.utils;


import edu.sumdu.tss.elephant.model.Backup;
import edu.sumdu.tss.elephant.model.BackupService;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.Preset;

public class BackupUtils {
    public static Backup createBackup(final Database database, final String pointName) {
        String dbName = database.getName();
        BackupService.perform(database.getOwner(), dbName, pointName);
        return BackupService.byName(dbName, pointName);
    }

    public static Backup createBackup(final Database database) {
        return createBackup(database, Preset.BACKUP_NAME.get());
    }
}

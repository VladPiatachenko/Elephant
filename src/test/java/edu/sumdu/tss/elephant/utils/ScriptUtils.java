package edu.sumdu.tss.elephant.utils;
import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.Script;
import edu.sumdu.tss.elephant.model.UserService;
import edu.sumdu.tss.elephant.Preset;
import org.apache.commons.io.FileUtils;
import org.sql2o.Connection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class ScriptUtils {
    public static Script createScript(final Database database, final String name, final String description, final String content) {
        String username = database.getOwner();
        String path = UserService.userStoragePath(username) +
                File.separator + "scripts" +
                File.separator + database.getName() +
                File.separator + StringUtils.randomAlphaString(20);
        File scriptFile = new File(path);

        try {
            FileUtils.forceMkdirParent(scriptFile);
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(content.getBytes()), scriptFile);
        } catch (IOException ex) {
            return null;
        }

        Script script = new Script();
        script.setFilename(name);
        script.setDescription(description);
        script.setSize(content.getBytes().length);
        script.setPath(path);
        script.setDatabase(database.getName());
        try (Connection con = DBPool.getConnection().open()) {
            Long key = con.createQuery("insert into scripts(database, filename, path, description) values (:database, :filename, :path, :description)", true).bind(script).executeUpdate().getKey(Long.class);
            script.setId(key);
        }

        return script;
    }

    public static Script createScript(final Database database) {
        return createScript(database, Preset.SCRIPT_NAME.get(), Preset.SCRIPT_DESCRIPTION.get(), "SELECT 1;");
    }
}
package cope.nebula.client.configs;

import cope.nebula.client.manager.PresetManager;
import cope.nebula.util.Globals;
import cope.nebula.util.internal.fs.FileUtil;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a boilerplate class for a configuration
 *
 * @author aesthetical
 * @since 4/9/22
 */
public abstract class AbstractConfig implements Globals {
    private final String fileName;

    public AbstractConfig(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Reads the config file and determines if we should save or load the config
     */
    public void read() {
        List<String> data = FileUtil.readFile(getFilePath());
        if (data == null || data.isEmpty()) {
            save();
        } else {
            load(String.join("\n", data));
        }
    }

    /**
     * Loads the data into the appropriate features
     * @param data the data from the text file
     */
    public abstract void load(String data);

    /**
     * Saves all data related to this configuration
     */
    public abstract void save();

    public Path getFilePath() {
        return PresetManager.getCurrentProfilePath().resolve(fileName);
    }
}

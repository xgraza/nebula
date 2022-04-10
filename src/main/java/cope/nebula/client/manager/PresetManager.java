package cope.nebula.client.manager;

import com.google.common.collect.Lists;
import cope.nebula.client.Nebula;
import cope.nebula.client.configs.AbstractConfig;
import cope.nebula.client.configs.impl.ModuleConfig;
import cope.nebula.util.internal.fs.FileUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Handles profile loading/saving/creation/deletion
 *
 * @author aesthetical
 * @since 4/9/22
 */
public class PresetManager {
    /**
     * The file path that will contain all the configurations created
     */
    private static final Path PROFILES_PATH = FileUtil.NEBULA.resolve("configs");

    /**
     * A file that contains the current profile
     */
    private static final Path CURRENT_PROFILE_PATH = PROFILES_PATH.resolve("profile.txt");

    /**
     * The current profile loaded in the client
     */
    private static String currentProfile = "default";
    private final ArrayList<AbstractConfig> configs = Lists.newArrayList(
            new ModuleConfig());

    public PresetManager() {
        // make sure the following directories/files exist
        if (!Files.exists(PROFILES_PATH)) {
            FileUtil.mkDir(PROFILES_PATH);
        }

        if (!Files.exists(CURRENT_PROFILE_PATH)) {
            FileUtil.touch(CURRENT_PROFILE_PATH, currentProfile);
        }

        if (!Files.exists(PROFILES_PATH.resolve("default"))) {
            FileUtil.mkDir(PROFILES_PATH.resolve("default"));
        }

        try {
            currentProfile = Objects.requireNonNull(FileUtil.readFile(CURRENT_PROFILE_PATH)).get(0);
        } catch (Exception e) {
            FileUtil.touch(CURRENT_PROFILE_PATH, currentProfile = "default");
        }

        if (!Files.exists(PROFILES_PATH.resolve(currentProfile))) {
            currentProfile = "default";
        }

        loadProfile();
    }

    /**
     * Loads the current profile.
     */
    public void loadProfile() {
        configs.forEach(AbstractConfig::read);
        Nebula.getLogger().info("Loaded profile {}", currentProfile);
    }

    /**
     * Saves the current profile
     * @param after what to do after saving each config in the profile
     */
    public void saveProfile(Supplier<Void> after) {
        configs.forEach(AbstractConfig::save);
        if (after != null) {
            after.get();
        }
    }

    public void setProfile(String profile) {
        if (!Files.exists(PROFILES_PATH.resolve(profile))) {
            FileUtil.mkDir(PROFILES_PATH.resolve(profile));
        }

        // save the profile
        saveProfile(() -> {
            currentProfile = profile;
            loadProfile();
            return null;
        });
    }

    /**
     * A list of all the profiles
     * @return all of the profiles
     */
    private List<String> getAllProfiles() {
        List<String> profiles = new ArrayList<>();

        File dir = PROFILES_PATH.toFile();
        if (dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (!file.isDirectory() || file.getName().equals("profile.txt")) {
                    continue;
                }

                profiles.add(file.getName());
            }
        }

        return profiles;
    }

    /**
     * Gets the current profile path
     * @return the current profile path
     */
    public static Path getCurrentProfilePath() {
        return PROFILES_PATH.resolve(currentProfile);
    }
}

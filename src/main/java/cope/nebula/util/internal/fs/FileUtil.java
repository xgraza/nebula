package cope.nebula.util.internal.fs;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Helps with file system related things
 *
 * Also stores important client paths
 *
 * @author aesthetical
 * @since 3/30/22
 */
public class FileUtil {
    public static final Path ROOT = Paths.get("");
    public static final Path NEBULA = ROOT.resolve("nebula");

    /**
     * Creates a directory
     * @param path the path to create the directory to
     */
    public static void mkDir(Path path) {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isRegularFile(path))) {
            return;
        }

        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a file
     * @param path the path to the file
     * @param content the contents of the file (null = empty)
     */
    public static void touch(Path path, @Nullable String content) {
        if (!Files.exists(path.getParent())) {
            return;
        }

        if (content == null || content.isEmpty()) {
            content = "";
        }

        OutputStream stream = null;
        try {
            stream = new FileOutputStream(path.toFile());
            stream.write(content.getBytes(StandardCharsets.UTF_8), 0, content.length());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads the contents of a file
     * @param path the path to the file
     * @return list of every line in the file
     */
    public static List<String> readFile(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            return null;
        }
    }
}

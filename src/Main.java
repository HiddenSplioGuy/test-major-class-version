import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) {

        String buildDir = args[0];
        System.out.println("buildDir : " + buildDir);

        AtomicReference<Integer> majorVersion = new AtomicReference<>(null);
        try {
            Files.walkFileTree(Path.of(buildDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().endsWith(".class")) {
                        try (InputStream in = new FileInputStream(file.toFile())) {
                            DataInputStream data = new DataInputStream(in);
                            if (0xCAFEBABE == data.readInt()) {
                                System.out.println(file.getFileName());
                                data.readUnsignedShort(); // minor version -> we don't care about it
                                majorVersion.set(data.readUnsignedShort());
                                return FileVisitResult.TERMINATE;
                            }
                        } catch (IOException ignored) {

                        }
                    }
                    // if this was not .class file or there was an error parsing its contents, we continue on to the next file
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {

        }

        System.out.println("majorFound : " + majorVersion.get());
    }
}
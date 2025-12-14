package com.example.encryptor7z;

import com.example.encryptor7z.EncryptorCli.CliRequest;
import com.example.utils.Utils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

final class ArchiveProcessor {

    private static final String CHECK_PREFIX = "CHECK FILE: ";

    void process(CliRequest request) {
        Objects.requireNonNull(request, "request");

        switch (request.operation()) {
            case ENCRYPT -> encrypt(request.input(), request.output(), request.password());
            case DECRYPT -> decrypt(request.input(), request.output(), request.password());
        }
    }

    private void encrypt(Path input, Path output, String password) {
        log("Preparing to encrypt", input, output);
        runProcessor(input, output, password, Mode.ENCRYPT);
    }

    private void decrypt(Path input, Path output, String password) {
        log("Preparing to decrypt", input, output);
        runProcessor(input, output, password, Mode.DECRYPT);
    }

    private void runProcessor(Path input, Path output, String password, Mode mode) {
        Encryption.setPassword(String.valueOf(password));

        try {
            Files.walkFileTree(input, new ProcessingVisitor(input, output, mode));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to process files", exception);
        }
    }

    private void log(String action, Path input, Path output) {
        System.out.printf("%s (%tT) from %s to %s%n", action, new Date(), input, output);
    }

    private void log(String action, Path source, Path destination, String details) {
        System.out.printf("%s (%tT) %s -> %s%s%n", action, new Date(), source, destination, details);
    }

    private enum Mode {
        ENCRYPT,
        DECRYPT
    }

    private final class ProcessingVisitor extends SimpleFileVisitor<Path> {
        private final Path sourceRoot;
        private final Path targetRoot;
        private final Mode mode;

        ProcessingVisitor(Path sourceRoot, Path targetRoot, Mode mode) {
            this.sourceRoot = sourceRoot;
            this.targetRoot = targetRoot;
            this.mode = mode;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetDir = resolveTarget(dir);
            Files.createDirectories(targetDir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path targetDir = resolveTarget(file).getParent();
            Files.createDirectories(targetDir);
            processFile(file, targetDir);
            return FileVisitResult.CONTINUE;
        }

        private Path resolveTarget(Path path) {
            var relative = sourceRoot.relativize(path);
            return targetRoot.resolve(relative);
        }

        private void processFile(Path source, Path targetDir) {
            try {
                var category = FileCategory.from(source, mode);
                Path destination = category.processor().apply(source, targetDir, mode);
                //log("Processed", source, destination, "");
                verifySize(source, destination);
            } catch (Exception exception) {
                log("Failed", source, targetDir, ": " + exception.getMessage());
            }
        }

        private void verifySize(Path source, Path destination) throws IOException {
            if (destination == null || !Files.exists(destination) || Files.isDirectory(destination)) {
                return;
            }

            long sourceSize = Files.size(source);
            long destinationSize = Files.size(destination);
            long difference = sourceSize - destinationSize;

            boolean exceedsAbsolute = difference > 1200;
			boolean exceedsRelative = false;
            if (sourceSize > 0 && difference > 0) {
                double delta = (double) difference / (double) sourceSize;
                exceedsRelative = delta > 0.0125d;
            }

			boolean exceedsThreshold = exceedsAbsolute && exceedsRelative;

            if (exceedsThreshold) {
                System.out.println(CHECK_PREFIX + destination.getFileName());
            }
        }
    }

    private enum FileCategory {
        IMAGE {
            @Override
            Path process(Path source, Path targetDir, Mode mode) throws IOException {
                return Encryption.encryptImage(source, targetDir);
            }
        },
        ENCRYPTED_IMAGE {
            @Override
            Path process(Path source, Path targetDir, Mode mode) throws IOException {
                return Encryption.decryptImage(source, targetDir);
            }
        },
        ARCHIVE {
            @Override
            Path process(Path source, Path targetDir, Mode mode) {
                if (mode == Mode.ENCRYPT) {
                    return Encryption.encryptArchive(source, targetDir);
                } else {
	                return Encryption.decryptArchive(source, targetDir);
                }
            }
        },
        OTHER {
            @Override
            Path process(Path source, Path targetDir, Mode mode) throws IOException {
                Path destination = Encryption.buildCopyPath(source, targetDir);
                Files.copy(source, destination, REPLACE_EXISTING, COPY_ATTRIBUTES);
                return destination;
            }
        };

        PathProcessor processor() {
            return this::process;
        }

        abstract Path process(Path source, Path targetDir, Mode mode) throws IOException;

        static FileCategory from(Path source, Mode mode) {
            String extension = FilenameUtils.getExtension(source.getFileName().toString()).toLowerCase();
            if (mode == Mode.DECRYPT) {
                if (Utils.isEncrypted(extension)) {
                    return ENCRYPTED_IMAGE;
                }
                return Utils.isArchive(extension) ? ARCHIVE : OTHER;
            }
            if (Utils.isImage(extension)) {
                return IMAGE;
            }
            if (Utils.isArchive(extension)) {
                return ARCHIVE;
            }
            return OTHER;
        }

    }

    @FunctionalInterface
    private interface PathProcessor {
        Path apply(Path source, Path targetDir, Mode mode) throws IOException;
    }
}

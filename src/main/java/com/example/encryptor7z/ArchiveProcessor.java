package com.example.encryptor7z;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

import com.example.encryptor7z.EncryptorCli.CliRequest;
import com.example.encryptor7z.EncryptorCli.Operation;
import com.example.utils.EncodeName;

final class ArchiveProcessor {

    private static final String CHECK_PREFIX = "CHECK FILE: ";

    void process(CliRequest request) {
        Objects.requireNonNull(request, "request");

        switch (request.operation()) {
            case ENCRYPT -> encrypt(request.input(), request.output(), request.password());
            case DECRYPT -> decrypt(request.input(), request.output(), request.password());
        }
    }

    private void encrypt(Path input, Path output, char[] password) {
        log("Preparing to encrypt", input, output);
        runProcessor(input, output, password, Mode.ENCRYPT);
    }

    private void decrypt(Path input, Path output, char[] password) {
        log("Preparing to decrypt", input, output);
        runProcessor(input, output, password, Mode.DECRYPT);
    }

    private void runProcessor(Path input, Path output, char[] password, Mode mode) {
        Encryption.setPassword(String.valueOf(password));
        scrub(password);

        try {
            Files.walkFileTree(input, new ProcessingVisitor(input, output, mode));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to process files", exception);
        }
    }

    private void log(String action, Path input, Path output) {
        System.out.printf("%s (%s) from %s to %s%n", action, Instant.now(), input, output);
    }

    private void log(String action, Path source, Path destination, String details) {
        System.out.printf("%s (%s) %s -> %s%s%n", action, Instant.now(), source, destination, details);
    }

    private void scrub(char[] password) {
        if (password == null) {
            return;
        }
        for (int index = 0; index < password.length; index++) {
            password[index] = '\0';
        }
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
                log("Processed", source, destination, "");
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
            long difference = Math.abs(sourceSize - destinationSize);

            boolean exceedsThreshold = difference > 1000;
            if (sourceSize > 0) {
                double delta = (double) difference / (double) sourceSize;
                exceedsThreshold |= delta > 0.01d;
            }

            if (exceedsThreshold) {
                System.out.println(CHECK_PREFIX + destination.getFileName());
            }
        }
    }

    private enum FileCategory {
        IMAGE {
            @Override
            Path process(Path source, Path targetDir, Mode mode) throws IOException {
                Path destination = buildEncryptedPath(source, targetDir);
                Encryption.encryptImage(source, targetDir);
                return destination;
            }
        },
        ENCRYPTED_IMAGE {
            @Override
            Path process(Path source, Path targetDir, Mode mode) throws IOException {
                Path destination = buildDecryptedPath(source, targetDir);
                Encryption.decryptImage(source, targetDir);
                return destination;
            }
        },
        ARCHIVE {
            @Override
            Path process(Path source, Path targetDir, Mode mode) {
                Path destination = buildArchivePath(source, targetDir);
                if (mode == Mode.ENCRYPT) {
                    Encryption.encryptArchive(source, targetDir);
                } else {
                    Encryption.decryptArchive(source, targetDir);
                }
                return destination;
            }
        },
        OTHER {
            @Override
            Path process(Path source, Path targetDir, Mode mode) throws IOException {
                Path destination = buildCopyPath(source, targetDir);
                Files.copy(source, destination, REPLACE_EXISTING, COPY_ATTRIBUTES);
                return destination;
            }
        };

        PathProcessor processor() {
            return this::process;
        }

        abstract Path process(Path source, Path targetDir, Mode mode) throws IOException;

        static FileCategory from(Path source, Mode mode) {
            String extension = FilenameUtils.getExtension(source.getFileName().toString())
                    .toLowerCase(Locale.ROOT);
            if (mode == Mode.DECRYPT) {
                if ("cry".equals(extension)) {
                    return ENCRYPTED_IMAGE;
                }
                return isArchive(extension) ? ARCHIVE : OTHER;
            }
            if (isImage(extension)) {
                return IMAGE;
            }
            if (isArchive(extension)) {
                return ARCHIVE;
            }
            return OTHER;
        }

        private static boolean isImage(String extension) {
            return switch (extension) {
                case "jpg", "jpeg", "png", "gif" -> true;
                default -> false;
            };
        }

        private static boolean isArchive(String extension) {
            return switch (extension) {
                case "zip", "rar", "7z" -> true;
                default -> false;
            };
        }

        private static Path buildEncryptedPath(Path source, Path targetDir) {
            String encodedName = EncodeName.encrypt(source.getFileName().toString()) + ".cry";
            return uniquePath(targetDir, encodedName);
        }

        private static Path buildDecryptedPath(Path source, Path targetDir) {
            String decodedName = EncodeName.decrypt(FilenameUtils.getBaseName(source.getFileName().toString()));
            return uniquePath(targetDir, decodedName);
        }

        private static Path buildArchivePath(Path source, Path targetDir) {
            return uniquePath(targetDir, source.getFileName().toString());
        }

        private static Path buildCopyPath(Path source, Path targetDir) {
            return uniquePath(targetDir, source.getFileName().toString());
        }

        private static Path uniquePath(Path directory, String desiredName) {
            Path candidate = directory.resolve(desiredName);
            if (!Files.exists(candidate)) {
                return candidate;
            }
            String base = FilenameUtils.getBaseName(desiredName);
            String extension = FilenameUtils.getExtension(desiredName);
            String extPart = extension.isBlank() ? "" : "." + extension;
            int counter = 1;
            Path result = candidate;
            while (Files.exists(result)) {
                String candidateName = "%s_%d%s".formatted(base, counter++, extPart);
                result = directory.resolve(candidateName);
            }
            return result;
        }
    }

    @FunctionalInterface
    private interface PathProcessor {
        Path apply(Path source, Path targetDir, Mode mode) throws IOException;
    }
}

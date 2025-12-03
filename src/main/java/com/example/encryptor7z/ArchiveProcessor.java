package com.example.encryptor7z;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

import com.example.encryptor7z.EncryptorCli.CliRequest;
import com.example.encryptor7z.EncryptorCli.Operation;

final class ArchiveProcessor {

    void process(CliRequest request) {
        Objects.requireNonNull(request, "request");

        if (request.operation() == Operation.ENCRYPT) {
            encrypt(request.input(), request.output(), request.password());
        } else {
            decrypt(request.input(), request.output(), request.password());
        }
    }

    private void encrypt(Path input, Path output, char[] password) {
        log("Preparing to encrypt", input, output);
        // Integrate SevenZipJBinding here to build encrypted archives.
        // The windows-amd64 native binding is present as a dependency for archive handling.
    }

    private void decrypt(Path input, Path output, char[] password) {
        log("Preparing to decrypt", input, output);
        // Integrate SevenZipJBinding here to read encrypted archives.
        // Ensure password handling stays confined to this method.
    }

    private void log(String action, Path input, Path output) {
        System.out.printf("%s (%s) from %s to %s%n", action, Instant.now(), input, output);
    }
}

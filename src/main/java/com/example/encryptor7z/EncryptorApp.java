package com.example.encryptor7z;

public final class EncryptorApp {
    private EncryptorApp() {
    }

    public static void main(String[] args) {
        var archiveProcessor = new ArchiveProcessor();
        var cli = new EncryptorCli(archiveProcessor);
        cli.run(args);
    }
}

# encryptor7z

A starter Java 17 command-line tool scaffolded with Gradle for encrypting and decrypting archives.

## Features
- Java 17 project configured with the Gradle (Groovy) build tool.
- Apache Commons CLI for ergonomic argument parsing.
- SevenZipJBinding (windows-amd64) dependency included for handling 7z and zip archives.

## Getting started
1. Install a Java 17 JDK.
2. Build or run the application with Gradle:
   ```bash
   ./gradlew run --args "-m encrypt -i ./path/to/input.zip -o ./path/to/output.7z -p secret"
   ```

## Building an executable fat JAR
To produce a runnable JAR that bundles all dependencies, use the Shadow plugin task:

```bash
./gradlew shadowJar
```

The resulting artifact is written to `build/libs/` with an `-all` classifier (for example, `encryptor7z-0.0.1-SNAPSHOT-all.jar`). Run it with your desired arguments:

```bash
java -jar build/libs/encryptor7z-0.0.1-SNAPSHOT-all.jar -m encrypt -i ./path/to/input.zip -o ./path/to/output.7z -p secret
```

## Command-line options
- `-m, --mode` — `encrypt` or `decrypt`.
- `-i, --input` — path to the source archive.
- `-o, --output` — path for the resulting archive.
- `-p, --password` — password used for protection/unlocking.

Archive handling is orchestrated in `ArchiveProcessor`, ready for SevenZipJBinding integration.

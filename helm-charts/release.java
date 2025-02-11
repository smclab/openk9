///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS info.picocli:picocli:4.6.3
//DEPS com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.2
//DEPS com.fasterxml.jackson.core:jackson-core:2.13.2

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "release",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Increase appVersion and image version"
)
public class release implements Callable<Integer> {

    static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
    }

    @CommandLine.Option(names = {"-p", "--print"}, arity = "0",description = "print current version")
    private boolean print;

    @CommandLine.Option(names = "-v", arity = "0", description = "enabled debug log")
    private boolean debug;

    @CommandLine.Option(
        names = {"-e", "--exclude"},
        arity = "1",
        description = "exclude pattern", defaultValue = "")
    private String excludePattern;

    @CommandLine.Option(
        names = {"--app-version"},
        arity = "1",
        description = "Chart appVersion",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String appVersion;

    @CommandLine.Option(
        names = {"--version"},
        arity = "1",
        description = "Chart vertsion",
        defaultValue = CommandLine.Option.NULL_VALUE
    )
    private String version;

    @Override
    public Integer call() throws Exception {

        List<PathJsonNode> pathJsonNodes = _getChartPathJsonNode();

        pathJsonNodes = pathJsonNodes
            .stream()
            .filter(e -> !e.getPath().toString().matches(excludePattern))
            .collect(Collectors.toList());

        if (print) {
            for (PathJsonNode pathJsonNode : pathJsonNodes) {

                Path path = pathJsonNode.getPath();

                JsonNode jsonNode = pathJsonNode.getJsonNode();

                System.out.println(
                    path +
                    " appVersion: " + jsonNode.get("appVersion").asText() +
                    ", version: " + jsonNode.get("version").asText());

            }

            return 0;
        }

        if (appVersion == null && version == null) {
            System.err.println("version or appVersion is required");
            return 1;
        }

        if (appVersion != null && version == null) {
            version = appVersion;
        }

        if (appVersion == null) {
            appVersion = version;
        }

        if (debug) {
            System.out.println("Charts found");
            for (PathJsonNode pathJsonNode : pathJsonNodes) {
                System.out.println(pathJsonNode.getPath());
            }
        }

        if (debug) {
            System.out.println("Start backup");
        }

        _createBackups(pathJsonNodes);

        if (debug) {
            System.out.println("Start update chart versions");
        }

        _updateChartVersions(pathJsonNodes);

        if (debug) {
            System.out.println("Start delete backup");
        }

        _deleteBackup(pathJsonNodes);

        return 0;

    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new release()).execute(args);
        System.exit(exitCode);
    }

    private void _updateChartVersions(
        List<PathJsonNode> pathJsonNodes) {

        try {

            for (PathJsonNode pathJsonNode : pathJsonNodes) {
                JsonNode jsonNode = pathJsonNode.getJsonNode();
                ObjectNode copy = jsonNode.deepCopy();

                Path path = pathJsonNode.getPath();

                copy.put("appVersion", version);
                copy.put("version", appVersion);

                mapper.writeValue(path.toFile(), copy);

                if (debug) {

                    System.out.println(
                        path + " prev version: " + copy.get("appVersion").asText() + " new version: " +
                        version);

                }

            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void _deleteBackup(List<PathJsonNode> pathJsonNodes) {

        try {
            for (PathJsonNode pathJsonNode : pathJsonNodes) {
                Path path = pathJsonNode.getPath();

                Path backupPath = _getBackupPath(path);

                File backupFile = backupPath.toFile();

                if (debug) {
                    System.out.println("delete backup file: " + backupPath);
                }

                backupFile.deleteOnExit();

            }

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Path _getBackupPath(Path path) {
        File file = path.toFile();
        return path.getParent().resolve(file.getName() + ".backup");
    }

    private void _createBackups(List<PathJsonNode> pathJsonNodes) {

        try {
            for (PathJsonNode pathJsonNode : pathJsonNodes) {
                Path path = pathJsonNode.getPath();
                byte[] originalContent = pathJsonNode.getOriginalContent();

                Path backupPath = _getBackupPath(path);

                File backupFile = backupPath.toFile();

                if (backupFile.exists()) {
                    throw new RuntimeException(
                        "backup files exist " + backupPath);
                }

                if (debug) {
                    System.out.println("create backup file: " + backupPath);
                }

                Files.write(backupFile.toPath(), originalContent);

            }

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private List<PathJsonNode> _getChartPathJsonNode() {

        try(Stream<Path> paths = Files.walk(Paths.get("."))) {

            List<Path> charts =
                paths
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toFile().getName().matches("Chart\\.(yml|yaml)"))
                    .collect(Collectors.toList());

            List<PathJsonNode> pathJsonNodes = new ArrayList<>(charts.size());

            for (Path chart : charts) {
                byte[] bytes = Files.readAllBytes(chart);
                JsonNode jsonNode = mapper.readTree(bytes);
                pathJsonNodes.add(new PathJsonNode(jsonNode, chart, bytes));
            }

            return pathJsonNodes;

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static class PathJsonNode {

        private PathJsonNode(JsonNode jsonNode, Path path, byte[] originalFile) {
            this.jsonNode = jsonNode;
            this.path = path;
            this.originalContent = originalFile;
        }

        public JsonNode getJsonNode() {
            return jsonNode;
        }

        public Path getPath() {
            return path;
        }

        public byte[] getOriginalContent() {
            return originalContent;
        }

        private final JsonNode jsonNode;
        private final Path path;

        private final byte[] originalContent;

    }

}

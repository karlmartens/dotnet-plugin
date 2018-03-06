package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Publish extends DotnetDefaultTask {

    private boolean _testUpToDate = true;
    private boolean _includeSymbols = false;
    private String _charset = "UTF-8";
    private String _nugetExecutable = "nuget";
    private List<Replacement> _replacements = new ArrayList<>();
    private String _repository = "";
    private String _apiKey = "";

    public void addReplacement(String attribute, String value) {
        _replacements.add(new Replacement(attribute, value));
    }

    public void setIncludeSymbols(boolean includeSymbols) {
        _includeSymbols = includeSymbols;
    }

    public void setCharset(String charset) {
        _charset = Objects.requireNonNull(charset);
    }

    public void setNugetExecutable(String executable) {
        _nugetExecutable = executable;
    }

    public void setRepository(String repository) {
        _repository = Objects.requireNonNull(repository);
    }

    public void setApiKey(String apiKey) {
        _apiKey = Objects.requireNonNull(apiKey);
    }

    public void setTestUpToDate(boolean b) {
        _testUpToDate = b;
    }

    @TaskAction
    void exec() {
        DotnetExtension ext = getExtension();
        String baseDir = directoryName(ext.getSolution());
        getProject().fileTree(baseDir, t -> {
            t.include(ext.getProjectPattern());
            t.exclude(ext.getTestPattern());
        }).forEach(this::runPublish);
    }

    private void runPublish(File file) {
        Charset charset = Charset.forName(_charset);
        String original = readContents(file, charset);

        String updated = doReplacements(original);

        String packageId = extractTag(updated, "PackageId");
        String version = extractTag(updated, "PackageVersion");
        if (uptoDate(packageId, version)) {
            System.out.println(String.format("Skipping %s(%s) was already published.", packageId, version));
            return;
        }

        if (!_replacements.isEmpty())
            writeContents(file, updated, charset);

        File packagePath = doPack(file);

        if (!_replacements.isEmpty())
            writeContents(file, original, charset);

        doPush(packagePath);
    }

    private boolean uptoDate(String packageId, String version) {
        if (!_testUpToDate)
            return false;

        System.out.println(String.format("Checking '%s' for version '%s' is already published.", packageId, version));

        try  (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            getProject().exec(execSpec -> {
                execSpec.setExecutable(_nugetExecutable);

                List<String> args = new ArrayList<>();
                args.add("list");
                args.add(packageId);
                args.add("-Source");
                args.add(_repository);
                args.add("-Prerelease");
                args.add("-AllVersions");
                args.add("-NonInteractive");

                execSpec.setArgs(args);
                execSpec.setStandardOutput(out);
            });

            String output = out.toString();
            return output.contains(version);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String doReplacements(String original) {
        String updated = original;
        for (Replacement r : _replacements) {
            updated = updated.replaceAll("(<" + r._attribute + ">).*(</" + r._attribute + ">)", "$1" + r._value + "$2");
        }
        return updated;
    }

    private File doPack(File file) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DotnetExtension ext = getExtension();
            getProject().exec(execSpec -> {
                execSpec.setExecutable(ext.getExecutable());

                List<String> args = new ArrayList<>();
                args.add("pack");
                args.add(file.getAbsolutePath());
                args.add("--no-restore");
                args.add("--no-build");
                when(_includeSymbols, () -> args.add("--include-symbols"));

                execSpec.setArgs(args);
                execSpec.setStandardOutput(out);
            });

            String output = out.toString();
            String fileExt = ".nupkg";
            if (_includeSymbols)
                fileExt = ".symbols" + fileExt;

            Pattern pattern = Pattern.compile("Successfully created package '(.*" + fileExt + ")'.");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                String packagePath = matcher.group(1);
                System.out.println(String.format("Package '%s' created.", packagePath));
                return new File(packagePath);
            }
            return null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void doPush(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new RuntimeException("Failed to generate nuget package. Nothing to publish.");
        }

        System.out.println(String.format("Publishing '%s' to repository '%s'", file.getAbsoluteFile(), _repository));
        DotnetExtension ext = getExtension();
        getProject().exec(execSpec -> {
            execSpec.setExecutable(ext.getExecutable());

            List<String> args = new ArrayList<>();
            args.add("nuget");
            args.add("push");
            args.add(file.getAbsolutePath());
            args.add("--source");
            args.add(_repository);
            args.add("--api-key");
            args.add(_apiKey);
            execSpec.setArgs(args);
        });
    }

    private static String extractTag(String str, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">(.*)</" + tag + ">");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String readContents(File file, Charset charset) {
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            return new String(content, charset);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void writeContents(File file, String content, Charset charset) {
        try {
            Files.write(file.toPath(), content.getBytes(charset), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class Replacement {
        private String _attribute;
        private String _value;

        private Replacement(String attribute, String value) {
            _attribute = Objects.requireNonNull(attribute);
            _value = Objects.requireNonNull(value);
        }
    }
}

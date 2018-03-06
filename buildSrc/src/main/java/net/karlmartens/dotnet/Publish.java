package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Publish extends DotnetDefaultTask {

    public static final String PACKAGE_ID = "PackageId";
    public static final String PACKAGE_VERSION = "PackageVersion";
    private boolean _testUpToDate = true;
    private boolean _includeSymbols = false;
    private boolean _includeSource = false;
    private String _charset = "UTF-8";
    private String _nugetExecutable = "nuget";
    private List<AttributeValue> _parameters = new ArrayList<>();
    private String _repository = "";
    private String _apiKey = "";

    public void addParameter(String attribute, String value) {
        _parameters.add(new AttributeValue(attribute, value));
    }

    public void setIncludeSymbols(boolean include) {
        _includeSymbols = include;
    }

    public void setIncludeSource(boolean include) {
        _includeSource = include;
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

        File projectDir = getProject().getProjectDir();
        getProject().fileTree(projectDir.toString(), t -> {
            t.include(ext.getProjectPattern());
            t.exclude(ext.getTestPattern());
        }).forEach(this::runPublish);
    }

    private void runPublish(File file) {
        if (_testUpToDate) {
            VersionInfo version = determineVersion(file);
            System.out.println(String.format("Checking %s is already published.", version));
            if (uptoDate(version)) {
                System.out.println(String.format("Skipping %s was already published.", version));
                return;
            }
        }

        File packagePath = doPack(file);

        doPush(packagePath);
    }

    private VersionInfo determineVersion(File file) {
        Charset charset = Charset.forName(_charset);
        String original = readContents(file, charset);

        Map<String, String> params = new HashMap<>();
        whenHasValue(extractTag(original, PACKAGE_ID), v -> params.put(PACKAGE_ID, v));
        whenHasValue(extractTag(original, PACKAGE_VERSION), v -> params.put(PACKAGE_VERSION, v));

        for (AttributeValue av : _parameters) {
            whenHasValue(av._value, v -> params.put(av._attribute, v));
        }

        String packageId = params.get(PACKAGE_ID);
        String packageVersion = params.get(PACKAGE_VERSION);
        if (packageId == null || packageVersion == null)
            return null;

        return new VersionInfo(packageId, packageVersion);
    }

    private boolean uptoDate(VersionInfo info) {
        try  (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            getProject().exec(execSpec -> {
                execSpec.setExecutable(_nugetExecutable);

                List<String> args = new ArrayList<>();
                args.add("list");
                args.add(info._packageId);
                args.add("-Source");
                args.add(_repository);
                args.add("-Prerelease");
                args.add("-AllVersions");
                args.add("-NonInteractive");

                execSpec.setArgs(args);
                execSpec.setStandardOutput(out);
            });

            String output = out.toString();
            return output.contains(info._version);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private File doPack(File file) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DotnetExtension ext = getExtension();
            getProject().exec(execSpec -> {
                execSpec.setExecutable(ext.getExecutable());

                List<String> args = new ArrayList<>();
                args.add("pack");
                args.add(file.getAbsolutePath());
                whenHasValue(ext.getConfiguration(), addNamedParameter(args, "--configuration"));
                whenHasValue(ext.getRuntime(), addNamedParameter(args, "--runtime"));
                args.add("--no-restore");
                args.add("--no-build");
                when(_includeSymbols, addParameter(args,"--include-symbols"));
                when(_includeSource, addParameter(args,"--include-symbols"));

                for (AttributeValue av : _parameters) {
                    args.add(String.format("/p:%s=\"%s\"", av._attribute, av._value));
                }

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

    private static class VersionInfo {
        private String _packageId;
        private String _version;

        private VersionInfo(String packageId, String version) {
            _packageId= Objects.requireNonNull(packageId);
            _version = Objects.requireNonNull(version);
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", _packageId, _version);
        }
    }

    private static class AttributeValue {
        private String _attribute;
        private String _value;

        private AttributeValue(String attribute, String value) {
            _attribute = Objects.requireNonNull(attribute);
            _value = Objects.requireNonNull(value);
        }
    }
}

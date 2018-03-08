package net.karlmartens.dotnet;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Publish extends DotnetDefaultTask {

    private static Logger LOGGER = Logging.getLogger(Publish.class);

    private boolean _testUpToDate = true;
    private String _charset = "UTF-8";
    private String _nugetExecutable = "nuget";
    private String _repository = "";
    private String _apiKey = "";

    public void setCharset(String charset) {
        _charset = charset;
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
            t.include(ext.getPackagePattern());
        }).forEach(this::runPublish);
    }

    private void runPublish(File file) {
        if (_testUpToDate) {
            VersionInfo version = determineVersion(file);
            LOGGER.quiet("Checking {} for already being published.", version);
            if (uptoDate(version)) {
                LOGGER.quiet("Skipping {} was already published.", version);
                return;
            }
        }

        doPush(file);
    }

    private VersionInfo determineVersion(File file) {
        Charset charset = Charset.forName(_charset);
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entryEnumeration = zip.entries();
            while (entryEnumeration.hasMoreElements()) {
                ZipEntry entry = entryEnumeration.nextElement();
                if (entry.getName().endsWith("nuspec")) {
                    byte[] data = new byte[(int) entry.getSize()];
                    zip.getInputStream(entry).read(data);

                    String str = new String(data, charset);
                    String id = extractTag(str, "id");
                    String version = extractTag(str, "version");
                    return new VersionInfo(id, version);
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Unable to determine package id and version.", ex);
            throw new RuntimeException(ex);
        }

        LOGGER.error("Unable to determine package id and version.");
        throw new RuntimeException("Unable to determine package id and version");
    }

    private boolean uptoDate(VersionInfo info) {
        try  (ByteArrayOutputStream out = new ByteArrayOutputStream();
              ByteArrayOutputStream err = new ByteArrayOutputStream()) {
            try {
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
            } catch (Throwable t) {
                LOGGER.error(out.toString());
                LOGGER.error(err.toString());
                LOGGER.error("", t);
                throw new RuntimeException(t);
            }

            String output = out.toString();
            return output.contains(info._version);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void doPush(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new RuntimeException("Failed to generate nuget package. Nothing to publish.");
        }

        LOGGER.quiet("Publishing '{}' to repository '{}'", file.getAbsoluteFile(), _repository);
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
}

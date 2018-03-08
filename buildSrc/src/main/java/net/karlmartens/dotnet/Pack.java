package net.karlmartens.dotnet;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pack extends DotnetDefaultTask {
    private static Logger LOGGER = Logging.getLogger(Publish.class);

    private boolean _includeSymbols = false;
    private boolean _includeSource = false;

    public void setIncludeSymbols(boolean include) {
        _includeSymbols = include;
    }

    public void setIncludeSource(boolean include) {
        _includeSource = include;
    }

    @TaskAction
    void exec() {
        DotnetExtension ext = getExtension();

        File projectDir = getProject().getProjectDir();
        getProject().fileTree(projectDir.toString(), t -> {
            t.include(ext.getProjectPattern());
            t.exclude(ext.getTestPattern());
        }).forEach(this::doPack);
    }

    private void doPack(File file) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ByteArrayOutputStream err = new ByteArrayOutputStream()) {
            DotnetExtension ext = getExtension();
            try {
                getProject().exec(execSpec -> {
                    execSpec.setExecutable(ext.getExecutable());

                    List<String> args = new ArrayList<>();
                    args.add("pack");
                    args.add(file.getAbsolutePath());
                    whenHasValue(ext.getConfiguration(), addNamedParameter(args, "--configuration"));
                    whenHasValue(ext.getRuntime(), addNamedParameter(args, "--runtime"));
                    args.add("--no-restore");
                    args.add("--no-build");
                    when(_includeSymbols, addParameter(args, "--include-symbols"));
                    when(_includeSource, addParameter(args, "--include-symbols"));
                    appendParameters(args);

                    execSpec.setArgs(args);
                    execSpec.setStandardOutput(out);
                    execSpec.setErrorOutput(err);
                });
            } catch (Throwable t) {
                LOGGER.error("\n" + out.toString());
                LOGGER.error(err.toString());
                LOGGER.error("", t);
                throw new RuntimeException(t);
            }

            Pattern pattern = Pattern.compile("Successfully created package '(.*\\.nupkg)'.");
            Matcher matcher = pattern.matcher(out.toString());
            while (matcher.find()) {
                String packagePath = matcher.group(1);
                LOGGER.quiet("Package '{}' created.", packagePath);
            }
        } catch (IOException t) {
            throw new RuntimeException(t);
        }
    }

}

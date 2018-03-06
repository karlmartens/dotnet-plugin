package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Test extends DotnetDefaultTask {

    @TaskAction
    void exec() {
        DotnetExtension ext = getExtension();

        File projectDir = getProject().getProjectDir();
        getProject().fileTree(projectDir.toString(), t -> {
            t.include(ext.getTestPattern());
        }).forEach(this::runTest);
    }

    private void runTest(File file) {
        DotnetExtension ext = getExtension();

        getProject().exec(execSpec -> {
            execSpec.setExecutable(ext.getExecutable());

            List<String> args = new ArrayList<>();
            args.add("test");
            args.add(file.getAbsolutePath());
            whenHasValue(ext.getConfiguration(), addNamedParameter(args, "--configuration"));
            whenHasValue(ext.getFramework(), addNamedParameter(args, "--framework"));
            args.add("--no-restore");
            args.add("--no-build");
            args.add("--logger");
            args.add(String.format(Locale.US, "\"trx;LogFileName=%s.trx\"", file.getName()));
            args.add("--results-directory");
            args.add("../TestResults");

            execSpec.setArgs(args);
        });
    }

}

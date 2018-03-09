package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Clean extends DotnetDefaultTask {

    @TaskAction
    void exec() {
        DotnetExtension ext = getExtension();

        getProject().exec(execSpec -> {
            execSpec.setExecutable(ext.getExecutable());

            List<String> args = new ArrayList<>();
            args.add("clean");
            whenHasValue(ext.getSolution(), args::add);
            whenHasValue(ext.getConfiguration(), addNamedParameter(args, "--configuration"));
            whenHasValue(ext.getFramework(), addNamedParameter(args, "--framework"));
            whenHasValue(ext.getRuntime(), addNamedParameter(args, "--runtime"));

            execSpec.setArgs(args);
        });

        cleanTestResults(ext.getSolution());
        cleanPackages();
    }

    private void cleanPackages() {
        DotnetExtension ext = getExtension();

        File projectDir = getProject().getProjectDir();
        getProject().fileTree(projectDir.toString(), t -> {
            t.include(ext.getPackagePattern());
        }).forEach(File::delete);
    }

    private void cleanTestResults(String solution) {
        String baseDir = directoryName(solution);
        getProject().fileTree(baseDir, t -> {
            t.include("TestResults/**/*.trx");
        }).forEach(File::delete);
    }
}

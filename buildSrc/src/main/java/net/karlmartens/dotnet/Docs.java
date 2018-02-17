package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

public class Docs extends DotnetDefaultTask {
    @TaskAction
    void exec() {
        DotnetExtension ext = getExtension();

        getProject().exec(execSpec -> {
            execSpec.setExecutable(ext.getDocsExecutable());

            List<String> args = new ArrayList<>();
            args.add("metadata");
            args.add(ext.getDocsSrc());
            execSpec.setArgs(args);
        });

        getProject().exec(execSpec -> {
            execSpec.setExecutable(ext.getDocsExecutable());

            List<String> args = new ArrayList<>();
            args.add("build");
            args.add(ext.getDocsSrc());
            execSpec.setArgs(args);
        });
    }
}

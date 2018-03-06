package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

public class Restore extends DotnetDefaultTask {

    @TaskAction
    void exec() {
        getProject().exec(execSpec -> {
            DotnetExtension ext = getExtension();
            execSpec.setExecutable(ext.getExecutable());

            List<String> args = new ArrayList<>();
            args.add("restore");
            whenHasValue(ext.getSolution(), args::add);
            whenHasValue(ext.getRuntime(), addNamedParameter(args, "--runtime"));

            execSpec.setArgs(args);
        });
    }

}

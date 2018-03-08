package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

public class Compile extends DotnetDefaultTask {

    @TaskAction
    void exec() {
        getProject().exec(execSpec -> {
            DotnetExtension ext = getExtension();
            execSpec.setExecutable(ext.getExecutable());

            List<String> args = new ArrayList<>();
            args.add("build");
            whenHasValue(ext.getSolution(), args::add);
            whenHasValue(ext.getConfiguration(), addNamedParameter(args, "--configuration"));
            whenHasValue(ext.getFramework(), addNamedParameter(args, "--framework"));
            whenHasValue(ext.getRuntime(), addNamedParameter(args, "--runtime"));
            args.add("--no-restore");
            appendParameters(args);

            execSpec.setArgs(args);
        });
    }

}

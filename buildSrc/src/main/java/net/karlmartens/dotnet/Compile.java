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
            args.add("--no-restore");
            whenHasValue(ext.getSolution(), args::add);

            execSpec.setArgs(args);
        });
    }

}

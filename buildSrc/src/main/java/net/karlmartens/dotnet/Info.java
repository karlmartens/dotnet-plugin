package net.karlmartens.dotnet;

import org.gradle.api.tasks.TaskAction;

import java.util.Arrays;

public class Info extends DotnetDefaultTask {

    @TaskAction
    void exec() {
        getProject().exec(execSpec -> {
            execSpec.setExecutable(getExtension().getExecutable());
            execSpec.setArgs(Arrays.asList("--info"));
        });
    }
}

package net.karlmartens.dotnet;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Arrays;

public class DotnetPlugin implements Plugin<Project> {

    private static final String INFO_TASK = "dotnetInfo";
    private static final String CLEAN_TASK = "dotnetClean";
    private static final String RESTORE_TASK = "dotnetRestore";
    private static final String COMPILE_TASK = "dotnetCompile";
    private static final String TEST_TASK = "dotnetTest";
    private static final String DOCS_TASK = "dotnetDocs";

    @Override
    public void apply(Project project) {
        final DotnetExtension extension = project.getExtensions().create("dotnet", DotnetExtension.class);

        project.getTasks().create(INFO_TASK, Info.class, task -> {
            task.setExtension(extension);
        });
        project.getTasks().create(CLEAN_TASK, Clean.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(INFO_TASK));
        });
        project.getTasks().create(RESTORE_TASK, Restore.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(INFO_TASK));
        });
        project.getTasks().create(COMPILE_TASK, Compile.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(CLEAN_TASK, RESTORE_TASK));
        });
        project.getTasks().create(TEST_TASK, Test.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(COMPILE_TASK));
        });
        project.getTasks().create(DOCS_TASK, Docs.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(COMPILE_TASK));
        });
    }
}

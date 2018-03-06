package net.karlmartens.dotnet;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import java.util.Arrays;

public class DotnetPlugin implements Plugin<Project> {

    private static final String INFO_TASK = "dotnetInfo";
    private static final String CLEAN_TASK = "dotnetClean";
    private static final String RESTORE_TASK = "dotnetRestore";
    private static final String COMPILE_TASK = "dotnetCompile";
    private static final String TEST_TASK = "dotnetTest";
    private static final String DOCS_TASK = "dotnetDocs";
    private static final String PUBLISH_TASK = "dotnetPublish";

    @Override
    public void apply(Project project) {
        final DotnetExtension extension = project.getExtensions().create("dotnet", DotnetExtension.class);

        TaskContainer tasks = project.getTasks();
        tasks.create(INFO_TASK, Info.class, task -> {
            task.setExtension(extension);
        });
        tasks.create(CLEAN_TASK, Clean.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(INFO_TASK));
        });
        tasks.create(RESTORE_TASK, Restore.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(INFO_TASK));
        });
        tasks.create(COMPILE_TASK, Compile.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(CLEAN_TASK, RESTORE_TASK));
        });
        tasks.create(TEST_TASK, Test.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(COMPILE_TASK));
        });
        tasks.create(DOCS_TASK, Docs.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(COMPILE_TASK));
        });
        tasks.create(PUBLISH_TASK, Publish.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(TEST_TASK));
        });

        tasks.create("clean", task -> {
           task.setDependsOn(Arrays.asList(CLEAN_TASK));
        });
        tasks.create("build", task -> {
            task.setDependsOn(Arrays.asList(COMPILE_TASK));
        });
        tasks.create("publish", task -> {
            task.setDependsOn(Arrays.asList(PUBLISH_TASK));
        });
    }
}

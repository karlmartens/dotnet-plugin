package net.karlmartens.dotnet;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import java.util.Arrays;

public class DotnetPlugin implements Plugin<Project> {

    private static final String INFO_TASK = "info";
    private static final String CLEAN_TASK = "clean";
    private static final String RESTORE_TASK = "restore";
    private static final String COMPILE_TASK = "compileDotnet";
    private static final String TEST_TASK = "test";
    private static final String DOCS_TASK = "docs";
    private static final String ASSEMBLE_TASK = "assemble";
    private static final String PUBLISH_TASK = "publish";

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
        tasks.create(ASSEMBLE_TASK, Pack.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(TEST_TASK));
        });
        tasks.create(PUBLISH_TASK, Publish.class, task -> {
            task.setExtension(extension);
            task.setDependsOn(Arrays.asList(ASSEMBLE_TASK));
        });

        tasks.create("build", task -> {
            task.setDependsOn(Arrays.asList(COMPILE_TASK));
        });
    }
}

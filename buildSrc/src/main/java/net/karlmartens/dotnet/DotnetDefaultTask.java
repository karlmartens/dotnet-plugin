package net.karlmartens.dotnet;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

import java.io.File;
import java.util.function.Consumer;

class DotnetDefaultTask extends DefaultTask {

    private DotnetExtension _ext = new DotnetExtension();

    @Internal
    public final void setExtension(DotnetExtension ext) {
        _ext = ext;
    }

    @Internal
    public final DotnetExtension getExtension() {
        return _ext;
    }

    protected static void when(boolean b, Runnable r) {
        if (b) {
            r.run();
        }
    }

    protected static void whenHasValue(String value, Consumer<String> consumer) {
        if (value != null && !value.isEmpty()) {
            consumer.accept(value);
        }
    }


    protected final String directoryName(String filePath) {
        String[] baseDir = { "." };
        whenHasValue(filePath, s -> {
            File file = new File(s);
            baseDir[0] = file.getAbsoluteFile().getParent();
        });
        return baseDir[0];
    }


}
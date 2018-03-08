package net.karlmartens.dotnet;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

class DotnetDefaultTask extends DefaultTask {

    private DotnetExtension _ext = new DotnetExtension();
    private List<Publish.AttributeValue> _parameters = new ArrayList<>();

    @Internal
    public final void setExtension(DotnetExtension ext) {
        _ext = ext;
    }

    @Internal
    public final DotnetExtension getExtension() {
        return _ext;
    }

    public void addParameter(String attribute, String value) {
        _parameters.add(new AttributeValue(attribute, value));
    }

    protected final Iterable<AttributeValue> getParameters() {
        return _parameters;
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

    protected final Consumer<String> addNamedParameter(List<String> args, String name) {
        return value -> {
            args.add(name);
            args.add(value);
        };
    }

    protected final Runnable addParameter(List<String> args, String parameter) {
        return () -> args.add(parameter);
    }

    protected final void appendParameters(Collection<String> args) {
        for (AttributeValue av : getParameters()) {
            args.add(String.format("/p:%s=\"\"%s\"\"", av.getAttribute(), av.getValue()));
        }
    }

    protected static final class AttributeValue {
        private String _attribute;
        private String _value;

        private AttributeValue(String attribute, String value) {
            _attribute = Objects.requireNonNull(attribute);
            _value = Objects.requireNonNull(value);
        }

        public String getAttribute() {
            return _attribute;
        }

        public String getValue() {
            return _value;
        }
    }

}

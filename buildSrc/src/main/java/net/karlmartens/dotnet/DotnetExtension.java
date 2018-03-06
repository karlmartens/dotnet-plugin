package net.karlmartens.dotnet;

import org.apache.tools.ant.taskdefs.condition.Os;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DotnetExtension {

    private String _configuration = null;
    private String _dotnetHome = null;
    private String _solution = "";
    private String _projectPattern = "**/*.csproj";
    private String _testPattern = "**/*Test.csproj";
    private String _docsHome = null;
    private String _docsSrc = "./docs/docfx.json";
    private String _framework = null;
    private String _runtime = null;

    public DotnetExtension() {
        _dotnetHome = System.getenv("DOTNET_HOME");
        _docsHome = System.getenv("DOCFX_HOME");
    }


    public String getConfiguration() {
        return _configuration;
    }

    public void setConfiguration(String configuration) {
        _configuration = configuration;
    }

    public String getFramework() {
        return _framework;
    }

    public void setFramework(String framework) {
        _framework = framework;
    }

    public String getRuntime() {
        return _runtime;
    }

    public void setRuntime(String runtime) {
        _runtime = runtime;
    }

    public String getDotnetHome() {
        return _dotnetHome;
    }

    public void setDotnetHome(String path) {
        _dotnetHome = path;
    }

    public String getExecutable() {
        String executable = "dotnet";
        if (_dotnetHome == null)
            return executable;

        Path path = Paths.get(_dotnetHome, executable);
        return path.toString();
    }

    public void setSolution(String solution) { _solution = solution; }

    public String getSolution() { return _solution; }

    public String getProjectPattern() {
        return _projectPattern;
    }

    public void setProjectPatten(String pattern) {
        _projectPattern = pattern;
    }

    public String getTestPattern() {
        return _testPattern;
    }

    public void setTestPattern(String pattern) {
        _testPattern = pattern;
    }

    public String getDocsHome() {
        return _docsHome;
    }

    public void setDocsHome(String path) {
        _docsHome = path;
    }

    public String getDocsExecutable() {
        String executable = "docfx";
        if (Os.isFamily(Os.FAMILY_WINDOWS))
            executable += ".exe";

        if (_docsHome == null)
            return executable;

        Path path = Paths.get(_docsHome, executable);
        return path.toString();
    }

    public String getDocsSrc() {
        return _docsSrc;
    }

    public void setDocsSrc(String src) {
        _docsSrc = src;
    }
}

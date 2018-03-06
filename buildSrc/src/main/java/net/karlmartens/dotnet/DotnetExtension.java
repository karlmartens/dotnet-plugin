package net.karlmartens.dotnet;

import org.apache.tools.ant.taskdefs.condition.Os;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DotnetExtension {

    private String _executable = "dotnet";
    private String _solution = "";
    private String _projectPattern = "**/*.csproj";
    private String _testPattern = "**/*Test.csproj";
    private String _docsHome = null;
    private String _docsSrc = "./docs/docfx.json";


    public void setExecutable(String executable) {
        _executable = executable;
    }

    public String getExecutable() {
        return _executable;
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

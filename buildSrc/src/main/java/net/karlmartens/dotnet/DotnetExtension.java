package net.karlmartens.dotnet;

public class DotnetExtension {

    private String _executable = "dotnet";
    private String _solution = "";
    private String _testPattern = "**/*Test.csproj";
    private String _docsExecutable = "docfx";
    private String _docsSrc = "./docs/docfx.json";

    public void setExecutable(String executable) {
        _executable = executable;
    }

    public String getExecutable() {
        return _executable;
    }

    public void setSolution(String solution) { _solution = solution; }

    public String getSolution() { return _solution; }

    public String getTestPattern() {
        return _testPattern;
    }

    public void setTestPattern(String pattern) {
        _testPattern = pattern;
    }

    public String getDocsExecutable() {
        return _docsExecutable;
    }

    public void setDocsExecutable(String executable) {
        _docsExecutable = executable;
    }

    public String getDocsSrc() {
        return _docsSrc;
    }

    public void setDocsSrc(String src) {
        _docsSrc = src;
    }
}

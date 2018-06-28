# oe-coverage
OpenEdge Coverage - [Sonarqube](https://www.sonarqube.org/) Plug-in

This repository contains a Sonarqube Coverage Plug-in for using on OpenEdge ABL projects.
## Sonarqube Test Coverage
This plug-in uses a generic format for Sonarqube's test coverage and test execution import known as [Generic Test Data](https://docs.sonarqube.org/display/SONAR/Generic+Test+Data). The Sonarqube's coverage format is a simple XML and looks like this:
```
<coverage version="1">
  <file path="file1.p">
    <lineToCover lineNumber="6" covered="true"/>
    <lineToCover lineNumber="7" covered="false"/>
  </file>
  <file path="file2.p">
    <lineToCover lineNumber="3" covered="true"/>
  </file>
</coverage>
```
The root node should be named "coverage" and its version attribute should be set to "1". Insert a "file" element for each file which can be covered by tests. Its "path" attribute can be either absolute or relative to the root of the module.
Inside a "file" element, insert a "lineToCover" for each line which can be covered by unit tests. It can have the following attributes:
* "lineNumber" (mandatory): number of line with [executable statements](https://docs.sonarqube.org/display/DEV/Executable+Lines).
* "covered" (mandatory): boolean value indicating whether tests actually hit that line.
## OpenEdge ABL Profiler
OpenEdge ABL has a built-in capability for providing details about runtime execution. This capability, called [Profiler](https://knowledgebase.progress.com/articles/Article/19495?q=profiler+handle&l=en_US&fs=Search&pn=1), collects execution times on blocks of code so that one can evaluate performance.
Runtime details provided by Profiler includes: i) covered and uncovered line numbers, ii) iterations counter, iii) average time, and iv) cumulative time. Below is a simple example of profiler output.
```
1 06/26/2018 "Profiler" 18:00:00 "" //session information block info
.
1 "file1.p" "" 0                    //program information block info
.
1 14 4 1                            //executable lines block info
.
1 14 1 0.000365 1.876325            //covered lines block info
.
```
### Generating Profiler
Configuration Profiler parameters are held by ABL session. Details of how to configure profiler session parameters can be obtained [here](https://knowledgebase.progress.com/articles/Article/P93997). Below is a simple example of profiler session parameters configuration:
```
PROFILER:ENABLED = TRUE.
PROFILER:DIRECTORY = "<path>".
PROFILER:FILE-NAME = "<path>/<output-file>".
PROFILER:LISTINGS = TRUE.
PROFILER:DESCRIPTION = "PROFILER".
PROFILER:PROFILING = TRUE.
PROFILER:TRACE-FILTER = "".

PROFILER:ENABLED = FALSE.
PROFILER:PROFILING = FALSE.
PROFILER:WRITE-DATA().
```
## Running Plug-in
This oe-coverage plug-in is a CLI application. This application can be used with ant builders or maven tasks.
```
SonarCoverage <profiler> <listing-path> <sonar-output> [sonar-source-path]
```
* "profiler" (mandatory): profiler output path.
* "listing-path" (mandatory): file or directory with [listing files](https://documentation.progress.com/output/ua/OpenEdge_latest/index.html#page/gsabl/generating-a-procedure-listing-file.html). This is a kind of intermediate source code containing preprocessed include lines.
* "sonar-output" (mandatory): Sonar's XML generic test
* "sonar-source-path" (optional): Sonar's project relative path

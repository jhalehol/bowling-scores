# Instructions

This is a single command line application that allows to calculate and report a bowling scoring game, next you will find instructions
to build/execute the command line.

## Build and Package

### Package
From the root path run `mvn clean package` all the required dependencies and build the project it will generate
a `jar` file in the `/target` folder it will execute all Unit Tests and Integration Tests, to avoid run tests for package
run command use the flat `-DskipTests`

## Running the application

* Using jar file

When the program be running the following commands are available

help: Shows the program help to visualize the usage of the command line

score: Command to convert a list of bowling scores into a bowling scoring view it receives
the parameter `--source` with the path of the scores file, some examples of usage:

First run the shell application: `java -jar target/bowling-score.jar`, it will open the command line interface:

![img.png](assets%2Fimg.png)

Running commands
```
score --source "/your-scoring-file"
```

Without parameter
```
score "/your-scoring-file"
```

Running command with a sample file (Some scoring file samples are located in the folder) `src/test/resources/`:
```
score --source src/test/resources/positive/scores.txt
```

Output should be similar to:
![img_1.png](assets%2Fimg_1.png)

* Running using shell script.

Run command `./run-shell.sh`

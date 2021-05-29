# Search Workers

## Prerequisites
* You will need JDK 8 or JDK 11 and JAVA_HOME env variable set.

# How to run
Use the `search-workers` script in the directory root to run the project. As the project was developed on Windows,
you might need to give execution permissions first.

``chmod +x search-workers``

Then just need to run:

``./sarch-workers -h``

To see the help content. The first time it may take a bit while downloads all the 
required files and dependencies needed to build the project.

## Extra arguments

As you might have seen in the help content, there are other arguments that can help you to run this project
with different configurations

### Setting the timeout value
You can set the timeout in seconds by using the `-t` parameter, the default value is  60seconds.
``./search-workers -t 20``

### Forcing timeout failure
To force a timeout failure you can use the `-f` argument as follows.
``./search-workers -f``






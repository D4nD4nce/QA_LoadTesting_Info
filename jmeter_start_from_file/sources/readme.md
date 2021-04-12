# this is java maven project

u need java 1.8, maven and inelliJ IDEA for compile
> don't forget to import all needed maven libs!

### use this to create jar pack
`clean package` - IDEA command line
`mvn clean package` - system console

### use this to start proj (generally for debug)
`clean package exec:java -Dexec.mainClass=main.Main_Parse -Dexec.args="start_params.csv UC01"`
1. arg1 - file with params to create
2. arg2 - key what line to choose from param file

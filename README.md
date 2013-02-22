m3da-server
===========

dead simple M3DA TCP server http://wiki.eclipse.org/Mihini/M3DA_Specification


compiling 
---------

with maven generate a runnable uber jar using the command :

> 
> mvn assembly:assembly -DdescriptorId=jar-with-dependencies
> 

run it using the command 

> java -jar target/m3da-server-1.0-SNAPSHOT-jar-with-dependencies.jar

You can start connecting your M3DA client on the TCP port 44900

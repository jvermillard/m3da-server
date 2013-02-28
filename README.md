M3DA server
===========

A very simple M3DA TCP server. M3DA is a secure and bandwith efficient M2M protocol.

The specification : http://wiki.eclipse.org/Mihini/M3DA_Specification

The client : http://www.eclipse.org/mihini

Compiling 
---------

With maven generate a runnable uber jar using the command :
 
> mvn assembly:assembly -DdescriptorId=jar-with-dependencies
 

start it using the command 

> java -jar target/m3da-server-1.0-SNAPSHOT-jar-with-dependencies.jar

You can start connecting your M3DA client on the TCP port 44900 (IANA official port for M3DA).

REST API
--------

You can see all the received data for a given client using the following URL : http://127.0.0.1:8080/data/{client identifier}
 
The client identifier is the value of "agent.config.agent.deviceId" in your mihini installation.

Examples : 
> GET http://127.0.0.1:8080/data/01121979

RESULT : 
 
```javascript
{
   "@sys.foo.Timestamp":[
      {
         "timestamp":"246977562322",
         "value":[
            1361975530
         ]
      }
   ],
   "@sys.foo.bar":[
      {
         "timestamp":"246977562322",
         "value":[
            123
         ]
      }
   ]
}
```

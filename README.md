# RSocket Client CLI (RSC)

```
usage: rsc Uri [Options]

Non-option arguments:
[String: Uri]        

Option                       Description                         
------                       -----------                         
--channel                    Shortcut of --im REQUEST_CHANNEL    
-d, --data [String]          Data (default: )                    
--dataMimeType [String]      MimeType for data (default:         
                               application/json)                 
--debug                      Enable FrameLogger                  
--delayElements [Long]       Enable delayElements(delay) in milli
                               seconds                           
--fnf                        Shortcut of --im FIRE_AND_FORGET    
--help                       Print help                          
--im, --interactionModel     InteractionModel (default:          
  [InteractionModel]           REQUEST_RESPONSE)                 
--limitRate [Integer]        Enable limitRate(rate)              
--log [String]               Enable log()                        
-m, --metadata [String]      Metadata (default: )                
--metadataMimeType [String]  MimeType for metadata (default:     
                               text/plain)                       
-q, --quiet                  Disable the output on next          
-r, --route [String]         Routing Metadata Extension          
--request                    Shortcut of --im REQUEST_RESPONSE   
--stream                     Shortcut of --im REQUEST_STREAM     
--take [Integer]             Enable take(n)                      
-v, --version                Print version                       
-w, --wiretap                Enable wiretap 
```

## Download

Download an executable jar or native binary (only for osx-x86_64 or linux-x86_64) from [Releases](https://github.com/making/rsc/releases).

## Example usages

```
rsc tcp://localhost:8080 --request --route hello -d Foo --debug
```

```
rsc ws://localhost:8080/rsocket --stream --route hello --debug --take 30
```

```
java -jar rsc.jar wss://rsocket-demo.herokuapp.com:443/ws --stream -d Trump
```
(secure protocols only work with the executable jar for now)

## Log options

### Default

By default, the data of the payload will be output (since 0.2.0).

```
$ rsc tcp://localhost:7000 -r greeting.foo 
{"id":1,"content":"Hello, foo!"}
```

### Enable Reactor's log() operator

`--log` option enables Reactive Stream Level log. `--quiet`/`-q` option disables the default output.

```
$ rsc tcp://localhost:7000 -r greeting.foo --log -q
2019-11-28 13:01:32.981  INFO --- [actor-tcp-nio-1] rsc             : onSubscribe(FluxMap.MapSubscriber)
2019-11-28 13:01:32.983  INFO --- [actor-tcp-nio-1] rsc             : request(unbounded)
2019-11-28 13:01:32.994  INFO --- [actor-tcp-nio-1] rsc             : onNext({"id":2,"content":"Hello, foo!"})
2019-11-28 13:01:32.994  INFO --- [actor-tcp-nio-1] rsc             : onComplete()
```

### Enable FrameLogger

`--debug` option enables RSocket Level log.

```
$ rsc tcp://localhost:7000 -r greeting.foo --debug -q
2019-11-28 13:02:07.139 DEBUG --- [actor-tcp-nio-1] i.r.FrameLogger : sending -> 
Frame => Stream ID: 1 Type: REQUEST_RESPONSE Flags: 0b100000000 Length: 22
Metadata:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 0c 67 72 65 65 74 69 6e 67 2e 66 6f 6f          |.greeting.foo   |
+--------+-------------------------------------------------+----------------+
Data:

2019-11-28 13:02:07.153 DEBUG --- [actor-tcp-nio-1] i.r.FrameLogger : receiving -> 
Frame => Stream ID: 1 Type: NEXT_COMPLETE Flags: 0b1100000 Length: 38
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 7b 22 69 64 22 3a 33 2c 22 63 6f 6e 74 65 6e 74 |{"id":3,"content|
|00000010| 22 3a 22 48 65 6c 6c 6f 2c 20 66 6f 6f 21 22 7d |":"Hello, foo!"}|
+--------+-------------------------------------------------+----------------+
```

### Enable Reactor's wiretap

`--wiretap`/`-w` option enables TCP Level log.


```
$ rsc tcp://localhost:7000 -r greeting.foo --wiretap -q
2019-11-28 13:02:28.347 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801] REGISTERED
2019-11-28 13:02:28.348 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801] CONNECT: localhost/127.0.0.1:7000
2019-11-28 13:02:28.348 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801, L:/127.0.0.1:54245 - R:localhost/127.0.0.1:7000] ACTIVE
2019-11-28 13:02:28.361 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801, L:/127.0.0.1:54245 - R:localhost/127.0.0.1:7000] WRITE: 67B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 40 00 00 00 00 04 00 00 01 00 00 00 00 4e |..@............N|
|00000010| 20 00 01 5f 90 1c 6d 65 73 73 61 67 65 2f 78 2e | .._..message/x.|
|00000020| 72 73 6f 63 6b 65 74 2e 72 6f 75 74 69 6e 67 2e |rsocket.routing.|
|00000030| 76 30 10 61 70 70 6c 69 63 61 74 69 6f 6e 2f 6a |v0.application/j|
|00000040| 73 6f 6e                                        |son             |
+--------+-------------------------------------------------+----------------+
2019-11-28 13:02:28.372 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801, L:/127.0.0.1:54245 - R:localhost/127.0.0.1:7000] FLUSH
2019-11-28 13:02:28.373 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801, L:/127.0.0.1:54245 - R:localhost/127.0.0.1:7000] WRITE: 25B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 16 00 00 00 01 11 00 00 00 0d 0c 67 72 65 |.............gre|
|00000010| 65 74 69 6e 67 2e 66 6f 6f                      |eting.foo       |
+--------+-------------------------------------------------+----------------+
2019-11-28 13:02:28.374 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801, L:/127.0.0.1:54245 - R:localhost/127.0.0.1:7000] FLUSH
2019-11-28 13:02:28.387 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801, L:/127.0.0.1:54245 - R:localhost/127.0.0.1:7000] READ: 41B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 26 00 00 00 01 28 60 7b 22 69 64 22 3a 34 |..&....(`{"id":4|
|00000010| 2c 22 63 6f 6e 74 65 6e 74 22 3a 22 48 65 6c 6c |,"content":"Hell|
|00000020| 6f 2c 20 66 6f 6f 21 22 7d                      |o, foo!"}       |
+--------+-------------------------------------------------+----------------+
2019-11-28 13:02:28.387 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xa0202801, L:/127.0.0.1:54245 - R:localhost/127.0.0.1:7000] READ COMPLETE
```

## Known issues

* Composite Metadata is not supported yet.
* Request Channel is not implemented yet
* Secure protocols (`wss`, `tcp+tls`) don't work with native binaries (the executable jar will work)
* Client side responder will not be supported.

## Build

```
./mvnw clean package -Pgraal -DskipTests
```

A native binary will be created in `target/classes/rsc-(osx|linux)-x86_64` depending on your OS.

For linux binary, you can use Docker:

```
docker run --rm \
   -v "$PWD":/usr/src \
   -v "$HOME/.m2":/root/.m2 \
   -w /usr/src \
   oracle/graalvm-ce:19.2.1 \
   bash -c 'gu install native-image && ./mvnw package -Pgraal -DskipTests'
```

```
docker run --rm \
   -v "$PWD":/usr/src \
   -w /usr/src \
   oracle/graalvm-ce:19.2.1 \
   ./target/classes/rsc-linux-x86_64 --version
```

## License
Licensed under the Apache License, Version 2.0.
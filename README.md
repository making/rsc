# RSocket Client CLI (RSC)

> Aiming to be a curl for RSocket

```
usage: rsc Uri [Options]

Non-option arguments:
[String: Uri]        

Option                                Description                           
------                                -----------                           
--ab, --authBearer [String]           Enable Authentication Metadata        
                                        Extension (Bearer).                 
--authBasic [String]                  [DEPRECATED] Enable Authentication    
                                        Metadata Extension (Basic). This    
                                        Metadata exist only for the backward
                                        compatibility with Spring Security  
                                        5.2                                 
--channel                             Shortcut of --im REQUEST_CHANNEL      
-d, --data [String]                   Data. Use '-' to read data from       
                                        standard input. (default: )         
--dataMimeType, --dmt [String]        MimeType for data (default:           
                                        application/json)                   
--debug                               Enable FrameLogger                    
--delayElements [Long]                Enable delayElements(delay) in milli  
                                        seconds                             
--fnf                                 Shortcut of --im FIRE_AND_FORGET      
--help                                Print help                            
--im, --interactionModel              InteractionModel (default:            
  [InteractionModel]                    REQUEST_RESPONSE)                   
--limitRate [Integer]                 Enable limitRate(rate)                
--log [String]                        Enable log()                          
-m, --metadata [String]               Metadata (default: )                  
--metadataMimeType, --mmt [String]    MimeType for metadata (default:       
                                        application/json)                   
--printB3                             Print B3 propagation info. Ignored    
                                        unless --trace is set.              
-q, --quiet                           Disable the output on next            
-r, --route [String]                  Enable Routing Metadata Extension     
--request                             Shortcut of --im REQUEST_RESPONSE     
--resume [Integer]                    Enable resume. Resume session duration
                                        can be configured in seconds.       
--retry [Integer]                     Enable retry. Retry every 1 second    
                                        with the given max attempts.        
-s, --setup [String]                  [DEPRECATED] Data for Setup payload.  
                                        Use --setupData or --sd instead.    
--sd, --setupData [String]            Data for Setup payload                
--setupMetadata, --sm [String]        Metadata for Setup payload            
--setupMetadataMimeType, --smmt       Metadata MimeType for Setup payload   
  [String]                              (default: application/json)         
--show-system-properties              [DEPRECATED] Show SystemProperties for
                                        troubleshoot. Use --                
                                        showSystemProperties instead.       
--showSystemProperties                Show SystemProperties for troubleshoot
--stacktrace                          Show Stacktrace when an exception     
                                        happens                             
--stream                              Shortcut of --im REQUEST_STREAM       
--take [Integer]                      Enable take(n)                        
--trace [TracingMetadataCodec$Flags]  Enable Tracing (Zipkin) Metadata      
                                        Extension. Unless sampling state    
                                        (UNDECIDED, NOT_SAMPLE, SAMPLE,     
                                        DEBUG) is specified, DEBUG is used  
                                        by default.                         
-u, --as, --authSimple [String]       Enable Authentication Metadata        
                                        Extension (Simple). The format must 
                                        be 'username:password'.             
-v, --version                         Print version                         
-w, --wiretap                         Enable wiretap                        
--wsHeader, --wsh [String]            Header for web socket connection      
--zipkinUrl [String]                  Zipkin URL to send a span (ex. http:  
                                        //localhost:9411). Ignored unless --
                                        trace is set.     
```

## Install

Download an executable jar or native binary (only for x86_64-apple-darwin or x86_64-pc-linux) from [Releases](https://github.com/making/rsc/releases).

### Install via Homebrew

You can install native binary via Homebrew.

```
brew install making/tap/rsc
```

### Install via Coursier

If you do not already have [couriser](https://get-coursier.io) installed on your machine, install it following steps given here: https://get-coursier.io/docs/cli-installation. 

To install the graalvm binary do: 

```
cs install rsc --contrib
``` 

To install the jvm binary (executable jar) do:

```
cs install rscj --contrib
```

## Example usages

```
rsc tcp://localhost:8080 --request --route hello -d Foo --debug
```

```
rsc ws://localhost:8080/rsocket --stream --route hello --debug --take 30
```

```
rsc wss://rsocket-demo.herokuapp.com/rsocket --stream --route searchTweets -d Trump
```

> To get `wss` work, environment variable `JAVA_HOME` must be set. (since 0.4.0)<br>
> If `JAVA_HOME` is set, system property `-Djava.library.path=${JAVA_HOME}/jre/lib/<platform>` is automatically added.

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

## Composite Metadata

`rsc` supports [Composite Metadata Extension](https://github.com/rsocket/rsocket/blob/master/Extensions/CompositeMetadata.md) sice 0.3.0.

If multiple metadataMimeTypes are specified, they are automatically composed (the order matters).

```
$ rsc tcp://localhost:7000 --metadataMimeType text/plain -m hello --metadataMimeType text/json -m '{"hello":"world"}' --wiretap
2019-11-29 13:39:54.696 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76] REGISTERED
2019-11-29 13:39:54.696 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76] CONNECT: localhost/127.0.0.1:7000
2019-11-29 13:39:54.697 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62738 - R:localhost/127.0.0.1:7000] ACTIVE
2019-11-29 13:39:54.706 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62738 - R:localhost/127.0.0.1:7000] WRITE: 78B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 4b 00 00 00 00 04 00 00 01 00 00 00 00 4e |..K............N|
|00000010| 20 00 01 5f 90 27 6d 65 73 73 61 67 65 2f 78 2e | .._.'message/x.|
|00000020| 72 73 6f 63 6b 65 74 2e 63 6f 6d 70 6f 73 69 74 |rsocket.composit|
|00000030| 65 2d 6d 65 74 61 64 61 74 61 2e 76 30 10 61 70 |e-metadata.v0.ap|
|00000040| 70 6c 69 63 61 74 69 6f 6e 2f 6a 73 6f 6e       |plication/json  |
+--------+-------------------------------------------------+----------------+
2019-11-29 13:39:54.715 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62738 - R:localhost/127.0.0.1:7000] FLUSH
2019-11-29 13:39:54.715 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62738 - R:localhost/127.0.0.1:7000] WRITE: 51B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 30 00 00 00 01 11 00 00 00 27 a1 00 00 05 |..0........'....|
|00000010| 68 65 6c 6c 6f 08 74 65 78 74 2f 6a 73 6f 6e 00 |hello.text/json.|
|00000020| 00 11 7b 22 68 65 6c 6c 6f 22 3a 22 77 6f 72 6c |..{"hello":"worl|
|00000030| 64 22 7d                                        |d"}             |
+--------+-------------------------------------------------+----------------+
2019-11-29 13:39:54.716 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62738 - R:localhost/127.0.0.1:7000] FLUSH
...
```

`--route` option is still respected.

```
$ rsc tcp://localhost:7000 --metadataMimeType text/plain -m hello --route greeting.foo --wiretap
2019-11-29 13:41:54.916 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76] REGISTERED
2019-11-29 13:41:54.917 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76] CONNECT: localhost/127.0.0.1:7000
2019-11-29 13:41:54.917 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62751 - R:localhost/127.0.0.1:7000] ACTIVE
2019-11-29 13:41:54.928 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62751 - R:localhost/127.0.0.1:7000] WRITE: 78B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 4b 00 00 00 00 04 00 00 01 00 00 00 00 4e |..K............N|
|00000010| 20 00 01 5f 90 27 6d 65 73 73 61 67 65 2f 78 2e | .._.'message/x.|
|00000020| 72 73 6f 63 6b 65 74 2e 63 6f 6d 70 6f 73 69 74 |rsocket.composit|
|00000030| 65 2d 6d 65 74 61 64 61 74 61 2e 76 30 10 61 70 |e-metadata.v0.ap|
|00000040| 70 6c 69 63 61 74 69 6f 6e 2f 6a 73 6f 6e       |plication/json  |
+--------+-------------------------------------------------+----------------+
2019-11-29 13:41:54.938 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62751 - R:localhost/127.0.0.1:7000] FLUSH
2019-11-29 13:41:54.938 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62751 - R:localhost/127.0.0.1:7000] WRITE: 38B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 23 00 00 00 01 11 00 00 00 1a fe 00 00 0d |..#.............|
|00000010| 0c 67 72 65 65 74 69 6e 67 2e 66 6f 6f a1 00 00 |.greeting.foo...|
|00000020| 05 68 65 6c 6c 6f                               |.hello          |
+--------+-------------------------------------------------+----------------+
2019-11-29 13:41:54.938 DEBUG --- [actor-tcp-nio-1] r.n.t.TcpClient : [id: 0xfd779f76, L:/127.0.0.1:62751 - R:localhost/127.0.0.1:7000] FLUSH
...
```

If you use `--route/-r` option, you need to specify to `--metadataMimeType/--mmt` option for the additional metadata even if the type is `application/json` which is the default mime type.

For example:

```
rsc tcp://localhost:8080 -r functionRouter --mmt application/json -m '{"function":"uppercase"}' -d 'RSocket'
``` 

## Backpressure

The `onNext` output can be delayed with the `--delayElements` (milli seconds) option.　Accordingly, the number of `request` will be automatically adjusted.

```
$ rsc tcp://localhost:8765 --stream --delayElements 100 --log
2019-11-28 14:17:25.479  INFO --- [actor-tcp-nio-1] rsc             : onSubscribe(FluxMap.MapSubscriber)
2019-11-28 14:17:25.479  INFO --- [actor-tcp-nio-1] rsc             : request(32)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(A)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(a)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(aa)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(aal)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(aalii)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(aam)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aani)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(aardvark)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(aardwolf)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaron)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronic)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronical)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronite)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronitic)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaru)
2019-11-28 14:17:25.483  INFO --- [actor-tcp-nio-1] rsc             : onNext(Ab)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(aba)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(Ababdeh)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(Ababua)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abac)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abaca)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abacate)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abacay)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abacinate)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abacination)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abaciscus)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abacist)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(aback)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abactinal)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abactinally)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abaction)
2019-11-28 14:17:25.484  INFO --- [actor-tcp-nio-1] rsc             : onNext(abactor)
A
a
aa
aal
aalii
aam
Aani
aardvark
aardwolf
Aaron
Aaronic
Aaronical
Aaronite
Aaronitic
Aaru
Ab
aba
Ababdeh
Ababua
abac
abaca
abacate
abacay
2019-11-28 14:17:27.841  INFO --- [     parallel-8] rsc             : request(24)
2019-11-28 14:17:27.856  INFO --- [actor-tcp-nio-1] rsc             : onNext(abaculus)
...
```

You can also limit the number of `request` with `--limitRate` option.

```
$ rsc tcp://localhost:8765 --stream --delayElements 100 --limitRate 8 --log
2019-11-28 14:21:35.175  INFO --- [actor-tcp-nio-1] rsc             : onSubscribe(FluxMap.MapSubscriber)
2019-11-28 14:21:35.175  INFO --- [actor-tcp-nio-1] rsc             : request(8)
2019-11-28 14:21:35.187  INFO --- [actor-tcp-nio-1] rsc             : onNext(A)
2019-11-28 14:21:35.188  INFO --- [actor-tcp-nio-1] rsc             : onNext(a)
2019-11-28 14:21:35.188  INFO --- [actor-tcp-nio-1] rsc             : onNext(aa)
2019-11-28 14:21:35.188  INFO --- [actor-tcp-nio-1] rsc             : onNext(aal)
2019-11-28 14:21:35.188  INFO --- [actor-tcp-nio-1] rsc             : onNext(aalii)
2019-11-28 14:21:35.188  INFO --- [actor-tcp-nio-1] rsc             : onNext(aam)
2019-11-28 14:21:35.188  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aani)
2019-11-28 14:21:35.188  INFO --- [actor-tcp-nio-1] rsc             : onNext(aardvark)
A
a
aa
aal
aalii
2019-11-28 14:21:35.702  INFO --- [     parallel-6] rsc             : request(6)
2019-11-28 14:21:35.714  INFO --- [actor-tcp-nio-1] rsc             : onNext(aardwolf)
2019-11-28 14:21:35.714  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaron)
2019-11-28 14:21:35.714  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronic)
2019-11-28 14:21:35.714  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronical)
2019-11-28 14:21:35.714  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronite)
2019-11-28 14:21:35.714  INFO --- [actor-tcp-nio-1] rsc             : onNext(Aaronitic)
aam
Aani
aardvark
aardwolf
Aaron
Aaronic
2019-11-28 14:21:36.326  INFO --- [     parallel-4] rsc             : request(6)
...
```

**Tip**: Using `--limitRate 1 --delayElements 1000 --debug` is a convenient way to trace a stream.

> The sample server application can be started with the following command:
> ```
> rsocket-cli --debug -i=@/usr/share/dict/words --server tcp://localhost:8765
> ```

## TODOs

- [x] Support resuming (0.3.0)
- [x] Support Composite Metadata (0.3.0)
- [x] Setup data (0.4.0)
- [x] Setup Metadata (0.6.0)
- [ ] RSocket Security
- [ ] RSocket Routing
- [x] Request Channel (0.4.0)
- [ ] Input from a file
- [x] Input from STDIN (0.4.0)

## Known issues

* ~Secure protocols (`wss`, `tcp+tls`) don't work with native binaries (the executable jar will work)~ (Supported in 0.4.0)
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

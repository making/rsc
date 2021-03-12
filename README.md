# RSocket Client CLI (RSC)
[![CI](https://github.com/making/rsc/workflows/CI/badge.svg)](https://github.com/making/rsc/actions?query=workflow%3ACI)

> Aiming to be a curl for RSocket

```
usage: rsc [options] uri

Non-option arguments:
[String: uri]        

Option                                Description                            
------                                -----------                            
--ab, --authBearer [String]           Enable Authentication Metadata         
                                        Extension (Bearer).                  
--authBasic [String]                  [DEPRECATED] Enable Authentication     
                                        Metadata Extension (Basic). This     
                                        Metadata exists only for the         
                                        backward compatibility with Spring   
                                        Security 5.2                         
--channel                             Shortcut of --im REQUEST_CHANNEL       
--completion [ShellType]              Output shell completion code for the   
                                        specified shell (bash, zsh, fish,    
                                        powershell)                          
-d, --data [String]                   Data. Use '-' to read data from        
                                        standard input. (default: )          
--dataMimeType, --dmt [String]        MimeType for data (default:            
                                        application/json)                    
--debug                               Enable FrameLogger                     
--delayElements [Long]                Enable delayElements(delay) in milli   
                                        seconds                              
--dumpOpts                            Dump options as a file that can be     
                                        loaded by --optsFile option          
--fnf                                 Shortcut of --im FIRE_AND_FORGET       
-h, --help [String]                   Print help                             
--im, --interactionModel              InteractionModel (default:             
  [InteractionModel]                    REQUEST_RESPONSE)                    
-l, --load [String]                   Load a file as Data. (e.g. ./foo.txt,  
                                        /tmp/foo.txt, https://example.com)   
--limitRate [Integer]                 Enable limitRate(rate)                 
--log [String]                        Enable log()                           
-m, --metadata [String]               Metadata (default: )                   
--metadataMimeType, --mmt [String]    MimeType for metadata (default:        
                                        application/json)                    
--optsFile [String]                   Configure options from a YAML file (e. 
                                        g. ./opts.yaml, /tmp/opts.yaml,      
                                        https://example.com/opts.yaml)       
--printB3                             Print B3 propagation info. Ignored     
                                        unless --trace is set.               
-q, --quiet                           Disable the output on next             
-r, --route [String]                  Enable Routing Metadata Extension      
--request                             Shortcut of --im REQUEST_RESPONSE      
--resume [Integer]                    Enable resume. Resume session duration 
                                        can be configured in seconds.        
--retry [Integer]                     Enable retry. Retry every 1 second     
                                        with the given max attempts.         
--sd, --setupData [String]            Data for Setup payload                 
--setupMetadata, --sm [String]        Metadata for Setup payload             
--setupMetadataMimeType, --smmt       Metadata MimeType for Setup payload    
  [String]                              (default: application/json)          
--showSystemProperties                Show SystemProperties for troubleshoot 
--stacktrace                          Show Stacktrace when an exception      
                                        happens                              
--stream                              Shortcut of --im REQUEST_STREAM        
--take [Integer]                      Enable take(n)                         
--trace [TracingMetadataCodec$Flags]  Enable Tracing (Zipkin) Metadata       
                                        Extension. Unless sampling state     
                                        (UNDECIDED, NOT_SAMPLE, SAMPLE,      
                                        DEBUG) is specified, DEBUG is used   
                                        if no state is specified.            
--trustCert [String]                  PEM file for a trusted certificate. (e.
                                        g. ./foo.crt, /tmp/foo.crt, https:   
                                        //example.com/foo.crt)               
-u, --as, --authSimple [String]       Enable Authentication Metadata         
                                        Extension (Simple). The format must  
                                        be 'username:password'.              
-v, --version                         Print version                          
-w, --wiretap                         Enable wiretap                         
--wsHeader, --wsh [String]            Header for web socket connection       
--zipkinUrl [String]                  Zipkin URL to send a span (e.g. http:  
                                        //localhost:9411). Ignored unless -- 
                                        trace is set.                                                                   
```

## Install

Download an executable jar or native binary from [Releases](https://github.com/making/rsc/releases).

To get `rsc` binary working on Windows, you will need to install [Visual C++ Redistributable Packages](https://www.microsoft.com/en-us/download/details.aspx?id=48145) in advance.

### Install via Homebrew (Mac / Linux)
[![Homebrew](https://github.com/making/rsc/workflows/Homebrew/badge.svg)](https://github.com/making/rsc/actions?query=workflow%3AHomebrew)

You can install native binary for Mac or Linux via [Homebrew](https://brew.sh/).

```
brew install making/tap/rsc
```

### Install via Scoop (Windows)
[![Scoop](https://github.com/making/rsc/workflows/Scoop/badge.svg)](https://github.com/making/rsc/actions?query=workflow%3AScoop)

You can install native binary for Windows via [Scoop](https://scoop.sh/).

```
scoop bucket add making https://github.com/making/scoop-bucket.git
scoop update
scoop install rsc
```

### Install via Coursier (Mac / Linux / Windows)
[![Coursier](https://github.com/making/rsc/workflows/Coursier/badge.svg)](https://github.com/making/rsc/actions?query=workflow%3ACoursier)

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
rsc --request --route=uppercase --data=Foo --debug tcp://localhost:7001
```

```
rsc --stream --route=hello --debug --take=30 ws://localhost:8080/rsocket
```

```
rsc --stream --route=searchTweets --data=Trump wss://rsocket-demo-1.appspot.com/rsocket
```

You can also send data via a file or URL using `-l`/`--load` option instead of `-d`/`--data` as follows

```
rsc --request --route=hello --load=./hello.txt --debug tcp://localhost:8080
rsc --request --route=hello --load=/tmp/hello.txt --debug tcp://localhost:8080
rsc --request --route=hello --load=https://example.com --debug tcp://localhost:8080
```

## Enable shell autocompletion 

rsc (0.8.0+) provides autocompletion support for Bash, Zsh, Fish and Powershell.

```
rsc --completion <SHELL>
```

shows the completion script.

![rsc-completion](https://user-images.githubusercontent.com/106908/106292859-af40bc00-6290-11eb-9f76-99b0d5e2914a.gif)

If you install `rsc` via Homebrew, the completion script is also installed under `/usr/local/Homebrew/completions/`.

Below are the procedures to set up autocompletion manually.

### Zsh

Add the following to the beginning of your `~/.zshrc`

```
autoload -Uz compinit && compinit
```

You now need to ensure that the rsc completion script gets sourced in all your shell sessions. 

```
echo 'source <(rsc --completion bash)' >>~/.zshrc
```

### Bash

the completion script depends on [bash-completion](https://github.com/scop/bash-completion).

#### on Mac

the completion script doesn't work with Bash 3.2 which is the default bash version on Mac.
It requires Bash 4.1+ and bash-completion v2. 

You can install these as follows

```
brew install bash
brew install bash-completion@2
```

Make sure `bash -v` shows 4.1+.

Add the bellow to your `~/.bash_profile`

```
[[ -r "/usr/local/etc/profile.d/bash_completion.sh" ]] && . "/usr/local/etc/profile.d/bash_completion.sh"
```

You now need to ensure that the rsc completion script gets sourced in all your shell sessions. 

```
echo 'source <(rsc --completion bash)' >>~/.bash_profile
```

#### on Linux
You can install bash-completion with `apt-get install bash-completion` or `yum install bash-completion`, etc.

Add `source /usr/share/bash-completion/bash_completion` to your `~/.bashrc`.

You now need to ensure that the rsc completion script gets sourced in all your shell sessions. 

```
echo 'source <(rsc --completion bash)' >>~/.bashrc
```

### Fish

TBD (help wanted)

```
rsc --completion fish
```

### Powershell

```
rsc --completion powershell | Out-String | Invoke-Expression
```

## Log options

### Default

By default, the data of the payload will be output (since 0.2.0).

```
$ rsc --route=add --data='{"x":10, "y":20}' tcp://localhost:7001
{"result":30}
```

### Enable Reactor's log() operator

`--log` option enables Reactive Stream Level log. `--quiet`/`-q` option disables the default output.

```
$ rsc --route=add --data='{"x":10, "y":20}' --log --quiet tcp://localhost:7001 
2021-02-06 17:50:15.809  INFO 95810 --- [actor-tcp-nio-2] rsc                                      : onSubscribe(FluxMap.MapSubscriber)
2021-02-06 17:50:15.809  INFO 95810 --- [actor-tcp-nio-2] rsc                                      : request(unbounded)
2021-02-06 17:50:15.820  INFO 95810 --- [actor-tcp-nio-2] rsc                                      : onNext({"result":30})
2021-02-06 17:50:15.820  INFO 95810 --- [actor-tcp-nio-2] rsc                                      : onComplete()
```

### Enable FrameLogger

`--debug` option enables RSocket Level log.

```
$ rsc --route=add --data='{"x":10, "y":20}' --debug --quiet tcp://localhost:7001
2021-02-06 17:50:32.560 DEBUG 95820 --- [actor-tcp-nio-2] io.rsocket.FrameLogger                   : sending -> 
Frame => Stream ID: 0 Type: SETUP Flags: 0b0 Length: 75
Data:

2021-02-06 17:50:32.560 DEBUG 95820 --- [actor-tcp-nio-2] io.rsocket.FrameLogger                   : sending -> 
Frame => Stream ID: 1 Type: REQUEST_RESPONSE Flags: 0b100000000 Length: 33
Metadata:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| fe 00 00 04 03 61 64 64                         |.....add        |
+--------+-------------------------------------------------+----------------+
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 7b 22 78 22 3a 31 30 2c 20 22 79 22 3a 32 30 7d |{"x":10, "y":20}|
+--------+-------------------------------------------------+----------------+
2021-02-06 17:50:32.571 DEBUG 95820 --- [actor-tcp-nio-2] io.rsocket.FrameLogger                   : receiving -> 
Frame => Stream ID: 1 Type: NEXT_COMPLETE Flags: 0b1100000 Length: 19
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 7b 22 72 65 73 75 6c 74 22 3a 33 30 7d          |{"result":30}   |
+--------+-------------------------------------------------+----------------+
```

### Enable Reactor's wiretap

`--wiretap`/`-w` option enables TCP Level log.


```
$ rsc --route=add --data='{"x":10, "y":20}' --wiretap --quiet tcp://localhost:7001
2021-02-06 17:51:20.140 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe] REGISTERED
2021-02-06 17:51:20.141 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe] CONNECT: localhost/127.0.0.1:7001
2021-02-06 17:51:20.141 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe, L:/127.0.0.1:62801 - R:localhost/127.0.0.1:7001] ACTIVE
2021-02-06 17:51:20.141 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe, L:/127.0.0.1:62801 - R:localhost/127.0.0.1:7001] WRITE: 78B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 4b 00 00 00 00 04 00 00 01 00 00 00 00 4e |..K............N|
|00000010| 20 00 01 5f 90 27 6d 65 73 73 61 67 65 2f 78 2e | .._.'message/x.|
|00000020| 72 73 6f 63 6b 65 74 2e 63 6f 6d 70 6f 73 69 74 |rsocket.composit|
|00000030| 65 2d 6d 65 74 61 64 61 74 61 2e 76 30 10 61 70 |e-metadata.v0.ap|
|00000040| 70 6c 69 63 61 74 69 6f 6e 2f 6a 73 6f 6e       |plication/json  |
+--------+-------------------------------------------------+----------------+
2021-02-06 17:51:20.142 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe, L:/127.0.0.1:62801 - R:localhost/127.0.0.1:7001] WRITE: 36B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 21 00 00 00 01 11 00 00 00 08 fe 00 00 04 |..!.............|
|00000010| 03 61 64 64 7b 22 78 22 3a 31 30 2c 20 22 79 22 |.add{"x":10, "y"|
|00000020| 3a 32 30 7d                                     |:20}            |
+--------+-------------------------------------------------+----------------+
2021-02-06 17:51:20.142 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe, L:/127.0.0.1:62801 - R:localhost/127.0.0.1:7001] FLUSH
2021-02-06 17:51:20.152 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe, L:/127.0.0.1:62801 - R:localhost/127.0.0.1:7001] READ: 22B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 13 00 00 00 01 28 60 7b 22 72 65 73 75 6c |.......(`{"resul|
|00000010| 74 22 3a 33 30 7d                               |t":30}          |
+--------+-------------------------------------------------+----------------+
2021-02-06 17:51:20.152 DEBUG 95837 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x39d9fefe, L:/127.0.0.1:62801 - R:localhost/127.0.0.1:7001] READ COMPLETE
```

## Setup payload

The data in `SETUP` payload can be specified by `--setupData`/`--sd` option and metadata can be specified by `--setupMetaData`/`--smd`.
Also the MIME type of the setup metadata can be specified by `--setupMetadataMimeType`/`--smmt` option.

For example:

```
rsc --setupData=foo --setupMetadata='{"value":"metadata"}' --setupMetadataMimeType=application/json --route=add --data='{"x":10, "y":20}' tcp://localhost:7001
```

As of 0.6.0, the following MIME types are supported.

* `application/json` (default)
* `text/plain`
* `message/x.rsocket.authentication.v0`
* `message/x.rsocket.authentication.basic.v0`
* `message/x.rsocket.application+json` (0.7.1+)

Accordingly, enum name of [`SetupMetadataMimeType`](https://github.com/making/rsc/blob/master/src/main/java/am/ik/rsocket/SetupMetadataMimeType.java) instead can be used with `--smmt` option

* `APPLICATION_JSON`
* `TEXT_PLAIN`
* `MESSAGE_RSOCKET_AUTHENTICATION`
* `AUTHENTICATION_BASIC`
* `APP_INFO` (0.7.1+)

## Composite Metadata

`rsc` always uses [Composite Metadata Extension](https://github.com/rsocket/rsocket/blob/master/Extensions/CompositeMetadata.md).
If multiple metadataMimeTypes are specified, they are automatically composed (the order matters).

```
$ rsc --metadataMimeType=text/plain --metadata=hello --metadataMimeType=application/json --metadata='{"hello":"world"}' --data='{"x":10, "y":20}' --wiretap --quiet tcp://localhost:7001
2021-02-06 18:00:29.100 DEBUG 95998 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x9c1425b3] REGISTERED
2021-02-06 18:00:29.101 DEBUG 95998 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x9c1425b3] CONNECT: localhost/127.0.0.1:7001
2021-02-06 18:00:29.101 DEBUG 95998 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x9c1425b3, L:/127.0.0.1:62844 - R:localhost/127.0.0.1:7001] ACTIVE
2021-02-06 18:00:29.102 DEBUG 95998 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x9c1425b3, L:/127.0.0.1:62844 - R:localhost/127.0.0.1:7001] WRITE: 78B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 4b 00 00 00 00 04 00 00 01 00 00 00 00 4e |..K............N|
|00000010| 20 00 01 5f 90 27 6d 65 73 73 61 67 65 2f 78 2e | .._.'message/x.|
|00000020| 72 73 6f 63 6b 65 74 2e 63 6f 6d 70 6f 73 69 74 |rsocket.composit|
|00000030| 65 2d 6d 65 74 61 64 61 74 61 2e 76 30 10 61 70 |e-metadata.v0.ap|
|00000040| 70 6c 69 63 61 74 69 6f 6e 2f 6a 73 6f 6e       |plication/json  |
+--------+-------------------------------------------------+----------------+
2021-02-06 18:00:29.102 DEBUG 95998 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x9c1425b3, L:/127.0.0.1:62844 - R:localhost/127.0.0.1:7001] WRITE: 58B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 37 00 00 00 01 11 00 00 00 1e a1 00 00 05 |..7.............|
|00000010| 68 65 6c 6c 6f 85 00 00 11 7b 22 68 65 6c 6c 6f |hello....{"hello|
|00000020| 22 3a 22 77 6f 72 6c 64 22 7d 7b 22 78 22 3a 31 |":"world"}{"x":1|
|00000030| 30 2c 20 22 79 22 3a 32 30 7d                   |0, "y":20}      |
+--------+-------------------------------------------------+----------------+
2021-02-06 18:00:29.102 DEBUG 95998 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0x9c1425b3, L:/127.0.0.1:62844 - R:localhost/127.0.0.1:7001] FLUSH
...
```

`--route` option is still respected.

```
$ rsc --metadataMimeType=text/plain --metadata=hello --metadataMimeType=application/json --metadata='{"hello":"world"}' --route=add --data='{"x":10, "y":20}' --wiretap --quiet tcp://localhost:7001
2021-02-06 18:01:28.434 DEBUG 96015 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0xf49f4cd5] REGISTERED
2021-02-06 18:01:28.435 DEBUG 96015 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0xf49f4cd5] CONNECT: localhost/127.0.0.1:7001
2021-02-06 18:01:28.435 DEBUG 96015 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0xf49f4cd5, L:/127.0.0.1:62848 - R:localhost/127.0.0.1:7001] ACTIVE
2021-02-06 18:01:28.436 DEBUG 96015 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0xf49f4cd5, L:/127.0.0.1:62848 - R:localhost/127.0.0.1:7001] WRITE: 78B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 4b 00 00 00 00 04 00 00 01 00 00 00 00 4e |..K............N|
|00000010| 20 00 01 5f 90 27 6d 65 73 73 61 67 65 2f 78 2e | .._.'message/x.|
|00000020| 72 73 6f 63 6b 65 74 2e 63 6f 6d 70 6f 73 69 74 |rsocket.composit|
|00000030| 65 2d 6d 65 74 61 64 61 74 61 2e 76 30 10 61 70 |e-metadata.v0.ap|
|00000040| 70 6c 69 63 61 74 69 6f 6e 2f 6a 73 6f 6e       |plication/json  |
+--------+-------------------------------------------------+----------------+
2021-02-06 18:01:28.436 DEBUG 96015 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0xf49f4cd5, L:/127.0.0.1:62848 - R:localhost/127.0.0.1:7001] WRITE: 66B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 00 00 3f 00 00 00 01 11 00 00 00 26 fe 00 00 04 |..?........&....|
|00000010| 03 61 64 64 a1 00 00 05 68 65 6c 6c 6f 85 00 00 |.add....hello...|
|00000020| 11 7b 22 68 65 6c 6c 6f 22 3a 22 77 6f 72 6c 64 |.{"hello":"world|
|00000030| 22 7d 7b 22 78 22 3a 31 30 2c 20 22 79 22 3a 32 |"}{"x":10, "y":2|
|00000040| 30 7d                                           |0}              |
+--------+-------------------------------------------------+----------------+
2021-02-06 18:01:28.437 DEBUG 96015 --- [actor-tcp-nio-2] reactor.netty.tcp.TcpClient              : [id: 0xf49f4cd5, L:/127.0.0.1:62848 - R:localhost/127.0.0.1:7001] FLUSH
...
```

If you use `--route/-r` option, you need to specify to `--metadataMimeType/--mmt` option for the additional metadata even if the type is `application/json` which is the default mime type.

For example:

```
rsc -r functionRouter --mmt application/json -m '{"function":"uppercase"}' -d 'RSocket' tcp://localhost:8080
``` 

## Backpressure

The `onNext` output can be delayed with the `--delayElements` (milli seconds) option.　Accordingly, the number of `request` will be automatically adjusted.

```
$ rsc --stream --delayElements=100 --log --route=uppercase.stream --data=rsocket tcp://localhost:7001     
2021-02-06 18:14:18.230  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onSubscribe(FluxMap.MapSubscriber)
2021-02-06 18:14:18.230  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : request(32)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.235  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:18.236  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
2021-02-06 18:14:20.595  INFO 96438 --- [     parallel-8] rsc                                      : request(24)
2021-02-06 18:14:20.598  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:14:20.598  INFO 96438 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
...
```

You can also limit the number of `request` with `--limitRate` option.

```
$ rsc --stream --delayElements=100 --limitRate=8 --log --route=uppercase.stream --data=rsocket tcp://localhost:7001
2021-02-06 18:06:04.919  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onSubscribe(FluxMap.MapSubscriber)
2021-02-06 18:06:04.919  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : request(8)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:04.922  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
2021-02-06 18:06:05.435  INFO 96118 --- [     parallel-6] rsc                                      : request(6)
2021-02-06 18:06:05.439  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:05.439  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:05.439  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:05.439  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:05.439  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:05.439  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
2021-02-06 18:06:06.048  INFO 96118 --- [    parallel-12] rsc                                      : request(6)
2021-02-06 18:06:06.050  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:06.050  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:06.050  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:06.050  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:06.050  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
2021-02-06 18:06:06.050  INFO 96118 --- [actor-tcp-nio-2] rsc                                      : onNext(RSOCKET)
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
RSOCKET
...
```

**Tip**: Using `--limitRate 1 --delayElements 1000 --debug` is a convenient way to trace a stream.

## Authentication

`rsc` supports [Authentication Extension](https://github.com/rsocket/rsocket/blob/master/Extensions/Security/Authentication.md) since 0.6.0.

The demo application is [here](https://github.com/making/demo-rsocket-security).

Note that since RSocket Java 1.0.3, [username field length is extended](https://github.com/rsocket/rsocket-java/pull/938).

rsc 0.6.0 uses RSocket Java 1.0.2. To support extended username, rsc 0.7.0 which uses RSocket Java 1.1.0 or above is required. 

### [Simple Authentication Type](https://github.com/rsocket/rsocket/blob/master/Extensions/Security/Simple.md)


To send credentials per stream, use `--authSimple <username>:<password>` option as follows: 

```
rsc tcp://localhost:8888 --authSimple user:password -r hello -d World
```

For shorter options, `--as` or `-u` (like `curl`!) are also available.

```
rsc tcp://localhost:8888 -u user:password -r hello -d World
```

To send credentials in `SETUP` payload, use `--sm simple:<username>:<password> --smmt message/x.rsocket.authentication.v0` as follows.

```
rsc tcp://localhost:8888 --sm simple:user:password --smmt message/x.rsocket.authentication.v0 -r hello -d World
```

slightly shorter version

```
rsc tcp://localhost:8888 --sm simple:user:password --smmt MESSAGE_RSOCKET_AUTHENTICATION -r hello -d World
```

### [Bearer Token Authentication Type](https://github.com/rsocket/rsocket/blob/master/Extensions/Security/Bearer.md)

To send token per stream, use `--authBearer <token>` option as follows: 

```
rsc tcp://localhost:8888 --authBearer MY_TOKEN -r hello -d World
```

For shorter option, `--ab` is also available.

To send credentials in `SETUP` payload, use `--sm token:<token> --smmt message/x.rsocket.authentication.v0` as follows.

```
rsc tcp://localhost:8888 --sm token:MY_TOKEN --smmt message/x.rsocket.authentication.v0 -r hello -d World
```

slightly shorter version

```
rsc tcp://localhost:8888 --sm token:MY_TOKEN --smmt MESSAGE_RSOCKET_AUTHENTICATION -r hello -d World
```

### Basic Authentication

[Basic Authentication](https://github.com/rsocket/rsocket/issues/272) is not a part of Authentication Extension.
It was implemented by Spring Security 5.2 before the spec was standardized.

`rsc` supports Basic Authentication for the backward compatibility with Spring Security 5.2.

To send credentials per stream, use `--authBasic <username>:<password>` option as follows: 

```
rsc tcp://localhost:8888 --authBasic user:password -r hello -d World
```

To send credentials in `SETUP` payload, use `--sm <username>:<password> --smmt message/x.rsocket.authentication.basic.v0` as follows.

```
rsc tcp://localhost:8888 --sm user:password --smmt message/x.rsocket.authentication.basic.v0 -r hello -d World
```

slightly shorter version

```
rsc tcp://localhost:8888 --sm user:password --smmt AUTHENTICATION_BASIC -r hello -d World
```

## Tracing

`rsc` supports [Tracing (Zipkin) Metadata Extension](https://github.com/rsocket/rsocket/blob/master/Extensions/Tracing-Zipkin.md) since 0.5.0

The demo application is [here](https://github.com/making/demo-rsocket-tracing).

```
$ rsc ws://localhost:8080/rsocket -r rr --trace --printB3 --zipkinUrl http://localhost:9411 
Hello World!
b3=5f035ed7dd21129b105564ef64c90731-105564ef64c90731-d
```

![image](https://user-images.githubusercontent.com/106908/86621556-5ad6a600-bff9-11ea-9040-8c300d2d8bcd.png)

## TODOs

- [x] Support resuming (0.3.0)
- [x] Support Composite Metadata (0.3.0)
- [x] Setup data (0.4.0)
- [x] Setup Metadata (0.6.0)
- [x] RSocket Authentication (0.6.0)
- [x] Request Channel (0.4.0)
- [x] Input from a file (0.8.0)
- [x] Input from STDIN (0.4.0)
- [ ] RSocket Routing Broker
- [ ] Client side responder

## Build

```
./mvnw clean package -Pnative -DskipTests
```

A native binary will be created in `target/classes/rsc-(osx|linux|windows)-x86_64` depending on your OS.

For linux binary, you can use Docker:

```
./mvnw spring-boot:build-image  -DskipTests
docker run --rm rsc:<version> --version  
```

### How to run E2E testing

```
git clone https://github.com/making/rsc-e2e
cd rsc-e2e
export RSC_PATH=...
export RSC_OIDCISSUERURL=https://uaa.run.pivotal.io/oauth/token # you can change this
export RSC_OIDCUSERNAME=...
export RSC_OIDCPASSWORD=...
./mvnw test
```

## License
Licensed under the Apache License, Version 2.0.

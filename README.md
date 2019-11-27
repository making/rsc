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
java -jar rsc.jar wss://rsocket-demo.herokuapp.com:443/ws --stream -d Trump --log
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
   -v "$HOME/.m2":/root/.m2 \
   -w /usr/src \
   oracle/graalvm-ce:19.2.1 \
   ./target/classes/rsc-linux-x86_64 --version
```

## License
Licensed under the Apache License, Version 2.0.
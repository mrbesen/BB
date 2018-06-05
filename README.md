# THIS REPO IS CONTINUED [HERE](http://git.mrbesen.de/mrbesen/bb)


# BB
*inefficent* computing network


## Start a Client:
```
java -jar Client.jar [host[:port]]
```
Host defaults to "127.0.0.1" and port default to "5454".

ctl + c to stop

## Start a Server
Warning: Server needs a HUGE amount of RAM!
```
java -jar -Xmx 6G -Xms 1G Server.jar [c:classname] [p:port]
```
classname default to "Test" and is the class thats loaded to generate Jobs.
port defaults to "5454".

to stop enter "stop" into the console.
Force Stop: ctl + c

crack.py and crack.sh need much ram.

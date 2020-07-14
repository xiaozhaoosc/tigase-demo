# tigase-demo
tigase-demo


## idea 
- vm 
````
-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -server -Xms100M -Xmx200M -XX:PermSize=32m -XX:MaxPermSize=256m -XX:MaxDirectMemorySize=128m -Djava.ext.dirs=D:\javaProject\tigase-demo\jars -Dtigase-configurator=tigase.shiku.conf.ShikuConfigurator


````
- program 
````
--property-file tigase-server/etc/init.properties
````

## linux 
- stop 
````
./scripts/tigase.sh stop etc/tigase.conf
````
- start 

````
./scripts/tigase.sh start etc/tigase.conf
tail -f logs/tigase-console.log
````
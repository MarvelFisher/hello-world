#!/bin/bash

# fix Apps could not login between LTS restart - AppServer restart
export LC_ALL="en_US.UTF-8"
WORKDIR=$PWD
cd $WORKDIR

if [ -s LTS_PID ]; then
LTS_PID=`cat LTS_PID`
kill $LTS_PID
fi

sleep 5
#ulimit -n 1024000

export LD_LIBRARY_PATH=$WORKDIR/windlib:$LD_LIBRARY_PATH

#JAVA_OPTS="-Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=$ip -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:log/gc`date +%Y%m%d_%H%M%S`.log -server -d64 -Xmx1g -Xms1g -XX:PermSize=256m -XX:+ParallelRefProcEnabled -XX:+PrintAdaptiveSizePolicy -XX:+HeapDumpOnOutOfMemoryError -XX:+DisableExplicitGC -XX:MaxGCPauseMillis=100 -XX:+UseG1GC -XX:MaxDirectMemorySize=1G -XX:AutoBoxCacheMax=3000000"
echo "Starting LTS "`date` | tee -a ./log/console.log
java ${JAVA_OPTS} -Duser.timezone=GMT+8 -cp jars/cyanspring-server-2.56.jar com.cyanspring.server.MarketDataServer conf/miserver.xml conf/common/mylog4j.xml >> ./log/console.log &
echo $! > LTS_PID


#!/bin/bash                                                                                                                                                                                            
CWD=`echo $(dirname $(readlink -f $0))`
cd $CWD

test -e server-cred.sh && source server-cred.sh

JAR=`ls target/scala-2.13/*.jar`
MAIN=io.syspulse.auth.App
CP="`pwd`/conf/:./$JAR:$JAR"

echo "=== Class Path ======================================="
echo "$CP"| sed "s/\:/\n/g"
echo "======================================================"
echo "OPT: $OPT"
echo "ARGS: $@"

# command:
EXEC="$JAVA_HOME/bin/java -Xss512M -cp $CP $AGENT $OPT $MAIN $@"
exec $EXEC

#!/bin/sh
#start-all.sh
# ----------------------------------------------------------------------------
#  Copyright 2005-2012 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
ls=`ls -ld "$PRG"`
link=`expr "$ls" : '.*-> \(.*\)$'`
if expr "$link" : '.*/.*' > /dev/null; then
PRG="$link"
else
PRG=`dirname "$PRG"`/"$link"
fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CARBON_HOME if not already set
[ -z "$CARBON_HOME" ] && CARBON_HOME=`cd "$PRGDIR/.." ; pwd`

sh "$CARBON_HOME/broker/bin/wso2server.sh" $* &
sleep 10
sh "$CARBON_HOME/analytics/bin/wso2server.sh" $* &
sleep 20
sh "$CARBON_HOME/core/bin/wso2server.sh" $* &

if [ ! -z "$*" ]; then
    exit;
else
    trap "sh $CARBON_HOME/bin/stop-all.sh; exit;" SIGINT SIGTERM
    while :
    do
            sleep 60
    done

fi
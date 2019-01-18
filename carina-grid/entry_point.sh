#!/bin/bash
#*******************************************************************************
# Copyright 2013-2019 QaProSoft (http://www.qaprosoft.com).
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#*******************************************************************************

ROOT=/opt/selenium
CONF=$ROOT/config.json

/opt/bin/generate_config >$CONF

echo "starting selenium hub with configuration:"
cat $CONF

if [ ! -z "$SE_OPTS" ]; then
  echo "appending selenium options: ${SE_OPTS}"
fi

function shutdown {
    echo "shutting down hub.."
    kill -s SIGTERM $NODE_PID
    wait $NODE_PID
    echo "shutdown complete"
}

java ${JAVA_OPTS} -DSTF_URL=$STF_URL -DSTF_TOKEN=$STF_TOKEN -cp /opt/selenium/selenium-server-standalone.jar:/opt/selenium/carina-grid-jar-with-dependencies.jar \
  org.openqa.grid.selenium.GridLauncherV3 \
  -role hub \
  -servlets com.qaprosoft.carina.grid.servlets.DeviceInfo,com.qaprosoft.carina.grid.servlets.ProxyInfo \
  -hubConfig $CONF \
  ${SE_OPTS} &
NODE_PID=$!

trap shutdown SIGTERM SIGINT
wait $NODE_PID

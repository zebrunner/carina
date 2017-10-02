#!/bin/bash

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

java ${JAVA_OPTS} -DSTF_URL=$STF_URL -DSTF_TOKEN=$STF_TOKEN -cp /opt/selenium/selenium-server-standalone.jar:/opt/selenium/carina-grid.jar \
  org.openqa.grid.selenium.GridLauncherV3 \
  -role hub \
  -hubConfig $CONF \
  ${SE_OPTS} &
NODE_PID=$!

trap shutdown SIGTERM SIGINT
wait $NODE_PID

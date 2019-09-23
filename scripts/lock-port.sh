#!/usr/bin/env bash
DSN="$ZOS_USERID.ZOWE.SDKLOCKS"
DSN_MEMBER="$DSN(P${TEST_PORT})"

echo "Locking test environment for port $TEST_PORT"

if ! zowe files ls am $DSN; then
    zowe files cre classic $DSN
fi

URL="https://river.zowe.org:10443/zosmf/restfiles/ds/$DSN_MEMBER"
echo URL=$URL

DATA="$CIRCLE_PROJECT_USERNAME:$CIRCLE_PROJECT_REPONAME:$CIRCLE_BUILD_NUM"

REPEAT=1
while [ "$REPEAT" -eq "1" ]; do
    curl --include --user $ZOS_USERID:$ZOS_PASSWORD --insecure -X PUT --header 'Content-Type: text/plain' --header 'Accept: application/json' --header 'X-IBM-Data-Type: text' --header 'X-IBM-Migrated-Recall: wait' --header 'X-IBM-Obtain-ENQ: SHRW' -d $DATA "$URL" > lock.out
    echo "1"
    if grep 'Enqueue for QNAME=SPFEDIT failed' lock.out; then
        echo "Waiting for lock to $DSN_MEMBER to be obtained"
        sleep 5
    else
        REPEAT=0
        if ! grep 'HTTP/1.1 20' lock.out; then
            cat lock.out
            echo "ERROR: Locking of $DSN_MEMBER failed"
            exit 1
        fi
    fi
done
SESSION_REF=$(grep 'X-IBM-Session-Ref:' lock.out | awk '{ print $2 }')
echo $SESSION_REF > .lock-session
rm -f lock.out

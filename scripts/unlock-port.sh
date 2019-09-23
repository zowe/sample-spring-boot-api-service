#!/usr/bin/env bash
DSN="$ZOS_USERID.ZOWE.SDKLOCKS"
DSN_MEMBER="$DSN(P${TEST_PORT})"

if [ ! -f ".lock-session" ]; then
    echo "WARN: .lock-session file does not exist"
    exit 0
fi

URL="https://river.zowe.org:10443/zosmf/restfiles/ds/$DSN_MEMBER"
echo URL=$URL

SESSION_REF=$(cat .lock-session)
echo SESSION_REF=$SESSION_REF
curl --include --user $ZOS_USERID:$ZOS_PASSWORD --insecure -X PUT --header 'Content-Type: text/plain' --header 'Accept: application/json' --header 'X-IBM-Data-Type: text' --header 'X-IBM-Migrated-Recall: wait' --header 'X-IBM-Release-ENQ: true' --header "X-IBM-Session-Ref:$SESSION_REF" -d '' "$URL" > unlock.out
if ! grep 'HTTP/1.1 204 No Content' unlock.out; then
    cat unlock.out
    echo "ERROR: Unlocking of $DSN_MEMBER failed"
    exit 1
fi
rm -f unlock.out
rm -f .lock-session

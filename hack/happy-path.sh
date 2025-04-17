#/bin/bash
set -euo pipefail
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
source $SCRIPT_DIR/common.sh;


echo "Start the workflow"

echo "Send process new shipment as if user used UI"
sendEvent "commands" "ProcessNewShipment" '{"shipment_id": "test", "blocks": [true, true, false, false, false, false, false, false, false]}'
# sendEvent "commands" "ProcessNewShipment" '{"shipment_id": "test", "blocks": [true, true, true, true, true, false, false, false, false]}'

echo "Wait 1 sec and then send the clearance button"
sleep 1
sendEvent "events" "AreaClearButtonPressed" "{}"
echo "Wait 4 secs for 'move block to nfc'"

# blocks=(red green red red green)
blocks=(green green)

for block in "${blocks[@]}"; do
    echo "Process block $block"

    if [[ $block == "green" ]]; then
        echo "Block is green, send NFC events"
        sendEvent "events" "NFCDistDetected" "{}"
        sendEvent "events" "NFCObjectDetected" "{}"
        sleep 1
        sendEvent "events" "NFCObjectRemoved" "{}"
        sendEvent "events" "NFCDistRemoved" "{}"

        sleep 5
    else
        echo "Block is $block, send NFC events (and wait for non-nfc-tag-event)"
        sendEvent "events" "NFCDistDetected" "{}"
        sleep 15
        sendEvent "events" "NFCDistRemoved" "{}"
        sendEvent "events" "RightObjectDetected" "{}"
        sleep 1
        sendEvent "events" "RightObjectRemoved" "{}"
        sendEvent "events" "LeftObjectDetected" "{}"
        sleep 1
        sendEvent "events" "LeftObjectRemoved" "{}"

        sleep 5
    fi
done

echo "Done."

#/bin/bash
set -euo pipefail
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
source $SCRIPT_DIR/common.sh;


echo "Start the workflow"

echo "Send process new shipment as if user used UI"
sendEvent "commands" "ProcessNewShipment" '{"shipment_id": "test", "blocks": [true, true, true, true, false, false, false, false, false]}'

echo "Wait 1 sec and then send the clearance button"
sleep 1
sendEvent "events" "AreaClearButtonPressed" "{}"

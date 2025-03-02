package events

import (
	"context"
	"sync"
)

func StartSendingEvents(ctx context.Context, wg *sync.WaitGroup) {
	go sendRotaryEvents(ctx, wg)
	go sendNFCEvents(ctx, wg)
	go sendDistanceEvents(ctx, wg)
}

package events

type Event struct {
	UID      string `json:"UID"`
	Type     string `json:"type"`
	Location string `json:"location"`
}

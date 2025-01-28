package dto

type CreateOrderRequest struct {
	ItemID   string `json:"itemId"`
	Quantity int    `json:"quantity"`
}

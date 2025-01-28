package dto

type CreateOrderRequest struct {
	OrderID  int64  `json:"orderId"`
	ItemID   string `json:"itemId"`
	Quantity int    `json:"quantity"`
}

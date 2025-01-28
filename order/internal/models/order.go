package models

type OrderStatus string

const (
	OrderStatusPending   OrderStatus = "Pending"
	OrderStatusConfirmed OrderStatus = "Confirmed"
	OrderStatusCancelled OrderStatus = "Cancelled"
)

type Order struct {
	ID       uint        `gorm:"primaryKey"`
	OrderID  int64       `gorm:"not null;uniqueIndex"`
	ItemID   string      `gorm:"not null"`
	Quantity int         `gorm:"not null"`
	Status   OrderStatus `gorm:"type:order_status;default:'Pending';not null"`
}

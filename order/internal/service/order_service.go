package service

import (
	"context"
	"encoding/json"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/mehmetcc/outbox-implementation/order/internal/dto"
	"github.com/mehmetcc/outbox-implementation/order/internal/models"
)

type OrderService struct {
	db *gorm.DB
}

func NewOrderService(db *gorm.DB) *OrderService {
	return &OrderService{db: db}
}

func (s *OrderService) CreateOrder(ctx context.Context, req dto.CreateOrderRequest) error {
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		order := models.Order{
			ItemID:   req.ItemID,
			Quantity: req.Quantity,
			Status:   models.OrderStatusPending,
		}

		if err := tx.Create(&order).Error; err != nil {
			return err
		}

		eventPayload := map[string]interface{}{
			"orderId":  order.ID,
			"itemId":   req.ItemID,
			"quantity": req.Quantity,
		}

		payloadBytes, _ := json.Marshal(eventPayload)

		outbox := models.Outbox{
			EventID:   uuid.NewString(),
			EventType: "OrderCreated",
			Payload:   payloadBytes,
			CreatedAt: time.Now().UTC(),
		}
		if err := tx.Create(&outbox).Error; err != nil {
			return err
		}

		return nil
	})
}

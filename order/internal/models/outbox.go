package models

import (
	"time"

	"gorm.io/datatypes"
)

type Outbox struct {
	ID        uint           `gorm:"primaryKey"`
	EventID   string         `gorm:"type:varchar(255);not null"`
	EventType string         `gorm:"type:varchar(255);not null"`
	Payload   datatypes.JSON `gorm:"type:jsonb;not null"`
	CreatedAt time.Time      `gorm:"not null;default:CURRENT_TIMESTAMP"`
}

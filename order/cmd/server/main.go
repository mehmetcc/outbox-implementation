package main

import (
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"

	"github.com/mehmetcc/outbox-implementation/order/internal/api"
	"github.com/mehmetcc/outbox-implementation/order/internal/models"
	"github.com/mehmetcc/outbox-implementation/order/internal/service"
)

func main() {
	dsn := "postgres://postgres:postgrespw@localhost:5432/orderdb?sslmode=disable"
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	// Create the Postgres ENUM type manually if it doesn't exist
	if err := db.Exec(`
        DO $$
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
                CREATE TYPE order_status AS ENUM ('Pending', 'Confirmed', 'Cancelled');
            END IF;
        END$$;
    `).Error; err != nil {
		log.Fatalf("Failed to create enum type: %v", err)
	}

	// Auto-migrate the models (tables) AFTER the enum type is ready
	if err := db.AutoMigrate(&models.Order{}, &models.Outbox{}); err != nil {
		log.Fatalf("AutoMigrate failed: %v", err)
	}

	// Initialize service
	orderService := service.NewOrderService(db)

	// Set up HTTP router
	r := mux.NewRouter()
	api.RegisterRoutes(r, orderService)

	log.Println("Listening on :9991...")
	if err := http.ListenAndServe(":9991", r); err != nil {
		log.Fatal(err)
	}
}

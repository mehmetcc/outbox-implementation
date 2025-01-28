package api

import (
	"encoding/json"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/mehmetcc/outbox-implementation/order/internal/dto"
	"github.com/mehmetcc/outbox-implementation/order/internal/service"
)

type Handler struct {
	orderService *service.OrderService
}

func NewHandler(orderService *service.OrderService) *Handler {
	return &Handler{
		orderService: orderService,
	}
}

func (h *Handler) CreateOrderHandler(w http.ResponseWriter, r *http.Request) {
	var req dto.CreateOrderRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON", http.StatusBadRequest)
		return
	}

	if req.Quantity < 1 {
		http.Error(w, "Quantity must be at least 1", http.StatusBadRequest)
		return
	}

	err := h.orderService.CreateOrder(r.Context(), req)
	if err != nil {
		http.Error(w, "Could not create order", http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusCreated)
	w.Write([]byte(`{"message": "order created"}`))
}

func RegisterRoutes(r *mux.Router, orderService *service.OrderService) {
	h := NewHandler(orderService)
	r.HandleFunc("/orders", h.CreateOrderHandler).Methods("POST")
}

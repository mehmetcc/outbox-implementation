CREATE TABLE Product (
    sku VARCHAR(36) PRIMARY KEY, -- Set VARCHAR length to 36 to match UUID length
    initial_stock INT NOT NULL,
    current_stock INT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create a function to update `is_available` based on `current_stock`
CREATE OR REPLACE FUNCTION update_availability()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.current_stock = 0 THEN
        NEW.is_available := FALSE;
    ELSE
        NEW.is_available := TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger to call the function before any UPDATE
CREATE TRIGGER trigger_update_availability
BEFORE UPDATE ON Product
FOR EACH ROW
EXECUTE FUNCTION update_availability();

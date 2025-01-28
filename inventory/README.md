# inventory

inventory is a generic ZIO2 microservice, designed to simulate an e-commerce stock management system.

## Running
In order to build this application, you need to have a running sbt and docker instance.

We start by publishing a fresh Docker image to our local repository:
```bash
sbt
Docker/publishLocal
```

We also need to have a running instance of PostgreSQL. For local testing purposes, there is a minimal setup for doing so:

```bash
docker compose -f local-database.yaml up
```

This project does not contain any database migration tool. As such, we have to generate the tables we are going to work with by hand:
```sql
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
```

The script can be found under sql folder and might be subject to change.


After that, run the following command:
```bash
docker run -it --user root inventory:0.1.0-SNAPSHOT
```

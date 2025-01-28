CREATE TABLE IF NOT EXISTS orders (
    id          SERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL UNIQUE,
    item_id     BIGINT NOT NULL,
    quantity    INT NOT NULL,
    status      order_status NOT NULL DEFAULT 'Pending'
);

-- You can create a Postgres enum type:
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
        CREATE TYPE order_status AS ENUM ('Pending', 'Confirmed', 'Cancelled');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS outbox (
    id          SERIAL PRIMARY KEY,
    event_id    VARCHAR(255) NOT NULL,
    event_type  VARCHAR(255) NOT NULL,
    payload     JSONB NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

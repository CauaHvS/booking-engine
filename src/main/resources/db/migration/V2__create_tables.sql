CREATE TABLE resources (
    id   UUID         PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE slots (
    id          UUID         PRIMARY KEY,
    resource_id UUID         NOT NULL REFERENCES resources(id),
    start_time  TIMESTAMP    NOT NULL,
    end_time    TIMESTAMP    NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE bookings (
    id      UUID         PRIMARY KEY,
    slot_id UUID         NOT NULL REFERENCES slots(id),
    user_id VARCHAR(255) NOT NULL,
    status  VARCHAR(20)  NOT NULL
);

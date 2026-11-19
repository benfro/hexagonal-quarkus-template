create table orders
(
    id          uuid primary key,
    customer_id varchar(64)              not null,
    status      varchar(16)              not null,
    created_at  timestamp with time zone not null
);

create table order_line
(
    order_id   uuid          not null references orders (id) on delete cascade,
    line_no    integer       not null,
    product_id varchar(64)   not null,
    quantity   integer       not null check (quantity > 0),
    unit_price numeric(12, 2) not null,
    primary key (order_id, line_no)
);

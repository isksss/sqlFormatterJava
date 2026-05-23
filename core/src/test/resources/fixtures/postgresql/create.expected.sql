create table if not exists public.orders(
  id bigint primary key,
  customer_id bigint not null,
  total numeric(12, 2) default 0,
  created_at timestamptz not null default now()
);
create index idx_orders_customer on public.orders(customer_id);

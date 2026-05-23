alter table
  public.orders
add
  column note text,
alter column
  total
set
  not null,
  drop column legacy_code;

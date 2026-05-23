with ranked as(
  select
    account_id,
    amount,
    row_number() over(
      partition by account_id
      order by
        created_at desc
    ) rn
  from
    payments
  where
    status = 'paid'
)
select
  account_id,
  case
    when amount > 100 then 'large'
    else 'small'
  end size
from
  ranked
where
  rn = 1;

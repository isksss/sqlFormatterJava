-- keep comment
select "order",price::numeric(10,2),created_at from "Sales Report" where note='do not alter :: text' and id=? /* keep block */;

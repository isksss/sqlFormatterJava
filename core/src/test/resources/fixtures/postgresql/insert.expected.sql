insert into
  audit_events(event_id, payload, tags)
values
  ($1, '{"kind":"login"}' :: jsonb, array [ 'auth', 'ok' ]) on conflict(event_id) do
update
set
  payload = excluded.payload returning event_id;

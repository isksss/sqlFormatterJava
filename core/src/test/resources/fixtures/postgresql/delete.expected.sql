delete from
  sessions
where
  expires_at < now()
  or user_id in(
    select
      id
    from
      users
    where
      disabled = true
  );

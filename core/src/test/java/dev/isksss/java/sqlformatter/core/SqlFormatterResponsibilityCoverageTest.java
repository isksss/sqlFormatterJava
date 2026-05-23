package dev.isksss.java.sqlformatter.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SqlFormatterResponsibilityCoverageTest {
    private static final FormatterConfig POSTGRESQL_THROW =
            new FormatterConfig("postgresql", null, null, null, null, ErrorPolicy.THROW, null);
    private static final FormatterConfig POSTGRESQL_KEEP_INPUT =
            new FormatterConfig("postgresql", null, null, null, null, ErrorPolicy.KEEP_INPUT, null);

    private final SqlFormatterService service = new SqlFormatterService();

    @ParameterizedTest(name = "{0}")
    @MethodSource("validSqlCases")
    void formatsValidSqlDeterministically(String name, String sql) {
        String formatted = service.format(sql, POSTGRESQL_THROW);

        assertAll(
                name,
                () -> assertFalse(formatted.isBlank()),
                () -> assertEquals(formatted, service.format(formatted, POSTGRESQL_THROW)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("brokenSqlCases")
    void recoversFromBrokenSql(String name, String sql) {
        String formatted = assertDoesNotThrow(() -> service.format(sql, POSTGRESQL_KEEP_INPUT), name);

        assertFalse(formatted.isBlank(), name);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tokenSensitiveCases")
    void preservesVariablesAndDialectTokens(String name, String sql, String token) {
        String formatted = service.format(sql, POSTGRESQL_THROW);

        assertTrue(formatted.contains(token), name + " should preserve " + token);
    }

    private static Stream<Arguments> validSqlCases() {
        return Stream.of(
                arg("select.basic", "select id,name from users"),
                arg("select.distinct", "select distinct account_id,status from events"),
                arg("select.alias", "select u.id as user_id,u.name display_name from users u"),
                arg("select.expression", "select (price*quantity)-discount total from order_lines"),
                arg("select.case", "select case when amount>100 then 'large' else 'small' end bucket from payments"),
                arg("select.scalar-subquery", "select id,(select max(created_at) from logins l where l.user_id=u.id) last_login from users u"),
                arg("select.exists", "select id from users u where exists(select 1 from orders o where o.user_id=u.id)"),
                arg("select.nested-condition", "select id from users where (active=true and locked=false) or role='admin'"),
                arg("select.function", "select coalesce(nullif(trim(name),''),'unknown') name from users"),
                arg("select.window", "select account_id,row_number() over(partition by account_id order by created_at desc) rn from payments"),
                arg("with.single", "with a as(select id from users) select * from a"),
                arg("with.multi", "with a as(select id from users),b as(select user_id from orders) select * from a join b on a.id=b.user_id"),
                arg("with.recursive", "with recursive t(n) as(select 1 union all select n+1 from t where n<5) select n from t"),
                arg("with.comments", "with a as(-- keep cte\nselect id from users) select * from a"),
                arg("from.derived", "select x.id from (select id from users where active=true) x"),
                arg("from.multiple", "select * from users u,accounts a where u.account_id=a.id"),
                arg("join.inner", "select * from users u join accounts a on u.account_id=a.id"),
                arg("join.left-and", "select * from users u left join accounts a on u.account_id=a.id and u.region=a.region"),
                arg("join.full-outer", "select * from a full outer join b on a.id=b.id"),
                arg("join.right", "select * from a right join b on a.id=b.id"),
                arg("join.quoted-and", "select * from a join b on a.label='x and y' and a.id=b.id"),
                arg("where.and-or", "select * from orders where status='paid' and amount>0 or priority=true"),
                arg("where.in", "select * from users where id in(select user_id from orders where total>100)"),
                arg("where.between", "select * from events where created_at between '2026-01-01' and '2026-01-31'"),
                arg("group.simple", "select account_id,count(*) from events group by account_id"),
                arg("group.having", "select account_id,count(*) c from events group by account_id having count(*)>1"),
                arg("order.nulls", "select * from users order by last_login desc nulls last,name asc"),
                arg("limit.offset", "select * from events order by id limit 10 offset 20"),
                arg("union", "select id from users union select user_id from orders"),
                arg("union.all", "select id from users union all select user_id from orders"),
                arg("insert.columns", "insert into users(id,name) values(1,'a')"),
                arg("insert.multi-row", "insert into users(id,name) values(1,'a'),(2,'b'),(3,'c')"),
                arg("insert.select", "insert into audit(user_id,created_at) select id,now() from users"),
                arg("insert.on-conflict", "insert into users(id,name) values($1,$2) on conflict(id) do update set name=excluded.name"),
                arg("update.simple", "update users set name='a',updated_at=now() where id=1"),
                arg("update.from", "update users u set name=s.name from staging_users s where u.id=s.id"),
                arg("delete.simple", "delete from users where inactive=true"),
                arg("delete.using", "delete from users u using banned_users b where u.id=b.user_id"),
                arg("merge.simple", "merge into target t using source s on t.id=s.id when matched then update set name=s.name when not matched then insert(id,name) values(s.id,s.name)"),
                arg("array.literal", "select array[1,2,3] as ids"),
                arg("json.operator", "select payload->>'kind' kind,payload#>>'{user,id}' user_id from events"),
                arg("json.construct", "select jsonb_build_object('id',id,'name',name) from users"),
                arg("comment.inline", "select id -- keep id\nfrom users where active=true"),
                arg("comment.block", "select /* keep block */ id from users"),
                arg("hint.optimizer", "select /*+ index(users idx_users_id) */ id from users where id=1"),
                arg("ddl.create-table", "create table users(id bigint primary key,name text not null,created_at timestamp)"),
                arg("ddl.create-table-constraint", "create table orders(id bigint,user_id bigint,constraint fk_user foreign key(user_id) references users(id))"),
                arg("ddl.alter-add", "alter table users add column nickname text"),
                arg("ddl.alter-drop", "alter table users drop column nickname"),
                arg("ddl.create-view", "create view active_users as select id,name from users where active=true"),
                arg("index.create", "create index idx_users_email on users(email)"),
                arg("index.unique", "create unique index idx_users_email_unique on users(lower(email)) where deleted_at is null"),
                arg("index.concurrently", "create index concurrently idx_events_created_at on events(created_at desc)"),
                arg("multi.select-update", "select * from users where id=$1; update users set last_seen=now() where id=$1;"),
                arg("multi.ddl-dml", "create table tmp_users(id bigint); insert into tmp_users(id) values(1); select * from tmp_users;"),
                arg("multi.transaction", "begin; update accounts set balance=balance-10 where id=1; update accounts set balance=balance+10 where id=2; commit;"),
                arg("variables.jdbc", "select * from users where id=? and active=?"),
                arg("variables.named", "select * from users where id=:user_id and status=:status"),
                arg("variables.postgres", "select * from users where id=$1 and status=$2"),
                arg("variables.cast", "select $1::jsonb->>'name' name"),
                arg("procedure.function", "create function add_one(i integer) returns integer language sql as $$ select i + 1; $$"),
                arg("procedure.plpgsql", "create function touch_user(p_id bigint) returns void language plpgsql as $$ begin update users set updated_at=now() where id=p_id; end; $$"),
                arg("procedure.do-block", "do $$ begin if exists(select 1 from users) then raise notice 'ok'; end if; end $$"),
                arg("messy.no-spaces", "select*from users where(id=1)and(active=true)"),
                arg("messy.mixed-case", "SeLeCt ID,Name FrOm Users WhErE Active=TRUE"),
                arg("messy.many-newlines", "select\n\nid,\n\nname\nfrom\nusers\nwhere\nactive=true"),
                arg("messy.tabs", "select\tid,\tname\tfrom\tusers\twhere\tactive=true"),
                arg("subquery.deep", "select * from users u where exists(select 1 from orders o where o.user_id=u.id and exists(select 1 from payments p where p.order_id=o.id))"),
                arg("function.multiline-target", "select greatest(coalesce(a,0),coalesce(b,0),coalesce(c,0)) from metrics"),
                arg("case.nested", "select case when a=1 then case when b=2 then 'x' else 'y' end else 'z' end from t"),
                arg("window.frame", "select sum(amount) over(partition by account_id order by created_at rows between unbounded preceding and current row) from payments"),
                arg("returning.insert", "insert into users(name) values('a') returning id,name"),
                arg("returning.delete", "delete from users where id=$1 returning id"),
                arg("locking", "select * from jobs where status='ready' order by id for update skip locked limit 1"));
    }

    private static Stream<Arguments> brokenSqlCases() {
        return Stream.of(
                arg("broken.empty-select", "select"),
                arg("broken.unclosed-string", "select 'abc from users"),
                arg("broken.unclosed-comment", "select /* comment from users"),
                arg("broken.unclosed-parenthesis", "select * from users where (id=1"),
                arg("broken.missing-cte-body", "with a as select * from a"),
                arg("broken.bad-join", "select * from a join on a.id=b.id"),
                arg("broken.trailing-comma", "select id, from users"),
                arg("broken.insert-values", "insert into users(id,name) values(1,)"),
                arg("broken.update-set", "update users set where id=1"),
                arg("broken.delete-where", "delete from users where and active=true"),
                arg("broken.create-table", "create table users(id bigint,"),
                arg("broken.alter", "alter table users add"),
                arg("broken.index", "create index on"),
                arg("broken.merge", "merge into t using s"),
                arg("broken.case", "select case when a=1 then 'x' from t"),
                arg("broken.procedure", "create function f() returns void as $$ begin"),
                arg("broken.json", "select payload-> from events"),
                arg("broken.array", "select array[1,2 from t"),
                arg("broken.multi", "select 1; update users set; insert into"),
                arg("broken.random", "select from where group by"));
    }

    private static Stream<Arguments> tokenSensitiveCases() {
        return Stream.of(
                Arguments.of("token.jdbc", "select * from users where id=? and active=?", "?"),
                Arguments.of("token.named", "select * from users where id=:user_id", ":user_id"),
                Arguments.of("token.postgres-position", "select * from users where id=$1", "$1"),
                Arguments.of("token.json-cast", "select $1::jsonb->>'name' name", "$1"),
                Arguments.of("token.single-quote", "select 'do not alter :: text' value", "'do not alter :: text'"),
                Arguments.of("token.double-quote", "select \"display Name\" from users", "\"display Name\""),
                Arguments.of("token.block-comment", "select /* keep block */ id from users", "/* keep block */"),
                Arguments.of("token.line-comment", "select id -- keep comment\nfrom users", "-- keep comment"),
                Arguments.of("token.hint", "select /*+ index(users idx_users_id) */ id from users", "/*+ index(users idx_users_id) */"),
                Arguments.of("token.dollar-quote", "create function f() returns int language sql as $$ select 1; $$", "$$"));
    }

    private static Arguments arg(String name, String sql) {
        return Arguments.of(name, sql);
    }
}

-- :name create-users-table
-- :command :execute
-- :result :raw
-- :doc Create users table
create table users (
  id         varchar(10) primary key,
  name       varchar(100),
  pass  varchar(100)
)

-- :name drop-users-table :!
-- :doc Drop users table if exists
drop table if exists users

-- :name insert-users :! :n
-- :doc Insert users
insert into users (id, name, pass)
values (:id, :name, :pass)

-- :name update-users :! :n
-- :doc Update users
update users
set name = :name, pass = :pass
where id = :id

-- :name delete-users :! :n
-- :doc Delete users
delete from users where id = :id

-- :name select-users :? :*
-- :doc Select users
select * from users order by id
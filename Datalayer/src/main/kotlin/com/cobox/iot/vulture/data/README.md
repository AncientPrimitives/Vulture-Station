# Vulture Station 数据库

## 建表脚本
```
-- 开启外键开关，Sqlite3需要指定
pragma foreign_keys = on;

-- 创建nas业务表
drop table if exists nas;
create table nas (
   record_id integer primary key autoincrement,
   group_id integer not null,
   url text not null,
   mime_type not null,
   media_type integer,
   bucket_id integer
);

-- 创建iot业务表
drop table if exists iot;
create table iot (
   id integer primary key autoincrement
);

-- 创建user_info表，并关联nas业务表和iot业务表的外键
drop table if exists user_info;
create table user_info (
   id integer primary key autoincrement,
   username text not null,
   secret text not null,
   token text not null,

   nas_id integer,
   iot_id integer,

   FOREIGN KEY (nas_id) REFERENCES nas(record_id) ON DELETE CASCADE ON UPDATE CASCADE,
   FOREIGN KEY (iot_id) REFERENCES iot(id) ON DELETE CASCADE ON UPDATE CASCADE
);
```
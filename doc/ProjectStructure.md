# 工程结构介绍

## 现有模块
module   | component                                     | class
 ---------|-----------------------------------------------|-------
gateway  | gateway, router, router_mgr                   |
nas      | nas_server(sub_router), nas_dao, nas_table    |
iot      | iot_server(sub_router), iot_dao, iot_table    |
user     | user_server(sub_router), user_dao, user_table |
database |                                               |
sdk      | framework                                     | VultureServer, VultureDao, VultureTable
sdk      | business, standard                            |


## 架构层级

![层级图](doc/img/层级.png)

## 模块结构

![模块图](doc/img/模块.png)
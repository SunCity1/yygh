# 网上预约挂号系统
## 项目介绍
本项目是网上预约挂号系统，一个基于微服务架构的前后端分离系统。网上预约挂号是近年来开展的一项便民就医服务，旨在缓解看病难、挂号难的就医难题，许多患者为看一次病要跑很多次医院，最终还不一定能保证看得上医生。网上预约挂号全面提供的预约挂号业务从根本上解决了这一就医难题。随时随地轻松挂号！不用排长队！


## 项目目录
* conmmon：公共模块父节点
  * common_util：工具类模块，比如exception、Jwt、Result 返回类型等
  * rabbit_util：rabbitmq 业务封装，比如配置，交换机队列常量定义，生产消息方法等
  * service_util：service服务的工具包，包含service服务的公共配置类，比如 Redis、Swagger 配置
* model：实体类模块
* service_gateway：服务网关
* service：api接口服务父节点
  * service_cmn：公共api接口服务
  * service_hosp：医院api接口服务
  * service_msm：短信 api接口服务
  * service_order：订单api接口服务
  * service_oss：文件api接口服务
  * service_statistics：统计api接口服务
  * service_task：定时任务服务
  * service_user：用户api接口服务
* service-client：feign服务调用父节点
  * service_cmn_client：公共api接口
  * service_hosp_client：医院api接口
  * service_order_client：订单api接口
  * service_user_client：用户api接口

## 项目选型

### 系统建构图

![image](https://user-images.githubusercontent.com/63488829/180612422-f1f155ec-cd05-425f-99a6-539c53aa205c.png)

### 后端技术

|      技术      |           说明            |                             官网                             |
| :------------: | :-----------------------: | :----------------------------------------------------------: |
|   SpringBoot   |          MVC框架          | [ https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot) |
|  SpringCloud   |        微服务框架         |           https://spring.io/projects/spring-cloud/           |
|  MyBatis-Plus  |          ORM框架          |                   https://mp.baomidou.com/                   |
|   Swagger-UI   |       文档生产工具        | [ https://github.com/swagger-api/swagger-ui](https://github.com/swagger-api/swagger-ui) |
|    RabbitMQ    |         消息队列          |   [ https://www.rabbitmq.com/](https://www.rabbitmq.com/)    |
|     Redis      |        分布式缓存         |                      https://redis.io/                       |
|      JWT       |        JWT登录支持        |                 https://github.com/jwtk/jjwt                 |
|     SLF4J      |         日志框架          |                    http://www.slf4j.org/                     |
|     Lombok     |     简化对象封装工具      | [ https://github.com/rzwitserloot/lombok](https://github.com/rzwitserloot/lombok) |
|     Nginx      |  HTTP和反向代理web服务器  |                      http://nginx.org/                       |
|    JustAuth    |     第三方登录的工具      |             https://github.com/justauth/JustAuth             |

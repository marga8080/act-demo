# act-demo
springboot（2.1.2.RELEASE） 集成Activiti（5.22.0）demo

## 配置数据库地址

在`application.yml`中配置数据库地址，第一次启动工程时会把activiti的25张表初始化到数据库

```yml
spring:     
  driverClassName: com.p6spy.engine.spy.P6SpyDriver
  url: jdbc:p6spy:mysql://127.0.0.1:3306/act_demo?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8
  username: root
  password: 123456
```



## 访问地址

http://127.0.0.1:7070/index





## 参考链接

[SpringBoot集成Activiti之在线设计器](<https://www.jianshu.com/p/41f11c99167a?tdsourcetag=s_pcqq_aiomsg>)

[springBoot2集成activiti，集成activiti在线设计器](<https://www.cnblogs.com/zhouyun-yx/p/10410274.html>)






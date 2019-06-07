# 商品秒杀系统
## 高并发架构
## 1.框架：SpringBoot2.1.4, MyBatis2.0.1
## 2.数据库：MySQL5.1.21 ， Redis3.0.7（高速缓存）
## 4.消息队列： RabbitMQ(实现异步下单)
## 5.负载均衡： Nginx(未做)
## 6.系统压测： Jemter(模拟多线程的工具)
## 7.Tomcat集群(未做)


ID | Module | Problem 
:-: | :-: | :-: 
1 | 安全问题 | 如何保证用户密码的安全性？（两次MD5） |
2 | 安全问题 | 如何减少秒杀地址被攻击？（秒杀时动态获取地址） |
3 | 安全问题 | 如何防止机器人恶意秒杀？（数学公式验证码） | 
4 | 安全问题 | 如何做接口限流防刷？（限制用户在规定时间的访问次数）| 
5 | 登录功能 | 如何保持用户的登录状态？（单服务器、分布式session）| 
6 | 秒杀功能 | 数据表设计：为什么要有秒杀商品/订单/库存表？| 
7 | 秒杀功能 | 如何判断秒杀客户端是android / ios / pc ？| 
8 | 计算机网络 | 同样的网络，为什么内网ip会变化？（DHCP）| 
9 | 分布式一致性 | 在分布式下如何保证生成id的唯一性？（SnowFlake）|
10 | 秒杀功能 | 如何保证每个客户端的秒杀倒计时不出现偏差？（以服务器时间为准）|
11 | Springboot | SpringBoot事务管理？ |
12 | 代码优化 | SpringBoot自定义参数解析HandlerMethodArgumentResolver？|
13 | 代码优化 | 如何高效实现参数校验，少写重复代码？（JSR303）|
14 | Springboot | 注解@Configuration、@Bean详解？|
15 | Mybatis | 数据库操作CRUD的返回值？ |
16 | Mybatis | @SelectKey的参数解析？ |
17 | Springboot | vo类的成员变量是怎么映射到数据库表的字段上的？ |
18 | MySQL | 函数LAST_INSERT_ID()的使用方法？ |
19 | 性能优化 | 并发瓶颈、Redis性能瓶颈、横向扩展与纵向扩展？ |
20 | 性能优化 | 纵向扩展：数据库分库分表思路？（垂直/水平切分） |
21 | 性能优化 | 高并发下的缓存更新策略（Cache Aside Pattern） |
22 | Java | Java中的注解是如何工作的？ |
23 | 代码优化 | 同一时间段，不同接口能承受的访问次数不一样，如何写一个通用的限流方法以适应所有的情况？ |
24 | 秒杀功能 | 如何从根本上解决超卖问题？（Redis预减库存） |
25 | 秒杀功能 | 如何从根本上解决重复秒杀问题？（数据库唯一索引） |
26 | 库存模型 | 本项目的商品库存模型总结？ |
27 | 秒杀功能 | 本项目的秒杀具体流程？ |
28 | Springboot | templates和static的区别？ |
29 | 多线程 | 项目的多线程和高并发体现在哪里？ |
30 | RabbitMQ | 四种交换机exchange模式（direct、topic、fanout、headers） |
31 | 页面优化 | thymeleaf和“页面静态化、前后端分离”的区别？ |
32 | 页面优化 | 如何优化页面，从而提高性能？ |



<a href="https://github.com/603599551/miaosha_1/blob/master/code-solve.md">以上问题的详解</a>

**未解决的问题**
Q1：在并发量达到一定数量级时，是否需要采用Redis Cluster来存储session？
Q2：Redis Cluster的应用场景？
Q3：该项目应该拓展成分布式，那么会涉及到很多问题，比如分布式事务？
Q4：Redis并发数达到一定时，为什么会使value为null？
Q5：GET和POST方法的区别 详解
Q6：HTTP中的缓存控制开关Pragma 和 Cache-Control -- https://blog.csdn.net/u012375924/article/details/82806617
Q7：秒杀优化
    Nginx水平扩展--安装Nginx、反向代理、负载均衡
    百万级并发 客户端->Nginx->Tomcat
    千万级并发 客户端->LVS->Nginx->Tomcat




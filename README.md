# 商品秒杀系统
## 1.框架：SpringBoot(2.1.4) , MyBatis(2.0.1)
## 2.数据库：MySQL(5.1.21) ， Redis(3.0.7)
## 3.缓存持久化：Redis(3.0.7)
## 4.消息队列： RabbitMQ(待更新)
## 5.负载均衡： Nginx(待更新)
## 6.系统压测： Jemter(待更新)
## 7.Tomcat集群(待更新)

## 8.安全问题(持续更新中)
### 8.1 两次MD5：保证用户密码的安全
#### 8.1.1 第一次MD5
##### 用户端：PASSWORD1=MD5(密码明文+固定salt)
##### 第一次MD5是为了保证密码明文在网络传输过程中的安全
#### 8.1.2 第二次MD5
##### 服务端：PASSWORD2=MD5(PASSWORD1+随机salt)，然后将PASSWORD2和随机salt存入数据库
##### 第二次MD5是为了防止数据库被盗的情况下的密码安全。（因为通过彩虹表有可能反推出密码明文。）
##### 加盐后的密码经过哈希加密得到的哈希串与加盐前的哈希串完全不同，黑客用彩虹表得到的密码根本就不是真正的密码。
##### 即使黑客知道了“盐”的内容、加盐的位置，还需要对哈希加密函数H和规约函数R进行修改，彩虹表也需要重新生成，因此加盐能大大增加利用彩虹表攻击的难度。

## 9.功能模块(持续更新中)
### 9.1 登录功能
#### 9.1.1 用户密码两次MD5(已完成)
#### 9.1.2 使用JSR303校验参数(已完成)

------------------------------------------------------------------------------------------------------------------------
#### 9.1.3 分布式session(已完成)

*****

##### 如何实现单服务器下的session？（保持用户的登录状态）
+ ①用户第一次登录成功，服务器为该用户创建一个session，并生成唯一的sessionID，然后就可以调用相关方法往session中增加内容了。
+ ②服务器将session存储在服务器的内存中，只把sessionID存到cookie中，并将cookie发送给客户端。
+ ③当客户端再次发送请求时，会把sessionID带上，服务器接收到请求后可以根据sessionID在内存中找到相应的session。

*****

##### 分布式Session共享解决方案

**问题原因：** 单服务器web应用中，session只会存储在该服务器中；当采用服务器集群时，若采用原先的session方案，会出现类似以下的问题：
用户的第一个请求发送到了服务器1，第二个请求发送到服务器2而找不到session，因此我们需要解决分布式session一致性的问题。

**方案1：session复制 （性能低，实现复杂，不常用）**
即 服务器之间同步session对象，使得每台服务器上都保存所有的session信息，使用session时，直接从本地获取。

**方案2：基于redis缓存的session共享**
即 用redis来存储所有的session信息，服务器每次读写session都访问redis。

$\color{red}{Q1：在并发量达到一定数量级时，是否需要采用Redis Cluster来存储session？}$
$\color{red}{Q2：Redis Cluster的应用场景？}$

*****

##### 本项目的分布式session具体实现如下：
+ 登录成功，服务端给用户生成一个token来标识用户(token相当于sessionID)；
+ 服务端将token和用户信息存到redis缓存中，key-token，value-用户信息；
+ 服务端同时将token写到cookie中，传递给客户端，客户端在随后的请求中都会携带着token；
+ 服务端拿到token之后，就根据token取到对应的session信息.

**注意事项：** 客户端在登录成功后，每次发送请求都带着token，服务器要验证token来保持客户端的登录状态，并更新cookie和session的有效期。

#### 9.1.4 登录流程总结(待更新)
参数格式校验->验证账号密码是否正确->登录成功
### 秒杀常见问题

#### <font color=#6495ED face="黑体">1.如何保证用户密码的安全性（两次MD5）</font>
```
 -- 第一次MD5：由客户端完成，PASSWORD1=MD5(密码明文+固定salt)，保证明文密码在网络传输过程中的安全。  
 -- 第二次MD5：由服务端完成，PASSWORD2=MD5(PASSWORD1+随机salt)，然后将PASSWORD2和随机salt存入数据库。  
        第二次MD5是为了防止数据库被盗的情况下的密码安全。（因为通过彩虹表有可能反推出密码明文。）  
        加盐后的密码经过哈希加密得到的哈希串与加盐前的哈希串完全不同，黑客用彩虹表得到的密码根本就不是真正的密码。  
```

#### <font color=#6495ED face="黑体">2.如何减少秒杀地址被攻击？（秒杀时动态获取地址）</font> 
```
 -- 在秒杀开始之前，接口地址不是写死的，需要从服务端获取，动态拼成一个地址，作用：安全。
 -- 1.用户点击“立即秒杀”，服务端随机生成一个path参数
 -- 2.key-用户、秒杀商品，value-path 存入Redis缓存
 -- 3.访问由随机生成的path拼接而成的秒杀地址，首先查询Redis缓存来验证秒杀地址是否正确，若正确就进行正常的秒杀操作。
```

#### <font color=#6495ED face="黑体">3.如何防止机器人恶意秒杀？（数学公式验证码）</font> 
```
 -- 作用： 1.防机器人 2.由于用户输入验证码的速度不同，因此可以把原先瞬间的并发量分散到多个时刻，达到削减并发量的效果。
 -- 步骤1：服务端随机生成算术表达式，然后画在一张图片上，把算式的结果存在Redis缓存中，向客户端输出图片。
 -- 步骤2：客户端在点击“立即秒杀时”要输入验证码，服务端将收到的结果和Redis缓存中的比对，成功或者失败都要及时删除Redis缓存中的结果。
```

#### <font color=#6495ED face="黑体">4.如何做接口限流防刷？（限制用户在规定时间的访问次数）</font> 
```
 -- 思路：系统本身能承受的并发量是有限的，我们要限制一个用户在一分钟之内只能访问某个地址多少次，超过这个次数就认为你是非法的。
 -- 步骤1：第一次访问，把访问次数写到缓存，同时给次数加个有效期。key-用户id+访问URI value-次数1
 -- 步骤2：一分钟之内如果再次访问，访问次数+1。
 -- 步骤3：如果一分钟之内超过限定数值就直接返回失败；否则到了下一个一分钟，缓存失效，相当于从0开始计算次数。
```

#### <font color=#6495ED face="黑体">5.如何保持用户的登录状态？（单服务器、分布式session）</font> 
```
 -- 如何实现单服务器下的session？
 -- ①用户第一次登录成功，服务器为该用户创建一个session，并生成唯一的sessionID，然后就可以调用相关方法往session中增加内容了。
 -- ②服务器将session存储在服务器的内存中，只把sessionID存到cookie中，并将cookie发送给客户端。
 -- ③当客户端再次发送请求时，会把sessionID带上，服务器接收到请求后可以根据sessionID在内存中找到相应的session。

 -- 分布式Session共享解决方案 2种
 -- 问题原因：单服务器web应用中，session只会存储在该服务器中；当采用服务器集群时，若采用原先的session方案，会出现类似以下的问题：
             用户的第一个请求发送到了服务器1，第二个请求发送到服务器2而找不到session，因此我们需要解决分布式session一致性的问题。
 
 -- 方案1：session复制 （性能低，实现复杂，不常用）
    服务器之间同步session对象，使得每台服务器上都保存所有的session信息，使用session时，直接从本地获取。
    
 -- 方案2：基于redis缓存的session共享（常用）
    用redis来存储所有的session信息，服务器每次读写session都访问redis。
    
 -- 本项目的分布式session具体实现如下：
 -- ①登录成功，服务端给用户生成一个token来标识用户(token相当于sessionID)；
 -- ②服务端将token和用户信息存到redis缓存中，key-token，value-用户信息；
 -- ③服务端同时将token写到cookie中，传递给客户端，客户端在随后的请求中都会携带着token；
 -- ④服务端拿到token之后，就根据token取到对应的session信息.
 -- ps：客户端在登录成功后，每次发送请求都带着token，服务器要验证token来保持客户端的登录状态，并更新cookie和session的有效期。
```

#### <font color=#6495ED face="黑体">6.数据表设计：为什么要有秒杀商品/订单/库存表？</font> 
```
 -- 秒杀活动涉及到商品信息、可秒杀的库存、秒杀时间等等。
 -- 如果不给秒杀单独成表，那么就需要在总表中添加一个或多个标识字段，总表会变得难以维护。
 -- ps：在实际项目中，数据库中的价格不会存小数，都会存整数，单位是“分”。
```

#### <font color=#6495ED face="黑体">7.如何判断秒杀客户端是android / ios / pc ？</font> 
```
 -- 前端js通过判断浏览器navigator的userAgent就可以判断客户端类型。
```

#### <font color=#6495ED face="黑体">8.同样的网络，为什么内网ip会变化？（DHCP）</font> 
```
 -- 动态主机配置协议DHCP
 -- 由于IP地址资源有限，宽带接入运营商不能做到给每个用户都能分配一个固定的IP地址（所谓固定IP就是即使在你不上网的时候，别人也不能用这个IP地址，这个资源一直被你所独占），
    所以要采用（动态主机配置协议）DHCP方式对局域网客户机进行临时的地址分配。（每次上网分配的IP地址可能会不一样，这跟当时IP地址资源有关。）
    即由DHCP服务器控制一段IP地址范围，客户机登录服务器时就可以自动获得服务器分配的IP地址和子网掩码。
```
#### <font color=#6495ED face="黑体">9.在分布式下如何保证生成id的唯一性？（SnowFlake）</font> 
```
 -- 分布式id生成算法SnowFlake：生成id的结果是一个64位大小的整数
 -- 算法详解：https://segmentfault.com/a/1190000011282426?utm_source=tag-newest
 -- ps：由于本项目没有采用分布式，因此是直接使用UUID/ID自增的方式实现。
```
#### <font color=#6495ED face="黑体">10.如何保证每个客户端的秒杀倒计时不出现偏差？（以服务器时间为准）</font> 
```
 -- 客户端的秒杀倒计时只有满足以下2个条件才能保证“无偏差”
    1.以服务器时间为准；
    2.要考虑网络传输的耗时。
    
 -- 我们要记录以下3个时间戳：
    client_request : 客户端向服务器发送请求的时间戳；
    client_start : 客户端接收到服务器返回的时间戳并开始计时的时间戳；
    server_response : 服务器响应客户端请求的时间戳；
 
 -- 要计算2个参数：
    网络传输的耗时 = |client_start - client_request| 
    时间偏差 = |client_start - server_response| - 网络传输的耗时
 
 -- 那么客户端可秒杀的时间要满足以下不等式： 
    本地时间 + 时间偏差 + 网络传输的耗时 < 秒杀截止时间 
    即 本地时间 + |client_start - server_response| < 秒杀截止时间

 -- 在保证倒计时“无偏差”的情况下，我们也要兼顾服务器性能的问题，因此程序实现思路如下：
    当用户第一次浏览页面时，客户端首先获取服务器的当前时间
    客户端的新时间以服务器时间为初始值，每隔一秒累加一秒并生成新的时间
    客户端的倒计时 = 服务器秒杀截止时间 - 服务器当前时间 - 网络传输耗时
 
 思路来源：https://www.jb51.net/article/72305.htm
 ps：本项目现在实现的“秒杀倒计时”是以客户端的时间为准。
```

#### <font color=#6495ED face="黑体">11.SpringBoot事务管理？</font> 
```
 -- SpringBoot事务管理：编程式、声明式（其中包括@Transactional）。
    详解：https://blog.csdn.net/nextyu/article/details/78669997
```

#### <font color=#6495ED face="黑体">12.自定义参数解析HandlerMethodArgumentResolver？</font> 
```
 -- 自定义参数解析器需要实现HandlerMethodArgumentResolver接口，可以解析的参数对象类型包括 自定义类、注解等等 
    应用实例：https://www.jianshu.com/p/40606baf49b8
    ps：本项目自定义了针对user对象的参数解析器，这么做的好处是可以省去大量用于XXX（如：验证登录）的重复代码。
```

#### <font color=#6495ED face="黑体">13.如何高效实现参数校验，少写重复代码？（JSR303）</font> 
```
 -- 本项目简单应用在对登录请求的参数校验上，5个相关文件为：
    IsMobile,isMobileValidator,ValidatorUtil,LoginCtrl,LoginVo
    JSR303详解：https://www.ibm.com/developerworks/cn/java/j-lo-jsr303/index.html
```

#### <font color=#6495ED face="黑体">14.注解@Configuration、@Bean详解？</font> 
```
 -- @Configuration 注释的类 类似于于一个 xml 配置文件的存在。
    它指示一个类声明一个或多个@Bean方法，并且可以由Spring容器处理，以便在运行时为这些bean生成BeanDefinition和服务请求。
    @Configuration注解介绍：https://www.jianshu.com/p/721c76c1529c
    @Bean注解的使用：https://www.cnblogs.com/feiyu127/p/7700090.html
```

#### <font color=#6495ED face="黑体">15.数据库操作CRUD的返回值？</font> 
```
 -- insert：返回值是插入成功的行数，但注解配置@SelectKey后，可以将新插入行的id映射到domain中相应的属性。
 -- update/delete：返回值是更新或删除的行数；无需指明resultClass；但如果有约束异常而删除失败，只能去捕捉异常。
 -- queryForObject：返回的是一个实例对象或null；需要包含<select>语句，并且指明resultMap。
 -- queryForList：返回的是实例对象的列表；需要包含<select>语句，并且指明resultMap。
```

#### <font color=#6495ED face="黑体">16.@SelectKey的参数解析？</font> 
```
 -- keyColumn -- 数据库的列  ； 
 -- keyProperty -- 指定返回的列映射到domain中的哪个属性 ；
 -- resultType -- 返回值的类型 ；
 -- before -- SelectKey语句的执行是否在insert语句之前 ； 
 -- statement -- 要运行的SQL语句 。
```

#### <font color=#6495ED face="黑体">17.vo类的成员变量是怎么映射到数据库表的字段上的？</font> 
```
 -- 通过application.properties配置的参数mybatis.configuration.map-underscore-to-camel-case可以让mybatis自动将SQL中查出来的带下划线的字段，转换为驼峰标志，再去匹配类中的属性。
```

#### <font color=#6495ED face="黑体">18.函数LAST_INSERT_ID()的使用方法？</font> 
```
 -- MYSQL的函数LAST_INSERT_ID()：若插入一条数据，得到刚INSERT的行的主键值；若一条INSERT语句插入多行，只会返回插入的第一行数据时产生的值。 
 -- 为什么LAST_INSERT_ID()只适用于自增主键？
    MYSQL官方手册说明的两种使用方法：
    1.LAST_INSERT_ID()不带参数，与AUTO_INCREMENT属性一起使用，插入新记录时，返回该自增字段的值。
    2.LAST_INSERT_ID(value+1)带参数，返回的是表达式“value+1”的值。
```

#### <font color=#6495ED face="黑体">19.并发瓶颈、Redis性能瓶颈、横向扩展与纵向扩展？</font> 
```
 -- 并发的瓶颈在于MYSQL，只要数据库扛得住压力，高并发的性能很容易提高，否则系统离宕机就不远了
 -- Redis比MYSQL快很多很多，Redis性能的瓶颈是网络
 -- 横向扩展：扩展服务器的数量。
 -- 纵向扩展：增加单机（cpu）的处理能力。
```

#### <font color=#6495ED face="黑体">20.纵向扩展：数据库分库分表思路？（垂直/水平切分）</font> 
```
 -- 关系型数据库本身比较容易成为系统瓶颈，单机存储容量、连接数、处理能力都有限。
    当单表的数据量达到1000W或100G以后，由于查询维度较多，即使添加从库、优化索引，做很多操作时性能仍下降严重。
    分布式数据库的核心内容是数据切分，以及切分后对数据的定位、整合。而数据切分的目的就在于减少单一数据库的负担，缩短查询时间。
    数据切分根据其切分类型，可以分为两种方式：垂直（纵向）切分和水平（横向）切分
 -- 1、垂直（纵向）切分
 -- 2、水平（横向）切分
 -- 3、分库分表带来的问题
 -- 4、什么时候需要做数据切分
 -- 5、案例分析
 -- 6、支持分库分表的中间件
 -- 
 ```
<a href="https://mp.weixin.qq.com/s?__biz=MzU2MTI4MjI0MQ==&mid=2247486361&idx=2&sn=54a666b5ff5398ea1062fe1ab9a446b1&chksm=fc7a6637cb0def211e37176547cc072b5d57dba2a49b3ba42c6face9543435587265bb609566&mpshare=1&scene=1&srcid=&key=aba27b4d9f74947f227b57b2b825d4184225b0e3a5014deed0a7ac0bf9d57e0e0fd7a09e074a444ca88deabb2299fd989503f75d49f5a4f12477a9d8a5488f5c0e54bbdab9ec6b68ee664a64261ead9d&ascene=1&uin=MjUxNDc0NDk4MA%3D%3D&devicetype=Windows+10&version=6206081f&lang=zh_CN&pass_ticket=MXiR9TZrde3Emj6nTSSkvehiNCqMqUqhfpfBPHgx7OlcnuuTdroZCosNEOeSrSxE
">“数据库分库分表”详解</a>

#### <font color=#6495ED face="黑体">21.高并发下的缓存更新策略（Cache Aside Pattern）？</font> 
```
 -- 缓存失效：应用程序先从cache取数据，没有得到的话就从数据库中取数据，成功后再放到缓存中。
 -- 缓存命中：应用程序从cache中取到数据后返回。
 -- 缓存更新：先把新数据存到数据库中，成功后再让缓存失效。
 
 -- 在缓存更新时，为什么先存数据库再更新缓存，顺序反过来可以吗？
    不可以，这个先后顺序是为了保证数据库和缓存信息的一致性。
    若先让缓存失效，再把新数据存到数据库，在以下情况会有问题：这时候来了一个读数据库的操作，将旧数据加载到缓存里，然后把新数据更新数据库。
 
 -- 在缓存更新时，为什么是删除缓存，而不是更新缓存？
    很多时候，在复杂点的缓存场景，缓存不单单是数据库中直接取出来的值。若频繁修改一个缓存涉及到的多个表，那么缓存也频繁更新。
    问题在于这个缓存会不会被频繁的访问？若一个缓存涉及的表的字段在1分钟内被修改了100次，那么缓存就更新100次，但是这个缓存在1分钟内只被访问了1次。
    但是如果采用删除缓存的策略，那么只在访问缓存时计算缓存，开销就可以大幅度降低了。
    （比如：复杂缓存计算，比如可能更新了某个表的一个字段，然后其对应的缓存，是需要查询另外两个表的数据并进行运算，才能计算出缓存最新的值的。）
```
<a href="https://mp.weixin.qq.com/s?__biz=MzI5NTYwNDQxNA==&mid=2247484970&idx=1&sn=483aebb007a2df784a84ed6bbb3efa85&chksm=ec505ffbdb27d6edb212e7aa1a2bd497bcf390ba703746ff51447fa4cbbbfc0c0eaa74cac743&mpshare=1&scene=1&srcid=&key=db761600f54d432f505d0a011531586094b204bdf4b3af5f809377761b5154eed61268d6ab1af1cf105e37117df9e7110aafccfc92804bae167e3b9a1bea2851420010c1ce31513b2292a364c16d455e&ascene=1&uin=MjUxNDc0NDk4MA%3D%3D&devicetype=Windows+10&version=6206081f&lang=zh_CN&pass_ticket=y8HpZk0B2xZxQhyR5SrjyfTg1wpVEhHF%2FzrVzo2g656xDhrDIWyO6tE4G7X6drui
">如何保证缓存与数据库的双写一致性？（CAP详解）</a>


#### <font color=#6495ED face="黑体">22.Java中的注解是如何工作的？</font> 
```
 -- 什么是注解Annotation？
 -- 为什么要引入Annotation？
 -- Annotation是如何工作的？
 -- 怎么编写自定义的Annotation？
 -- 注解的使用方法（如何加到某个方法的开头）
 -- 怎么调用自定义的Annotation？
 -- ps：xmind已经总结了该知识点。
 ```
<a href="https://mp.weixin.qq.com/s?__biz=MzU2MTI4MjI0MQ==&mid=2247486430&idx=2&sn=99d6f347a9abc26960f22983e0ed2e82&chksm=fc7a6670cb0def660ad200ec4f13c6907fdcd5fd324aef7c2540448e67e3eb997041f3256620&mpshare=1&scene=1&srcid=&key=b696c313200170491859bcffe546973067715b55e53da8e0c0c07356ead8257df34ce627ae5d3f42fe3ab267d814de63af4cc71ee90194cc33d4e2ed71b3c2e880d2109892f6bf7d8f70cb9684342c80&ascene=1&uin=MjUxNDc0NDk4MA%3D%3D&devicetype=Windows+10&version=6206081f&lang=zh_CN&pass_ticket=HuTbgvYHLZgwQ8o2bzMasYCuvw%2BaT7HJCgTp0aznFhkGeyDzdnCN7%2Fkvg6UIwOG2
">Java中的注解是如何工作的？详解</a>

#### <font color=#6495ED face="黑体">23.同一时间段，不同接口能承受的访问次数不一样，如何写一个通用的限流方法以适应所有的情况？</font> 
```
 -- 自定义一个注解，在每个方法前加注解配置（时间段、访问次数、是否需要登录）即可。
    再自定义一个拦截器，拦截对象是所有被调用的方法，判断方法是否包含注解@AccessLimit，若有则做相应处理。
    以上做法可以减少对业务代码的侵入，使限流方法变得通用化。
 ```

#### <font color=#6495ED face="黑体">24.如何从根本上解决超卖问题？（Redis预减库存）</font> 
```
 -- 一定程度上解决超卖问题，减少对Redis的访问
    内存标记：系统初始化，HashMap<GoodsId,isSoldOut> 默认标记商品未售罄false
            当某商品的秒杀库存为0时，在Map中标记该商品已售罄。
            每当服务端收到客户端的请求，先通过map判断商品是否售罄，未售罄再继续操作。
 -- 从根本上解决超卖问题
    1.系统初始化，服务端把秒杀商品的库存数量加载到Redis缓存中。
    2.服务端收到客户端的秒杀请求，Redis先预减库存，若库存不足就直接返回“秒杀失败”。
    由于Redis是单线程模型，因此在多线程的情况下通过Redis预减库存的方式就能从根本上解决超卖问题，亦可减少对数据库的访问。
 ```
 
#### <font color=#6495ED face="黑体">25.如何从根本上解决重复秒杀问题？（数据库唯一索引）</font> 
```
 -- 一定程度上解决重复秒杀问题，减少对数据库的访问
    当用户成功秒杀某商品时，将秒杀订单信息记录在Redis缓存中。
    服务端收到客户端的秒杀请求，首先要查询Redis缓存中的秒杀订单信息，判断是否重复秒杀。
    若是，则return false；否则继续后续操作。
 -- 从根本上重复秒杀问题
    在秒杀订单表的两个字段“商品id”、“用户id”加上唯一索引。
 ```
 
#### <font color=#6495ED face="黑体">26.本项目的库存模型总结？</font> 
```
 -- 本项目用到了3个库存值：
    1.Redis缓存的可秒杀库存；（防止超卖，减少对数据库的访问，成功下单就可以锁定商品）
    2.数据库中的商品总库存；
    3.数据库中的商品可秒杀库存。
 -- 本项目采用的业务逻辑：Redis预减库存->数据库减去秒杀库存和总库存->下单（写入普通订单、秒杀订单）。
 -- 本项目尽可能简化了库存模型，省略了订单支付、商品出库、商品入库等操作。
 
 -- 若考虑以上因素，那么就多了一个“预占库存”的概念，用于统计未出库的数量（已下单未支付、已支付未出库），业务逻辑如下：
    Redis预减库存->下单（写入普通订单、秒杀订单）->预占（预占库存+1）->支付->出库（释放预占库存，并减去实际库存）->完成
 ```
 
#### <font color=#6495ED face="黑体">27.本项目的秒杀具体流程？</font> 
```
 -- 1.系统初始化：服务端把商品库存数量加载到Redis缓存中，内存（HashMap<GoodsId,Boolean>）默认标记商品未售罄；
 -- 2.服务端收到客户端的请求，先通过map判断商品是否售罄；（一定程度上防止超卖，减少对Redis的访问）
 -- 3.Redis预减库存，若库存不足就直接返回“秒杀失败”；（从根本上防止超卖，减少对数据库的访问）
 -- 4.查询Redis缓存中是否存在相应的秒杀订单；（一定程度上防止重复秒杀，减少对数据库的访问）
 -- 5.秒杀请求入队，服务端响应客户端“排队中” ；（同步下单 -> 异步下单，改善用户体验）
 -- 6.秒杀请求出队，服务端判断数据库的库存是否大于0；（在第3步已经防止超卖，为了保险起见增加了这步）
 -- 7.查询Redis缓存中是否存在相应的秒杀订单；（一定程度上防止重复秒杀，减少对数据库的访问）
 -- 8.服务端操作数据库：减库存->下订单->写入秒杀订单 ，事务执行成功的话会把秒杀订单放到Redis缓存中。
 -- 9.若第8步的减库存失败，则会在Redis缓存中标记该商品已售罄，并return false。
 -- 10.客户端在收到响应“排队中”后，会轮询服务端的Redis缓存（判断是否存在秒杀订单/是否售罄），直到秒杀成功或失败。（排队中0 成功orderId 失败-1）
 ```
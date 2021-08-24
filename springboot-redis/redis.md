# Redis的关键字

高并发,KV存储,内存数据库,丰富的数据结构,单线程（6版本之前）


## 为什么出现缓存
一般情况下,数据都是在数据库中,应用系统直接操作数据库。当访问量上万,数据库压力增大,这个时候


>分库分表,读写分离。的确,这些确实是解决比较高的访问量的解决办法,但是,如果访问量更大,10万,100万呢？怎么分似乎都不解决问题吧,所以我们需要用到其他办法,来解决高并发带来的数据库压力。


缓存就是缓存数据到内存中一份,当访问的时候我们会访问内存的数据如果内存中的数据不存在,这个时候,我们再去
读取数据库,之后把数据库中的数据再备份一份到内存中,这样下次读请求过来的时候,还是会直接先从内存中访问,
访问到内存的数据了之后就直接返回了。


这样做就完美的降低了数据库的压力,可能十万个请求进来,全部都访问到了内存的备份数据,而没有去数据库访问数据,
或者只有少量几个请求访问到了数据库这样真的是大大降低了数据库的压力,而且这样做提高了系统响应。




## 什么是缓存？
缓存原指CPU上的一种高速存储器,它先于内存与CPU交换数据,速度很快。现在泛指存储在计算机上的原始数据的复制集,便于快速访问。


## Redis高效的核心要素
- 高速的存储介质  (存储介质随机访问延迟：机械硬盘--》10ms  固态硬盘--》100us  内存条--》150ns   单位换算：1000)
- 优良的底层数据结构  底层是HashMap 时间复杂度是O(1)
- 高效的网络IO模型  epoll  kqueue
- 高效的线程模型  IO多线程，执行单线程

## Redis分析
**redis底层是hashtable**


- 数组的下标索引计算：
hash(key) %hashtable.size=[0,hashtable.size-1]
 

- 产生hash冲突(采用链表法  next指针)
hash(key) %hashtable.size= 7
hash(key1) %hashtable.size= 7

指针next指向可以是：头插法和尾插法

>redis使用的是头插法,需要数据在下一次会被访问到。hash冲突越来越多的时候会进行成倍的扩容


## 缓存的读写模式

缓存有三种读写模式


### Cache Aside Pattern (常用)

Cache Aside Pattern (旁路缓存),是最经典的缓存+数据库读写模式

`读的时候,先读缓存,缓存没有的话,就读数据库,然后取出数据后放入缓存,同时返回响应。`

![](https://img2020.cnblogs.com/blog/1231979/202011/1231979-20201115221432339-1110952946.png)


更新的时候,先更新数据库,然后再删除缓存
![](https://img2020.cnblogs.com/blog/1231979/202011/1231979-20201115222447070-640301574.png)


#### 为什么是删除缓存,而不是更新缓存呢？

1.缓存的值是一个结构,hash,list,更新数据需要遍历

2.懒加载,使用的时候才更新缓存,也可以采用异步的方式填充缓存


#### 高并发脏读的三种情况
1.先更新数据库,在更新缓存  
    update与commit之间,更新缓存,commit失败,则DB与缓存数据不一致


2.先删除缓存,再更新数据库
    update与commit之间,有新的读,缓存空,读DB数据到缓存,数据是旧的数据
commit后DB为新的数据,则DB与缓存数据不一致


3.先更新数据库，再删除缓存（推荐）
    update与commit之间，有新的读，缓存空，读DB数据到缓存，数据是旧的数据
commit后DB为新的数据,则DB与缓存数据不一致`采用延时双删策略`。



### Read/Write Through Pattern
应用程序只操作缓存，缓存操作数据库
Read-Through（穿透读模式/直读模式）：应用程序读缓存，缓存没有，由缓存回源到数据库，并写入缓存
Write-Through（穿透写模式/直写模式）：应用程序写缓存，缓存写数据库。该种模式需要提供数据库的handler，开发较为复杂




### Write Behind Caching Pattern
应用程序只更新缓存

缓存通过异步的方式将数据批量或合并后更新到DB中

不能时时同步，甚至会丢数据




## Redis是什么
Redis是一个高性能的开源的，C语言的NoSQL(非关系型数据库)也叫做缓存数据库,数据保存在内存中,Redis是以key-value形式存储，和传统的关系型数据库不一样。不一定遵循传统数据库的那些基本要求。比如，不遵循SQL标准，事务，表结构等。
Redis有非常丰富的数据类型，比如String，list，set，zset，hash等



## Redis可以做什么
1.减轻数据库压力，提高并发量，提高系统响应时间

2.做Session分离
    传统的session是由自己的tomcat进行维护和管理的，再集群和分布式情况下，不同的tomcat要管理不同的session，只能在各个tomcat之间，通过网络和IO进行session复制极大影响了系统的性能
Redis解决了这一个问题，将登陆成功后的session信息，存放在Redis中，这样多个tomcat就可以共享Session信息了    

![](https://img2020.cnblogs.com/blog/1231979/202011/1231979-20201115225103914-216092337.png)

    
    
3.做分布式锁
一般Java中的锁都是多线程锁，是在一个进程中的，多个进程在并发的时候也会产生问题，也要控制时序性，这个时候Redis可以用来做分布式锁，使用Redis的setnx命令来实现


### Redis缺点
1.额外的硬件支出
缓存是一种软件系统中以空间换时间的技术，需要额外的磁盘空间和内存空间来存储数据


2.高并发缓存失效
在高并发的情况下，会出现缓存失效（缓存穿透，缓存雪崩，缓存击穿等问题）造成瞬间数据库访问量增大，甚至崩溃，所以这些问题是一定要去解决的


3.缓存与数据库数据同步
缓存与数据库无法做到数据的时时同步


4.缓存并发竞争
多个Redis客户端同时对一个key进行set值的时候由于执行顺序引起的并发的问题



## Redis的数据结构
Redis是一个key-value的存储系统，key的类型是字符串

Redis中常见的value的数据类型，有五种，string，list，hash，set，zset

key类型设计：
string->
   1.char[] data="legend\0"
   2.SDS：simple dynamic string
      key：data="sdfgdss\0fgdhshsjs"

redis的源码分析
   在server.h  --> redisDb.h 头文件里面
   dict字典


>相同的类型(type)  底层的编码(encoding)也会不相同
>同一个类型在内存中用不同的数据结构存储这种就是编码
 


### String字符串类型
String适合做单值缓存，对象缓存，分布式锁等

```
set             set key value           赋值

get             get key                 取值

getset          getset key value	    取值并赋值

setnx           setnx key value         当value不存在时采用赋值set key value NX PX 3000 原子操作，px 设置毫秒数

append          append key value        向尾部追加值

strlen          strlen key	            获取字符串长度

incr            incr key	            递增数字

incrby          incrby key increment	增加指定的整数

decr            decr key	            递减数字

decrby          decrby key decrement	减少指定的整数

mset            mset key value key value	批量赋值

mget            mget key key	        批量取值
```
![](https://img2020.cnblogs.com/blog/1231979/202011/1231979-20201117091554948-1592297842.png)

https://juejin.im/post/6893871526710525966?utm_source=gold_browser_extension
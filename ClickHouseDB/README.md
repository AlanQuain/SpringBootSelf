## ClickHouse-OLAP分析引擎

### 前置条件
explain 20.6版本之后才有


优化：
参数配置优化：cpu、内存、建议值

新特性：
    物化视图、20.8之后物化MySQL


监控及备份：
    普罗米修斯、Grafana
    
    
使用版本：
    ClickHouse：21.3.7.14
    
    
技术基础要求
    sql基础+linux基础+zookeeper基础    
    
    
### ClickHouse入门
ClickHouse 是俄罗斯的 Yandex于2016年开源的列式存储数据库(DBMS),使用C++语言编写,主要用于在线分析处理查询(OLAP),能够使用SQL查询实时生成分析

HBase也是列式存储

OLTP 连接事务处理 Mysql


下面的表为例
![](https://img2020.cnblogs.com/blog/1231979/202107/1231979-20210730080910474-758539115.png)

行式存储时,数据在磁盘上的组织结构
![](https://img2020.cnblogs.com/blog/1231979/202107/1231979-20210730080955458-1155032126.png)

列式存储时,数据在磁盘上的组织结构
![](https://img2020.cnblogs.com/blog/1231979/202107/1231979-20210730081059910-822029793.png)



#### 列式存储的好处
- 对于列的聚合,计数,求和等统计操作原因优于行式存储。

- 由于某一列的数据类型都是相同的,针对于数据存储更容易进行数据压缩,每一列
选择更优的数据压缩算法,大大提高了数据的压缩比重。

- 由于数据压缩比更好,一方面节省了磁盘空间,另一方面对于cache 也有了更大
的发挥空间。


#### DBMS功能
几乎覆盖了标准SQL的大部分语法,包括DDL和DML,以及配套的各种函数,用户管理及权限管理,数据的备份及恢复


#### 多样化引擎
ClickHouse和MySQL类似,把表级的存储引擎插件化,根据表的不同需求可以设定
不同的存储引擎。目前包括合并树、日志、接口和其他四大类20多种引擎。


#### 高吞吐读写能力
ClickHouse采用类LSM Tree的结构,数据写入后定期在后台Compaction。通过类
LSM tree 的结构,ClickHouse在数据导入时全部是顺序append写,写入后数据段不可
更改,在后台compaction时也是多个段merge sort后顺序写回磁盘。顺序写的特性,充
分利用了磁盘的吞吐能力,即便在HDD上也有着优异的写入性能。
官方公开benchmark测试显示能够达到50MB-200MB/s的写入吞吐能力,按照每
行100Byte估算,大约相当于50W-200W条/s的写入速度。


#### 数据分区与线程级并行
ClickHouse将数据划分为多个partition,每个partition再进一步划分为多个index granularity(索引粒度),然后通过多个CPU核心分别处理其中的一部分来实现并行数据处
理。在这种设计下,单条Query就能利用整机所有CPU。极致的并行处理能力,极大的降
低了查询延时。
所以,ClickHouse即使对于大量数据的查询也能够化整为零平行处理。但是有一个弊
端就是对于单条查询使用多cpu,就不利于同时并发多条查询。所以对于高qps的查询业
务,ClickHouse并不是强项。


### ClickHouse安装

#### 确定防火墙处于关闭状态
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801143403941-1246139949.png)


#### CentOS取消打开文件数限制
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801131223117-1688049062.png)

>es也调过这个参数

在Centos7中的 `/etc/security/limits.conf` 文件的末尾加入以下内容

sudo vim /etc/security/limits.conf
```
* soft nofile 65536
* hard nofile 65536
* soft nproc 131072
* hard nproc 131072
```

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801143834994-1418066682.png)


参数分析：
第一列  * ：显示的用户和 用户组  单独对某些账号：user@usergroup
第二列  hard/soft ：当前生效的,soft：软状态 小于等于 hard 硬状态
第三列  nofile/nproc： 文件数、用户进程数
第四列：数量


注意：在 /etc/security/limits.d/20-nproc.conf文件的末尾加入以下内容(可以覆盖上面的内容)

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801144414687-736183403.png)


sudo vim /etc/security/limits.d/20-nproc.conf
```
* soft nofile 65536
* hard nofile 65536
* soft nproc 131072
* hard nproc 131072
```

修改前：
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801144449039-476252261.png)

修改后：
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801144510404-819507248.png)


重新登录即可生效配置
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801144653391-847537836.png)


#### 安装依赖
```
sudo yum install -y libtool

sudo yum install -y *unixODBC*

```
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801145019382-1582903947.png)
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801145052889-1584399909.png)


#### Centoa取消SELINUX (S:Security E:Enforce)  
修改/etc/selinux/config 中的SELINUX=disabled

$ sudo vim /etc/selinux/config
```
SELINUX=disabled
```

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801145402097-713995347.png)


内核级别配置改完之后需要重启服务器,还有另外一种方案可以临时生效 setenforce 0
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801145633347-261528823.png)


#### ClickHouse版本选择安装(clickhouse-21.7.3.14)
[ClickHouse官网](https://clickhouse.tech/docs/zh/)
[Docker-ClickHouse官网](https://hub.docker.com/r/yandex/clickhouse-client/tags?page=1&ordering=last_updated)



20.5 final多线程
20.6.3  支持explain执行计划
20.8 同步MySQL




安装文件：sudo rpm -ivh *.rpm
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801150843715-1804984571.png)


二进制文件在 bin ===》/usr/bin
配置文件在 conf===》  /etc/clickhouse-server/
lib文件在 lib===》  /var/lib/clickhouse/
日志文件在  /var/log/clickhouse


安装执行日志记录
```
[root@localhost clickhouse]# sudo rpm -ivh *.rpm
warning: clickhouse-client-21.7.3.14-2.noarch.rpm: Header V4 RSA/SHA1 Signature, key ID e0c56bd4: NOKEY
Preparing...                          ################################# [100%]
Updating / installing...
   1:clickhouse-common-static-21.7.3.1################################# [ 25%]   2:clickhouse-client-21.7.3.14-2    ################################# [ 50%]
   3:clickhouse-server-21.7.3.14-2    ################################# [ 75%]
ClickHouse binary is already located at /usr/bin/clickhouse
Symlink /usr/bin/clickhouse-server already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-server to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-client already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-client to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-local already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-local to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-benchmark already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-benchmark to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-copier already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-copier to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-obfuscator already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-obfuscator to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-git-import to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-compressor already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-compressor to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-format already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-format to /usr/bin/clickhouse.
Symlink /usr/bin/clickhouse-extract-from-config already exists but it points to /clickhouse. Will replace the old symlink to /usr/bin/clickhouse.
Creating symlink /usr/bin/clickhouse-extract-from-config to /usr/bin/clickhouse.
Creating clickhouse group if it does not exist.
 groupadd -r clickhouse
Creating clickhouse user if it does not exist.
 useradd -r --shell /bin/false --home-dir /nonexistent -g clickhouse clickhouse
Will set ulimits for clickhouse user in /etc/security/limits.d/clickhouse.conf.
Creating config directory /etc/clickhouse-server/config.d that is used for tweaks of main server configuration.
Creating config directory /etc/clickhouse-server/users.d that is used for tweaks of users configuration.
Config file /etc/clickhouse-server/config.xml already exists, will keep it and extract path info from it.
/etc/clickhouse-server/config.xml has /var/lib/clickhouse/ as data path.
/etc/clickhouse-server/config.xml has /var/log/clickhouse-server/ as log path.
Users config file /etc/clickhouse-server/users.xml already exists, will keep it and extract users info from it.
 chown --recursive clickhouse:clickhouse '/etc/clickhouse-server'
Creating log directory /var/log/clickhouse-server/.
Creating data directory /var/lib/clickhouse/.
Creating pid directory /var/run/clickhouse-server.
 chown --recursive clickhouse:clickhouse '/var/log/clickhouse-server/'
 chown --recursive clickhouse:clickhouse '/var/run/clickhouse-server'
 chown clickhouse:clickhouse '/var/lib/clickhouse/'
 groupadd -r clickhouse-bridge
 useradd -r --shell /bin/false --home-dir /nonexistent -g clickhouse-bridge clickhouse-bridge
 chown --recursive clickhouse-bridge:clickhouse-bridge '/usr/bin/clickhouse-odbc-bridge'
 chown --recursive clickhouse-bridge:clickhouse-bridge '/usr/bin/clickhouse-library-bridge'
Enter password for default user: 
Password for default user is empty string. See /etc/clickhouse-server/users.xml and /etc/clickhouse-server/users.d to change it.
Setting capabilities for clickhouse binary. This is optional.

ClickHouse has been successfully installed.

Start clickhouse-server with:
 sudo clickhouse start

Start clickhouse-client with:
 clickhouse-client

Created symlink from /etc/systemd/system/multi-user.target.wants/clickhouse-server.service to /etc/systemd/system/clickhouse-server.service.
   4:clickhouse-common-static-dbg-21.7################################# [100%]
[root@localhost clickhouse]# 

```


启动clickhouse
systemctl status clickhouse-server
or
sudo clickhouse start

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801152100606-252892615.png)


交互式的命令行
clickhouse-client -m
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801152346241-289950032.png)

show databases;


这个是自动
clickhouse-client --query "show databases;"

docker 安装clickhouse-server+clickhouse-client+Tabix
https://blog.csdn.net/m0_37813354/article/details/109526076


关闭开机自启(生产环境不要关,自己测的可以)
systemctl disable clickhouse-server


#### Docker版本安装
docker cp temp-clickhouse-server:/etc/clickhouse-server/config.xml /vagrant/clickhouse/conf/config.xml
docker cp temp-clickhouse-server:/etc/clickhouse-server/users.xml /vagrant/users.xml
D:\software\clickhouse\conf


PASSWORD=$(base64 < /dev/urandom | head -c8); echo "root"; echo -n "root" | sha256sum | tr -d '-'

root
4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2


<users>
	<root>			
		<password_sha256_hex>4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2</password_sha256_hex>
       	<networks incl="networks" replace="replace">
           <ip>::/0</ip>
        </networks>
        <profile>root</profile>
        <quota>root</quota>
    </root>
</users>


docker run -d --name=single-clickhouse-server -p 8123:8123 -p 9000:9000 -p 9009:9009 --ulimit nofile=262144:262144 --volume D:/clickhouse/data:/var/lib/clickhouse:rw --volume D:/clickhouse/conf:/etc/clickhouse-server:rw --volume D:/clickhouse/log:/var/log/clickhouse-server:rw yandex/clickhouse-server




### 数据类型

位数

#### 整型
固定长度的整型,包括有符号整型或无符号整型。
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801152842151-768480695.png)


#### 浮点型
float32--float
float64--double


#### 布尔类型

Uint8   取值：0 或1


#### decimal
有符号的浮点数,可在加、减和乘法运算过程中保持精度。对于除法,最低有效数字会被丢弃(不舍入)

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801153221285-765252841.png)

使用场景: - 般金额字段、汇率、利率等字段为了保证小数点精度,都使用Decimal进行
存储。


#### 字符串String
字符串可以任意长度的。它可以包含任意的字节集,包含空字节


FixString(5)
使用场景:名称、文字描述、字符型编码。固定长度的可以保存一 些定长的内容, 比如一
些编码,性别等但是考虑到- -定的变化风险,带来收益不够明显,所以定长字符串使用意义
有限。


#### 枚举类型
- 建表
```
CREATE TABLE t_enum
(
    x Enum8('hello' = 1, 'world' = 2)
)
ENGINE = TinyLog;
```
这个x列只能存储类型定义中列出的值：'hello' 或 'world'

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801153902718-1043514809.png)


- INSERT INTO t_enum VALUES ('hello'), ('world'), ('hello');

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801153942674-703021994.png)


- INSERT INTO t_enum VALUES ('hello22');

![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801154021047-533531149.png)


- select * from t_enum;
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801154113600-712689137.png)


- SELECT CAST(x, 'Int8') FROM t_enum;
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801154216201-1586761827.png)


#### 时间类型
目前ClickHouse有三种时间类型
- Date 接受年-月-日的字符串比如 '2019-12-16'
- Datetime 接受年-月-日 时:分:秒的字符串比如 '2019-12-16 20:50:10'
- Datetime64 接受年-月-日时:分:秒.亚秒的字符串比如'2019-12-16 20:50:10.66'
日期类型,用两个字节存储,表示从1970-01-01 (无符号)到当前的日期值。4


#### 数组
Array(T):由T类型元素组成的数组。
T可以是任意类型,包含数组类型。但不推 荐使用多维数组,ClickHouse 对多维数
组的支持有限。例如,不能在MergeTree表中存储多维数组。
➢创建数组方式 1,使用array函数


SELECT array(1, 2) AS x, toTypeName(x) ;


具体可参考：https://clickhouse.tech/docs/en/sql-reference/data-types/int-uint/



### 表引擎
#### 表引擎的使用

表引擎是ClickHouse的一大特色。可以说,表引擎决定 了如何存储表的数据。包括:。
- 数据的存储方式和位置,写到哪里以及从哪里读取数据。。
- 支持哪些查询以及如何支持
- 并发数据访问。
- 索引的使用(如果存在)
- 是否可以执行多线程请求
- 数据复制参数。

>引擎的名称大小写敏感



#### TinyLog
以列文件的形式保存在磁盘上,不支持索引,没有并发控制。一般保存少量数据的小表,

>生产环境上作用有限。可以用于平时练习测试用。


#### Memory
内存引擎,数据以未压缩的原始形式直接保存在内存当中,服务器重启数据就会消失。
读写操作不会相互阻塞,不支持索引。简单查询下有非常非常高的性能表现(超过10G/s)。
一般用到它的地方不多, 除了用来测试,就是在需要非常高的性能,同时数据量又不太


#### MergeTree
ClickHouse中最强大的表引擎当属MergeTree(合并树)引擎及该系列(*MergeTree)
中的其他引擎,支持索引和分区,地位可以相当于innodb之于Mysql。而且基于
MergeTree,还衍生除了很多小弟,也是非常有特色的引擎。。

```sql
-- 建表
create table t_order_mt(
id UInt32,
sku_id String,
total_amount Decimal(16,2),
create_time Datetime
) engine = MergeTree
partition by toYYYYMMDD(create_time)
primary key (id)
order by (id,sku_id);


-- 插入数据
insert into t_order_mt values
(101,'sku_001',1000.00,'2021-06-01 12:00:00') ,
(102,'sku_002',2000.00,'2021-06-01 11:00:00'),
(102,'sku_004',2500.00,'2021-06-01 12:00:00'),
(102,'sku_002',2000.00,'2021-06-01 13:00:00'),
(102,'sku_002',12000.00,'2021-06-01 13:00:00'),
(102,'sku_002',600.00,'2021-06-02 12:00:00');


-- 通常是按照时间来分区
partition by toYYYYMMDD(create_time)
-- 主键(默认不唯一 特别注意)
primary key (id)
-- 分区内排序
order by (id,sku id);

```


![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801162100900-1149599672.png)
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801162143951-936656441.png)
![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801162230257-1768643507.png)


MergeTree实还有很多参数(绝大多数用默认值即可),但是三个参数是更加重要的,
也涉及了关于MergeTree的很多概念。



#### partition by分区(可选)

- 作用
分区的目的主要是降低扫描的范围,优化查询速度

- 如果不填
只会使用一个分区(all分区)

- 分区目录
MergeTree是以列文件+索引文件+表定义文件组成的,但是如果设定了分区那么这
些文件就会保存到不同的分区目录中。。

- 并行
分区后,面对涉及跨分区的查询统计, ClickHouse 会以分区为单位并行处理。。

- 数据写入与分区合并。
任何一个批次的数据写入都会产生一个临时分区, 不会纳入任何一个已有的分区。写入
后的某个时刻(大概10-15分钟后), ClickHouse 会自动执行合并操作(等不及也可
以手动通过optimize执行) , 把临时分区的数据,合并到已有分区中。
optimize table xxxx final;


![](https://img2020.cnblogs.com/blog/1231979/202108/1231979-20210801161223358-660358222.png)

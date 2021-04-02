# mall

## 介绍
springboot商城项目学习

## 技术选型
本项目的后台管理页面是使用vue搭建的单体页面，前台页面使用springboot的thymeleaf，
只有后台管理的系统是使用前后端分离。

### 主要后端技术栈
|技术|说明|
| ---- | ---- |
|Spring Boot|核心框架|
|MybatisPlus|持久层框架|
|Mysql|持久层数据库|
|Druid|数据库连接池|
|Nacos|注册发现中心|
|SpringCloud Getaway|分布式网关|
|SpringCloud Openfeign|远程调用|
|Thymeleaf|模板引擎|
|Redis|缓存|
|ElasticSearch|商城业务的搜索框架|
|SpringSession|单点登录|
|OAuth2.0|社交登录|
|SpringCache|缓存框架|
|RabbitMQ|消息队列|
|Nginx|动静分离负载均衡|
|Alipay-sdk|支付宝沙箱支付|
|Zipkin|链路追踪|

### 前端技术栈
|技术|说明|
| ---- | ---- |
|Vue|核心MVVM框架|
|Vue-Router|前端路由|
|Vuex|组件通信|
|Element-ui|UI组件|
|Axios|异步通讯|

## 安装教程

本项目的开发是全程在一台机器机跑的，所以下面的nginx、redis、es、mysql、rabbitmq等都是通过docker安装在linux虚拟机上面

#### 1.配置Nginx

1.  在 nginx.conf的http里面加上
    
    下面的地址为你的getaway地址
    
    ```
    upstream mall{
    server 192.168.1.105:88;
    }
    ```
    
2. 配置转发规则，在conf.d里面创建 freedymall.conf,并加上下面配置
   ```
   server {
    listen       80;
    server_name  freedymall.com *.freedymall.com;
   
    location /static/ {
      root /usr/share/nginx/html;
    }
   
    location / {
      proxy_set_header Host $Host;
      proxy_pass http://mall;
    }
   }
   ```

3. 配置静态资源
  将上面resource里面的static文件夹复制到，nginx目录下的html里面，

#### 2.配置host

因为前台页面都是通过nginx和getaway的域名转发所以需要配置本机host

前面的地址为你的nginx的地址

```
192.168.56.10 freedymall.com
192.168.56.10 search.freedymall.com
192.168.56.10 item.freedymall.com
192.168.56.10 auth.freedymall.com
192.168.56.10 cart.freedymall.com
192.168.56.10 order.freedymall.com
192.168.56.10 member.freedymall.com
192.168.56.10 seckill.freedymall.com
```

#### 3.配置Nacos
打开上面resource里面的nacos，进入bin打开命令行输入
``startup -m standalone``启动nacos服务器

#### 4.配置Mysql
打开上面resource里面的sql，分别执行各个sql文件创建数据库，
每个微服务对应的数据库名分别如下

|模块名|数据库名|
| ---- | ----  |
|coupon|mall_sms|
|member|mall_ums|
|order|mall_oms|
|product|mall_pms|
|ware|mall_wms|
|renren-fast|mall_admin|

#### 5.配置redis
在每个模块的配置文件中加上你自己redis的地址和密码等信息

#### 6.配置ElasticSearch
在kibana中创建以下的索引映射

```
put /product
{
    "mappings":{
        "properties": {
            "skuId":{
                "type": "long"
            },
            "spuId":{
                "type": "keyword"
            },
            "skuTitle": {
                "type": "text",
                "analyzer": "ik_smart"
            },
            "skuPrice": {
                "type": "double"
            },
            "skuImg":{
                "type": "keyword"
            },
            "saleCount":{
                "type":"long"
            },
            "hasStock": {
                "type": "boolean"
            },
            "hotScore": {
                "type": "long"
            },
            "brandId": {
                "type": "long"
            },
            "catalogId": {
                "type": "long"
            },
            "brandName": {
                "type": "keyword"
            },
            "brandImg":{
                "type": "keyword"
            },
            "catalogName": {
                "type": "keyword"
            },
            "attrs": {
                "type": "nested",
                "properties": {
                    "attrId": {
                        "type": "long"
                    },
                    "attrName": {
                        "type": "keyword"
                    },
                    "attrValue": {
                        "type": "keyword"
                    }
                }
            }
        }
    }
}
```
#### 7. 配置消息队列
在order、seckill、ware模块里面的配置文件中配置

```properties
spring.rabbitmq.addresses=你的rabbitmq服务器地址
spring.rabbitmq.port=5672
spring.rabbitmq.virtual-host=/
```

#### 8.配置阿里云oss对象存储（可选）
不配置将不能使用图片上传服务
在third——party模块的配置文件中配置

```yaml
 alicloud:
      access-key: 你自己的access-key
      secret-key: 你自己的 secret-key
      oss:
        endpoint:  你自己的endpoint
        bucket: 你自己的bucket
```
#### 9.配置邮件发送服务（可选）
不配置将不能使用邮箱进行账号注册功能
在third——party模块的配置文件中配置

```yaml
freedymall:
  email:
    hostName: 你的邮箱SMTP服务器地址
    from: 你的电子邮箱
    authentication: 你的授权码
```
上面的信息可以在你的邮箱里面找到
下面以qq邮箱为例展示如何获取上面信息
1. 打开QQ邮箱(地址:https://mail.qq.com/)；
2. 登录后点击设置
3. 点击账户，下拉找到POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务
4. 打开Pop3/SMPT服务 ，获取邮箱授权码(需要手机验证)
5. 开启成功后会获得一个授权码.
6. qq的smtp服务器地址一般为smtp.qq.com

#### 10.启动所有项目

访问http://freedymall.com可以打开前台页面

访问http://localhost:8080可以打开后台界面


## 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

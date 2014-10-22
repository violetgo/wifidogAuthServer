wifidogAuthServer
=================

###简介
---
wifidog 的 authserver 的 java 实现;可以用于验证用户上网,简单的作为广告路由的认证端;

目前仅支持验证用户登录,注册,记录用户上网时长,登录时推送广告页;

在登录成功后会返回portal/mac.html;修改portal/mac.html 可以做简单的广告宣传;

如果不存在网关 mac 则返回默认页面;


###文件说明
---
* create.sql 数据库初始化,数据库使用 mysql;
* conf/log4j.xml 日志配置;
* conf/mybatis-config.xml mysql连接配置;
* server.sh:启动脚本;支持 start stop restart;
* src/ 源码目录,由于程序比较简单;注释较少;
* web/ 静态页面的源码目录;

###使用
---
	mvn clean package
	mv target/authServer-1.0.0-releases.jar ./authServer.jar
	chmod -R +x *
	./server.sh start
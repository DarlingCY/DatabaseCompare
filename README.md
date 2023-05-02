# DatabaseCompare —— 数据库比对工具

## 目前支持

- MySQL

## 使用教程

本项目基于JDK 8，使用前请先配置对于环境

1. 前往[releases](https://github.com/DarlingCY/DatabaseCompare/releases)页面下载最新包并解压到任意文件夹
2. 编辑application.yml文件，如下示例所示
    ```yml
    #newDataBase 新版本数据库
    newDatabase:
      url: jdbc:mysql://127.0.0.1:3306 #修改为自己的数据库连接地址
      user: root #账户
      pass: 123456 #密码
    #oldDataBase 旧版本数据库
    oldDatabase:
      url: jdbc:mysql://127.0.0.1:3306 #修改为自己的数据库连接地址
      user: root #账户
      pass: 123456 #密码
    ```
3. 在当前解压文件夹中执行如下命令(xxx请替换为jar包名称)
   ```shell
   java -jar xxx.jar --server.port=8120 --spring.config.location=application.yml
   ```
4. 启动成功后访问 http://127.0.0.1:8120 即可查看及操作对比结果
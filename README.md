# DatabaseCompare —— 数据库比对工具

该工具可对比两数据库的数据库变更、数据表变更、表字段变更，并提供可视化界面查看。

## 目前支持

- MySQL

## 使用教程

本项目基于JDK 8，使用前请先配置对于环境

1. 前往[releases](https://github.com/DarlingCY/DatabaseCompare/releases)页面下载最新包并解压到任意文件夹
2. 编辑application.yml文件，如下示例所示
    ```yml
   #源数据库
   sourceDatabase:
      host: #数据库HOST
      port: #数据库PORT
      user: #数据库USER
      password: #数据库PASSWORD
      useSshTunnel: #是否使用SSH隧道
      sshHost: #SSH隧道HOST
      sshPort: #SSH隧道PORT
      sshUser: #SSH隧道USER
      sshPassword: #SSH隧道PASSWORD
   #目标数据库
   targetDatabase:
      host: #数据库HOST
      port: #数据库PORT
      user: #数据库USER
      password: #数据库PASSWORD
      useSshTunnel: #是否使用SSH隧道
      sshHost: #SSH隧道HOST
      sshPort: #SSH隧道PORT
      sshUser: #SSH隧道USER
      sshPassword: #SSH隧道PASSWORD
    ```
3. 在当前解压文件夹中执行如下命令
   ```shell
   java -jar DatabaseCompare.jar
   ```
4. 启动成功后访问 http://127.0.0.1:8888 即可查看及操作对比结果
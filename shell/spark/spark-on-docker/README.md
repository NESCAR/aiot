# Docker安装Spark和部署应用

## 1. 安装Spark集群
（1）创建文件夹，比如spark

（2）下载配置文件并部署

```bash
./spark-on-docker.sh
```

spark的worker数量，以及worker内存的分配，都可以通过修改docker-compose.yml文件来调整

## 2. 部署应用

（1）将Jar包放在jars文件夹下

jar包编写和编译：https://blog.csdn.net/boling_cavalry/article/details/86776746

（2）运行举例

1. 拷贝文件

```bash
# 将txt文件拷贝到hdfs
# docker-compose.yml 已经将宿主机的input_files目录挂载到namenode容器上了
# 以下指令将容器内的文件上传到hdfs中
## 创建/input文件夹；将文件HappyJack.txt拷贝到hdfs的/input文件夹
docker exec namenode hdfs dfs -mkdir /input \
&& docker exec namenode hdfs dfs -put /input_files/HappyJack.txt /input
```

执行完成后，即可从hdfs中查看HappyJack.txt

2. 运行

```bash
# 注意：/root/jars/sparkwordcount-1.0-SNAPSHOT.jar 不需要更改为实际绝对路径
## sparkwordcount-1.0-SNAPSHOT的三个参数：namenode表示hdfsHost；
##   8020：hdfsPort（具体端口在hadoop.env）；HappyJack.txt：要处理的文件对象
##   url：String hdfsBasePath = "hdfs://" + hdfsHost + ":" + hdfsPort;
sudo docker exec -it master spark-submit \
--class com.bolingcavalry.sparkwordcount.WordCount \
--executor-memory 512m \
--total-executor-cores 2 \
/root/jars/sparkwordcount-1.0-SNAPSHOT.jar \
namenode \
8020 \
HappyJack.txt
```

3. 查看结果

在hdfs的`/output`文件夹中包含了结果的文件

[!结果](./img/output.png)

```bash
docker exec namenode hdfs dfs -cat /output/20200705121744/part-00000
```

## 3. 参考

Spark集群简单搭建：https://blog.csdn.net/boling_cavalry/article/details/86851069

性能优化：https://blog.csdn.net/boling_cavalry/article/details/87438666

WordCount应用：https://blog.csdn.net/boling_cavalry/article/details/86776746

## 4. 可视化

HDFS(共3个datanode) : aliyun:50070

Spark : aliyun:8080
# IMAGES REPOSITORY   
# DESCRIPTION         安装docker-demo相关环境
# VERSION             0.0.1

# 指定基础镜像
FROM registry.cn-hangzhou.aliyuncs.com/luliangwei/alpine:java8
#FROM registry.cn-hangzhou.aliyuncs.com/luliangwei/ubuntu:overlay-java8

# 指定镜像创建者和联系方式
MAINTAINER luliangwei luliangwei1874@163.com

# 环境变量定义
ENV SERVER_SERVICE              docker-demo
ENV SERVER_VERSION              0.0.1-SNAPSHOT
ENV SERVER_PROFILE              dev
ENV SERVER_REPOS                public-snapshots
ENV SERVER_CONTEXT              /
ENV SERVER_TYPE                 jar
ENV SERVER_ROOT_HOME            /usr/local/bin/${SERVER_SERVICE}
#ENV SERVER_BIN                  ${SERVER_ROOT_HOME}/${SERVER_SERVICE}.${SERVER_TYPE}
ENV SERVER_BIN                  ${SERVER_ROOT_HOME}/${SERVER_SERVICE}-${SERVER_VERSION}.${SERVER_TYPE}
ENV SERVER_DEFAULT_PORT         1874
ENV SERVER_DEFAULT_SSL_PORT     41874
ENV COMPOSE_CONVERT_WINDOWS_PATHS 1

# 添加独立的外部文件,s6-overlay提供的一个运行的文件夹下
ADD ./run /etc/services.d/${SERVER_SERVICE}/

RUN mkdir -p ${SERVER_ROOT_HOME}

# 将jar包复制到容器内（这种做法不好），实际生产中应该从nexus库中取
ADD ./docker-demo-0.0.1-SNAPSHOT.jar ${SERVER_ROOT_HOME}

# 给run文件执行权限
RUN chmod +x /etc/services.d/${SERVER_SERVICE}/run

# 从远端添加相应的jar包到容器内
# RUN mkdir -p ${SERVER_ROOT_HOME} && \
#    cd ${SERVER_ROOT_HOME} && \
#	wget -c 'http://mvn.ns.me/nexus/service/local/artifact/maven/content?r='${SERVER_REPOS}'&g=cn.signit&a='${SERVER_SERVICE}'&v='${SERVER_VERSION}'&e='${SERVER_TYPE} -O ${SERVER_BIN}

# 执行run脚本，运行jar包,基于alpine的镜像不用这行命令，因为已经写了启动脚本
# ENTRYPOINT sh /etc/services.d/${SERVER_SERVICE}/run

#需要暴露的端口
EXPOSE ${SERVER_DEFAULT_PORT}

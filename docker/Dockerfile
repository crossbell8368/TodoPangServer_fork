# Base: Linux-Ubuntu
FROM ubuntu:22.04

# Package: JDK, SSH
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-11-jdk \
    sudo && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# config_1: account
RUN groupadd -r appgroup && \
    useradd -r -g appgroup todopang && \
    usermod -aG sudo todopang

# config_2: password
ARG ROOT_PASSWORD
ARG SERVICE_PASSWORD
RUN echo "root:${ROOT_PASSWORD}" | chpasswd && \
    echo "todopang:${SERVICE_PASSWORD}" | chpasswd

# config_3: dir
WORKDIR /mnt/workspace
VOLUME ["/mnt/workspace"]
RUN chown -R todopang:appgroup /mnt/workspace

# config_4: port
EXPOSE 8080


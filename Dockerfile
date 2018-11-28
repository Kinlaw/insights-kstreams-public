FROM openjdk:8
MAINTAINER Chris Kyrouac <ckyrouac@redhat.com>
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN mkdir -p /opt/maven && \
    cd /opt/maven && \
    curl -fsSL http://apache.osuosl.org/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz \
    | tar -xzC /opt/maven --strip-components=1 && \
    ln -s /opt/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /opt/maven

RUN cd /usr/src/myapp && \
    mvn clean package

CMD ["mvn", "exec:java", "-Dexec.mainClass=myapps.Pipe"]


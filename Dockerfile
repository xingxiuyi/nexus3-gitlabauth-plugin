FROM maven:3.5.2 as builder
MAINTAINER cbuchart@auchan.fr
COPY . /build
WORKDIR /build
RUN mvn clean package

FROM sonatype/nexus3:3.10.0
USER root
RUN mkdir -p /opt/sonatype/nexus/system/fr/auchan/nexus3-gitlabauth-plugin/1.1.1/
COPY --from=builder /build/target/nexus3-gitlabauth-plugin-1.1.1.jar /opt/sonatype/nexus/system/fr/auchan/nexus3-gitlabauth-plugin/1.1.1/
RUN echo "mvn\:fr.auchan/nexus3-gitlabauth-plugin/1.1.1 = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties

USER nexus
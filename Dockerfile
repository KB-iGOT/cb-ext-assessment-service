FROM eclipse-temurin:17-jdk-jammy

RUN useradd -ms /bin/bash appuser

# Install necessary dependencies
RUN apt-get update && \
    apt-get install -y \
        curl \
        libxrender1 \
        libjpeg-turbo8 \
        fontconfig \
        libxtst6 \
        xfonts-75dpi \
        xfonts-base \
        xz-utils && \
    rm -rf /var/lib/apt/lists/*

RUN curl "https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6-1/wkhtmltox_0.12.6-1.buster_amd64.deb" -L -o "wkhtmltopdf.deb"
RUN dpkg -i wkhtmltopdf.deb

COPY cb-ext-assessment-service-0.0.1-SNAPSHOT.jar /opt/

RUN chown -R appuser:appuser /opt
USER appuser
WORKDIR /opt


CMD ["/bin/bash", "-c", "java -XX:+PrintFlagsFinal $JAVA_OPTIONS -XX:+UnlockExperimentalVMOptions -jar /opt/cb-ext-assessment-service-0.0.1-SNAPSHOT.jar"]


FROM eclipse-temurin:17-jdk-jammy

RUN useradd -ms /bin/bash appuser

# Install dependencies
RUN apt-get update && \
    apt-get install -y \
        curl \
        fontconfig \
        libxrender1 \
        libjpeg-turbo8 \
        libx11-6 \
        libxext6 \
        libxtst6 \
        xfonts-75dpi \
        xfonts-base \
        fonts-liberation2 && \
    rm -rf /var/lib/apt/lists/*

# Install wkhtmltopdf 0.12.6.1
RUN curl -fL \
    "https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6.1-3/wkhtmltox_0.12.6.1-3.jammy_amd64.deb" \
    -o /tmp/wkhtmltopdf.deb && \
    dpkg -i /tmp/wkhtmltopdf.deb && \
    apt-get update && \
    apt-get install -f -y && \
    rm -f /tmp/wkhtmltopdf.deb && \
    rm -rf /var/lib/apt/lists/*

# Verify installation
RUN wkhtmltopdf --version

COPY cb-ext-assessment-service-0.0.1-SNAPSHOT.jar /opt/

RUN chown -R appuser:appuser /opt

USER appuser
WORKDIR /opt

CMD ["/bin/bash", "-c", "java -XX:+PrintFlagsFinal $JAVA_OPTIONS -XX:+UnlockExperimentalVMOptions -jar /opt/cb-ext-assessment-service-0.0.1-SNAPSHOT.jar"]

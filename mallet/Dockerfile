FROM eclipse-temurin:17-jdk

# Install dependencies (solo quelli non già presenti nell'immagine Temurin)
RUN apt-get update && apt-get install -y \
    ant \
    git \
    wget \
    unzip \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Create working directory
WORKDIR /opt

# Clone Mallet repo
RUN git clone https://github.com/mimno/Mallet.git mallet

WORKDIR /opt/mallet

# Build Mallet
RUN ant && ant jar

# Add Mallet bin directory to PATH
ENV PATH="/opt/mallet/bin:${PATH}"

# Create volume for data to process
RUN mkdir /data
VOLUME ["/data"]

# Default command: open shell
CMD ["/bin/bash"]

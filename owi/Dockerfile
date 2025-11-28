#OWI CONTAINER

FROM python:3.11-slim

# Install only what you need; `bash` is optional
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

# Install the required Python packages
RUN pip install py4lexis --index-url https://opencode.it4i.eu/api/v4/projects/107/packages/pypi/simple && \
    pip install owilix --index-url https://opencode.it4i.eu/api/v4/projects/92/packages/pypi/simple && \
    pip list
    
# Create dataset directory
RUN mkdir -p /app/dataset

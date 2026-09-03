# overall docker_security

Docker Security of A12 Vanilla
Docker Images
Introduction
At A12, we prioritize the security and efficiency of our backend services. Our Docker images are
designed to be secure by default, adhering to best practices to protect and optimize your
applications.
Key Points:
• Scope: These guidelines apply to our backend services only. Currently, Docker images are
available exclusively for these components.
• Purpose: Our goal is to ensure our Docker images are secure and efficient, minimizing potential
vulnerabilities and maintaining high performance.
Security Best Practices
Trusted Base Images
Base Image Selection: For all backend components, we use the eclipse-temurin:17-jre-jammy
image. This image is selected due to its reputable source, reliability, and compatibility with the Java
runtime environment, which is crucial for our backend services.
# Example of using a trusted base image
FROM eclipse-temurin:17-jre-jammy
Avoid Unnecessary Packages
Our Docker images include only essential packages, reducing the risk of security vulnerabilities and
keeping the images lightweight and fast.
# Example of minimal package installation
RUN apt-get update && apt-get install -y \
git \
curl \
--no-install-recommends
Secure Handling of Secrets
We do not hardcode secrets in our Dockerfiles. Instead, we utilize secure methods to manage
1

-- 1 of 4 --

secrets, such as injecting them at runtime or using environment variables securely.
# Example of using environment variables for secrets
ENV DATABASE_URL=""
Use Multi-stage Builds
To reduce the size of the final image, we use multi-stage builds. This practice ensures that only the
necessary components are included in the final image, enhancing security and efficiency.
# Example of a multi-stage build
FROM maven:3.8.5-jdk-11 AS build
WORKDIR /app
COPY . .
RUN mvn clean install
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /app/target/backend.jar /app/backend.jar
CMD ["java", "-jar", "/app/backend.jar"]
Optimize Build Cache Usage
We design our Dockerfiles to leverage the build cache effectively, reducing rebuild times and
improving build efficiency. We are cautious with cache invalidation triggers, especially with the
ADD and COPY commands.
# Example of efficient cache usage
COPY . /app
RUN cd /app && make
Minimize Image Layers
We minimize the number of layers in our Docker images to keep them lightweight and efficient.
Combining multiple RUN instructions into a single line when appropriate helps achieve this goal.
# Example of minimizing image layers
RUN apt-get update && apt-get install -y \
git \
curl \
&& apt-get clean
2

-- 2 of 4 --

Sort Multi-line Arguments
We alphabetically sort multi-line arguments to facilitate smoother updates and circumvent package
duplication through consistent sorting practices.
# Example of sorted multi-line arguments
RUN apt-get update && apt-get install -y \
curl \
git \
jq \
vim
Avoid Direct Fetching from the Internet
We avoid fetching files directly from the internet in our Dockerfiles. Instead, we download and
validate content separately to ensure security.
# Securely fetching and validating a file
RUN curl -o /tmp/somefile.tar.gz -L https://example.com/somefile.tar.gz \
&& echo "checksum /tmp/somefile.tar.gz" | sha256sum -c - \
&& tar -xzf /tmp/somefile.tar.gz -C /app
Secure Handling of Files (ADD or COPY)
We prefer using COPY over ADD to avoid the unexpected behaviors associated with ADD, such as
unpacking tar files or handling remote URLs. COPY is more predictable and secure.
# Example of using COPY
COPY ./local-file.txt /app/local-file.txt
Avoid sudo Usage
We do not use sudo within our Docker containers to limit the escalation of privileges. This measure
ensures that users operating the container are not part of the sudoers group, reducing potential
security breaches.
Specify a Non-root User
We specify a non-root user in our Dockerfile to mitigate security risks. This user (a12 with UID 1001
and GID 1001) is consistently used across all backend components to prevent container-to-host
privilege escalation.
# Specifying a non-root user
3

-- 3 of 4 --

RUN groupadd -g 1001 a12 && useradd -r -u 1001 -g a12 a12
USER a12
These practices ensure that our backend Docker images are secure by default, providing a robust
foundation for deploying our backend services with confidence. Our commitment to security
extends through every layer of our Docker images, from base image selection to efficient handling
of secrets and minimizing potential vulnerabilities.
For any questions or further information, please reach out to our security team.
4

-- 4 of 4 --


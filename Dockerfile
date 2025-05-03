FROM openjdk:17-buster
WORKDIR /app

# Copy application JAR and wait-for-redis script
COPY target/ratelimiter-0.0.1-SNAPSHOT.jar app.jar
COPY wait-for-redis.sh wait-for-redis.sh

# Set environment variables
ENV SPRING_REDIS_HOST=redis
ENV SPRING_REDIS_PORT=6379

# Install required tools
RUN apt-get update && apt-get install -y netcat dos2unix
RUN dos2unix wait-for-redis.sh
RUN chmod +x wait-for-redis.sh

# Start wait-for-redis.sh to check Redis availability and then start the app
CMD ["./wait-for-redis.sh", "redis", "6379", "--", "java", "-jar", "app.jar"]

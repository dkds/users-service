FROM openjdk:8-jdk-alpine3.9

VOLUME /tmp

ARG DEPENDENCY=build/dist
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","-Dspring.profiles.active=docker","com.test.elk.userservice.Application"]
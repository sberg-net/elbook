FROM amazoncorretto:17-alpine3.21-jdk

ENV DB_HOST="localhost"
ENV DB_PORT="3306"
ENV DB_NAME="elbook"
ENV DB_USER="elbook"
ENV DB_PASSWORD="elbook"
ENV FLYWAY_USER=\${DB_USER}
ENV FLYWAY_PASSWORD=\${DB_PASSWORD}
ENV FLYWAY_CHECKSUM_REPAIR="false"

ENV MAIL_HOST="notSet"
ENV MAIL_PORT="465"
ENV MAIL_USERNAME="notSet"
ENV MAIL_PASSWORD="notSet"
ENV MAIL_SSL_ENABLED="true"

ENV DEFAULT_ADMIN_USER="admin"
ENV DEFAULT_ADMIN_PWD="admin"
ENV VZD_CRED_ENCRYPTION_KEYS="bravo,delta,tango,27"

ENV ELBOOK_APP_KARTENDATENTRANS_BASE_URL="localhost"
ENV APP_PROFILES="default"
ENV TZ=Europe/Berlin

ENV LOG_MAIL_ENABLE_ERROR_APPENDER="false"
ENV LOG_MAIL_SUBJECT_ADDON="notSet"
ENV LOG_MAIL_SMTP_HOST="notSet"
ENV LOG_MAIL_SMTP_PORT="25"
ENV LOG_MAIL_STARTTLS="true"
ENV LOG_MAIL_USERNAME="notSet"
ENV LOG_MAIL_PASSWORD="notSet"
ENV LOG_MAIL_SEND_FROM="notSet"
ENV LOG_MAIL_SEND_TO="notSet"

RUN apk add --update ttf-dejavu
RUN apk add --no-cache openssh-client tzdata bash

COPY target/elbook-*.jar /opt/app.jar

RUN mkdir /elbook
RUN chmod a+rwx /elbook
WORKDIR /elbook

RUN addgroup --gid 2728 elbook \
  &&  adduser --disabled-password --ingroup elbook \
  --home /elbook --no-create-home --uid 2728 elbook

USER elbook:elbook

ENTRYPOINT ["java",\
            "-Dspring.profiles.active=${APP_PROFILES}",\
            "-jar","/opt/app.jar"]

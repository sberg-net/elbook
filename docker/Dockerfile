FROM amazoncorretto:17-alpine3.21-jdk

ENV ELBOOK_DB_HOST="localhost"
ENV ELBOOK_DB_PORT="3306"
ENV ELBOOK_DB_ROOTPASSWORD="mrdata123!"
ENV ELBOOK_DB_NAME="elbook"
ENV ELBOOK_DB_USER="elbook"
ENV ELBOOK_DB_PASSWORD="elbook"

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

ENV LOG_MAIL_ENABLE_ERROR_APPENDER="false"
ENV LOG_MAIL_SUBJECT_ADDON="notSet"
ENV LOG_MAIL_SMTP_HOST="notSet"
ENV LOG_MAIL_SMTP_PORT="25"
ENV LOG_MAIL_STARTTLS="true"
ENV LOG_MAIL_USERNAME="notSet"
ENV LOG_MAIL_PASSWORD="notSet"
ENV LOG_MAIL_SEND_FROM="notSet"
ENV LOG_MAIL_SEND_TO="notSet"

RUN mkdir /opt/elbook
COPY target/elbook-*.jar /opt/elbook/app.jar

ENV TZ=Europe/Berlin
RUN apk add --update ttf-dejavu
RUN apk add --no-cache openssh-client tzdata bash

ENTRYPOINT ["java","-Dspring.profiles.active=${APP-PROFILES}",\
            "-Delbook.db.rootpassword=${ELBOOK-DB-ROOTPASSWORD}",\
            "-jar","/opt/elbook/app.jar"]

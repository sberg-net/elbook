FROM amazoncorretto:17-alpine3.16-jdk

ENV ELBOOK-DB-HOST="localhost"
ENV ELBOOK-DB-PORT="3306"
ENV ELBOOK-DB-ROOTPASSWORD="mrdata123!"
ENV ELBOOK-DB-NAME="elbook"
ENV ELBOOK-DB-USER="elbook"
ENV ELBOOK-DB-PASSWORD="elbook"

ENV MAIL-HOST="notSet"
ENV MAIL-PORT="465"
ENV MAIL-USERNAME="notSet"
ENV MAIL-PASSWORD="notSet"
ENV MAIL-SSL-ENABLED="true"

ENV DEFAULT_ADMIN_USER="admin"
ENV DEFAULT_ADMIN_PWD="admin"
ENV VZD_CRED_ENCRYPTION_KEYS="bravo,delta,tango,27"

ENV ELBOOK-APP-KARTENDATENTRANS-BASE-URL="localhost"
ENV APP-PROFILES="default"

ENV LOG-MAIL-ENABLE-ERROR-APPENDER="false"
ENV LOG-MAIL-SUBJECT-ADDON="notSet"
ENV LOG-MAIL-SMTP-HOST="notSet"
ENV LOG-MAIL-SMTP-PORT="25"
ENV LOG-MAIL-STARTTLS="true"
ENV LOG-MAIL-USERNAME="notSet"
ENV LOG-MAIL-PASSWORD="notSet"
ENV LOG-MAIL-SEND-FROM="notSet"
ENV LOG-MAIL-SEND-TO="notSet"

RUN mkdir /opt/elbook
COPY target/elbook-*.jar /opt/elbook/app.jar

ENV TZ=Europe/Berlin
RUN apk add --update ttf-dejavu
RUN apk add --no-cache openssh-client tzdata bash

ENTRYPOINT ["java","-Dspring.profiles.active=${APP-PROFILES}",\
            "-Delbook.db.host=${ELBOOK-DB-HOST}",\
            "-Delbook.db.port=${ELBOOK-DB-PORT}",\
            "-Delbook.db.rootpassword=${ELBOOK-DB-ROOTPASSWORD}",\
            "-Delbook.db.name=${ELBOOK-DB-NAME}",\
            "-Delbook.db.user=${ELBOOK-DB-USER}",\
            "-Delbook.db.password=${ELBOOK-DB-PASSWORD}",\
            "-Dspring.mail.host=${MAIL-HOST}",\
            "-Dspring.mail.port=${MAIL-PORT}",\
            "-Dspring.mail.username=${MAIL-USERNAME}",\
            "-Dspring.mail.password=${MAIL-PASSWORD}",\
            "-Dspring.mail.properties.mail.smtp.ssl.enable=${MAIL-SSL-ENABLED}",\
            "-Delbook.defaultAdminUser=${DEFAULT_ADMIN_USER}",\
            "-Delbook.defaultAdminPwd=${DEFAULT_ADMIN_PWD}",\
            "-Delbook.encryptionKeys=${VZD_CRED_ENCRYPTION_KEYS}",\
            "-Delbook.app.kartendatentrans-base-url=${ELBOOK-APP-KARTENDATENTRANS-BASE-URL}",\
            "-Dlog.mail.enable.error.appender=${LOG-MAIL-ENABLE-ERROR-APPENDER}",\
            "-Dlog.mail.subject.addon=${LOG-MAIL-SUBJECT-ADDON}",\
            "-Dlog.mail.smtp.host=${LOG-MAIL-SMTP-HOST}",\
            "-Dlog.mail.smtp.port=${LOG-MAIL-SMTP-PORT}",\
            "-Dlog.mail.starttls=${LOG-MAIL-STARTTLS}",\
            "-Dlog.mail.username=${LOG-MAIL-USERNAME}",\
            "-Dlog.mail.password=${LOG-MAIL-PASSWORD}",\
            "-Dlog.mail.send.from=${LOG-MAIL-SEND-FROM}",\
            "-Dlog.mail.send.to=${LOG-MAIL-SEND-TO}",\
            "-jar","/opt/elbook/app.jar"]
# Docker Compose

## Beispiel docker compose File
Das Beispiel zeigt ein Basis-Compose-File für elbook.
Für Details müssen die Parameter aus [README.md](..%2FREADME.md) beachtet werden.

`docker-compose.yml`
```dockerfile
version: '3.3'
services:
  elbook-springboot:
    environment:
      ELBOOK-DB-HOST: "elbook-mariadb"
      ELBOOK-DB-ROOTPASSWORD: "elbook-db-master-pwd"
    depends_on:
      - elbook-mariadb
    restart: always
    image: sbergit/elbook:2.0.1
    container_name: elbook-springboot
    volumes:
      - ./springboot/logs:/logs:Z
      - ./springboot/data:/data:Z
    ports:
      - 80:8080
    networks:
      - elbook

  elbook-mariadb:
    restart: always
    image: mariadb:10.11.8
    container_name: elbook-mariadb
    environment:
      MYSQL_ROOT_PASSWORD: "elbook-db-master-pwd"
      MYSQL_ALLOW_EMPTY_PASSWORD: "no"
      TZ: 'Europe/Berlin'
    volumes:
      - ./mariadb-db/:/var/lib/mysql:Z
    networks:
      - elbook
  
networks:
  elbook:
```

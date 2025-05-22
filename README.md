# lanaseq

Location Analysis Net Application for High-throughput sequencing

## Install

### Compile

```shell
mvn package -Dtest=skip
```

### Configure

* Copy `lanaseq.war` file on server
* Create `application.yml` file at same location as `lanaseq.war`

Here is an example of the most common properties present in `application.yml`:

```yaml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost/lanaseq
    username: lanaseq
    password: 'some_password'
  ldap:
    base: ou=users,ou=example,dc=com
    template:
      ignore-partial-result-exception: true
    urls: ldap://ldap.example.com
    username: lanaseq
    password: 'some_password'
  mail:
    host: mail.example.com

server:
  port: 8080
  forward-headers-strategy: NATIVE

vaadin:
  productionMode: true

app:
  home:
    folder: ${user.home}/lanaseq/network_drive
    windows-label: 'Z:\lanaseq'
    unix-label: 'smb://server/lanaseq'
  upload:
    folder: ${app.home.folder}/upload
    windows-label: '${app.home.windows-label}\upload'
    unix-label: '${app.home.unix-label}/upload'
  server-url: https://lanaseq.example.com

security:
  rememberMeKey: 'some_random_key'

ldap:
  enabled: true
  id-attribute: sAMAccountName
  object-class: user

mail:
  from: lanaseq@example.com
  to: lanaseq@example.com

logging:
  file:
    name: ${user.home}/lanaseq/log/${spring.application.name}.log
  level:
    com.vaadin: DEBUG
```

### Start server as a service

Use systemd or init.d to start the service.

### Database

Copy the file `src/migration/environments/development.properties` to
`src/migration/environments/development.properties` and replace at least the following values:

```text
driver=org.mariadb.jdbc.Driver
url=jdbc:mariadb://localhost/lanaseq
username=lanaseq
password=some_password
```

Run the migration script.

```shell
mvn migration:up
```

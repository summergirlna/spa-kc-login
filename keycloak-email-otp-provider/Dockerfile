FROM maven:3.9-eclipse-temurin-21 AS provider-build

WORKDIR /workspace

COPY keycloak-email-otp-provider/pom.xml keycloak-email-otp-provider/pom.xml

RUN mvn -f keycloak-email-otp-provider/pom.xml dependency:go-offline

COPY keycloak-email-otp-provider/src keycloak-email-otp-provider/src

RUN mvn -f keycloak-email-otp-provider/pom.xml clean package -DskipTests

FROM quay.io/keycloak/keycloak:26.2.5

COPY --from=provider-build /workspace/keycloak-email-otp-provider/target/keycloak-email-otp-provider-0.0.1-SNAPSHOT.jar /opt/keycloak/providers/keycloak-email-otp-provider.jar

RUN /opt/keycloak/bin/kc.sh build
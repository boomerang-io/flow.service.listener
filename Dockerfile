FROM eclipse-temurin:17.0.19_10-jre-ubi10-minimal

RUN if command -v microdnf >/dev/null 2>&1; then \
        microdnf update -y && microdnf install -y binutils && microdnf clean all; \
    elif command -v dnf >/dev/null 2>&1; then \
        dnf -y update && dnf -y install binutils && dnf clean all; \
    else \
        echo "No supported package manager found" && exit 1; \
    fi
    
ENV JAVA_OPTS=""
ENV BMRG_HOME=/opt/boomerang
ENV BMRG_SVC=service-listener

WORKDIR $BMRG_HOME
ADD target/$BMRG_SVC.jar service.jar
RUN sh -c 'touch /service.jar'

# Create user, chown, and chmod. 
# OpenShift requires that a numeric user is used in the USER declaration instead of the user name
RUN chmod -R u+x $BMRG_HOME \
    && chgrp -R 0 $BMRG_HOME \
    && chmod -R g=u $BMRG_HOME
USER 2000

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ./service.jar" ]

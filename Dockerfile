FROM openjdk:8-alpine

WORKDIR /opt/app

##### DEFAULT VALUES, COULD BE CHANGED DURING BUILDS #####
ARG profile=dev
ARG environ=yizsell-environment.properties

###### ALTHOUGH DEFAULTS HAVE BEEN SET, YOU CAN CHOOSE TO CHANGE EITHER OR ALL OF THEM #####
ENV APP_ACTIVE_PROFILE=$profile
ENV APP_ENVIRONMENT=$environ

#### Verifying which build is currently being run
RUN echo "Running a docker build for $APP_ACTIVE_PROFILE"

ADD target/*.jar inventory-system-backend-1.0.0.RELEASE.jar

CMD java -jar -DAPP_ACTIVE_PROFILE=$APP_ACTIVE_PROFILE -Dspring.config.additional-location=file:///opt/app/$APP_ENVIRONMENT inventory-system-backend-1.0.0.RELEASE.jar
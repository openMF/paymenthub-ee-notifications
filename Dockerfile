FROM eclipse-temurin:21-jre
# 9191 = the message gateway HTTP API (/sms, /smsbridges, /tenants) and the actuator.
# 5000 = the Camel REST routes of the notification connector.
EXPOSE 9191 5000

# Copy the boot jar by name, not `*.jar`: `gradlew build` also produces a
# -plain.jar, which has no main class and sorts first in the glob.
COPY build/libs/app.jar app.jar
CMD ["java", "-jar", "app.jar"]

production:
	make stop || true
	mvn clean package spring-boot:repackage
	java \
		-Xmx330m \
		-Xss512k \
		-Dspring.profiles.active=production \
		-Xdebug -Xrunjdwp:transport=dt_socket,address=8785,server=y,suspend=n \
		-jar target/FootballBot-1.0-SNAPSHOT.jar &

stop:
	kill -9 $$(ps aux | grep FootballBot | grep -v grep | awk '{print $$2}')

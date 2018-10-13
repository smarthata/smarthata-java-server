
USER_ID ?= $(shell stat -c "%u:%g" .)


build:
	mvn clean package
run:
	mvn spring-boot:run


# Server deploy
update:
	git fetch
	git reset --hard origin/master
release:
	mvn --batch-mode release:prepare release:perform
deploy:
	systemctl stop smarthata
	sleep 20
	cp ./target/smarthata.jar /app/smarthata/
	systemctl start smarthata


# Docker build
docker-spring-boot-run: docker-rm
	docker run -d --name smarthata-server \
		-v $$(pwd):/app -w /app \
		-v $$(pwd)/.mvn:/var/maven/.m2 \
		-p 8080:8080 \
		maven:3.5 \
		mvn -Duser.home=/var/maven spring-boot:run
	docker logs -f smarthata-server

docker-build:
	docker run --rm \
        -v $$(pwd):/app -w /app \
        -v $$(pwd)/.mvn:/var/maven/.m2 \
        -u ${USER_ID} \
        maven:3.5 \
        mvn -Duser.home=/var/maven package

docker-run: docker-build docker-rm
	docker run -d --name smarthata-server \
        -v $$(pwd):/app -w /app \
        -p 8080:8080 \
        maven:3.5 \
        java -jar ./target/smarthata-server-*.jar
	docker logs -f smarthata-server

docker-stop:
	-docker stop smarthata-server

docker-rm: docker-stop
	-docker rm smarthata-server


# Docker Mysql
mysql-start: mysql-stop
	docker start smarthata-mysql

mysql-run: mysql-rm
	docker run -d --name smarthata-mysql -p3306:3306 \
		-e MYSQL_RANDOM_ROOT_PASSWORD=true \
		-e MYSQL_DATABASE=smarthata \
		-e MYSQL_USER=smarthata \
		-e MYSQL_PASSWORD=password \
		mariadb
	docker logs -f smarthata-mysql

mysql-stop:
	-docker stop smarthata-mysql

mysql-rm: mysql-stop
	-docker rm smarthata-mysql

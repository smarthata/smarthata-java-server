
USER_ID ?= $(shell stat -c "%u:%g" .)


build:
	mvn clean package
run:
	mvn spring-boot:run

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

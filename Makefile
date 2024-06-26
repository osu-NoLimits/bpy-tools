#!/usr/bin/env make

build:
	cd bpy-tools/ && mvn clean package shade:shade;
	mv bpy-tools/target/build-jar-with-dependencies.jar build.jar

run:
	@if screen -ls | grep -q "bpy-tools"; then \
        echo "Screen session 'bpy-tools' is running."; \
		exit 1; \
    else \
        echo "Screen session 'bpy-tools' is not running."; \
    fi

	screen -S bpy-tools java -jar build.jar;

restart:
	@if screen -ls | grep -q "bpy-tools"; then \
        echo "Screen session 'bpy-tools' is running."; \
    else \
        echo "Screen session 'bpy-tools' is not running."; \
		exit 1; \
    fi

	make stop
	screen -S bpy-tools java -jar build.jar;

restart-b:
	make build;
	make restart;

run-b:
	make build;
	make run;

go:
	screen -x bpy-tools

stop:
	screen -S bpy-tools -X quit; \

no-screen:
	java -jar build.jar




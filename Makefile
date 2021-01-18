run: build
	java mylox/Lox

build:
	javac mylox/*.java

clean:
	rm -rf mylox/*.class
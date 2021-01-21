run: build
	java mylox/Lox

build:
	@javac mylox/*.java
	@javac tool/GenerateAST.java

build-ast: build
	@java tool.GenerateAST mylox

clean:
	rm -rf mylox/*.class tool/*.class
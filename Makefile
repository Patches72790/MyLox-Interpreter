run: build
	@java mylox/Lox

build:
	@javac mylox/*.java
	@javac tool/GenerateAST.java

debug: build
	@java mylox.Lox -S -P

build-ast: build
	@rm -f Expr*
	@java tool.GenerateAST mylox

clean:
	rm -rf mylox/*.class tool/*.class
run: build
	@java mylox/Lox

test: build
	@java mylox/Lox tests/anonFuncTest.lox
	
build: build-ast
	@javac mylox/*.java

# debug: build
# @java mylox.Lox -S -P

build-ast: 
	@rm -f Expr*
	@javac tool/GenerateAST.java
	@java tool.GenerateAST mylox

clean:
	rm -rf mylox/*.class tool/*.class 

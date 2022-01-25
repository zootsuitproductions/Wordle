JFLAGS = -g
JC = javac -cp .:res/java-json.jar  src/Client.java
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# This uses the line continuation character (\) for readability
# You can list these all on a single line, separated by a space instead.
# If your version of make can't handle the leading tabs on each
# line, just remove them (these are also just added for readability).
CLASSES = \
	src/Client.java \
	src/WordleGame.java \
	src/Main.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        Bplustree.java \
        Container.java \
        Internal_Node.java \
        Leaf_Node.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
        $(RM) *.class
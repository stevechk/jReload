# jReload
java library that allows on-the-fly recompilation and reloading of java source code

Just grab the jreload.jar, and ensure your application is started with the line:
<pre>
-javaagent:/path/to/jreload.jar
</pre>

You will also need to run your application under the JDK (NOT JRE) and include tools.jar
on the classpath.

Example usage:

<pre>
// create a Recompiler for each source class.
// pass in the root source directory and the full (canonical) class name
Recompiler exampleRecompiler = new Recompiler("src", "org.jreload.ExampleClass");

// create an instance of org.jreload.ExampleClass
ExampleClass exampleInstance = new ExampleClass();
exampleInstance.DoSomething();

while (true) {

	// now, open ExampleClass.java in a text editor and make some changes
	System.out.println("edit the file, and press any key to continue");
	System.in.read();

	// recompile the updated ExampleClass.java
	exampleRecompiler.recompile();

	// instance will automatically execute the new code
	exampleInstance.DoSomething();

}
</pre>

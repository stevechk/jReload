package org.jreload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Allow a java source file to be recompiled and reloaded dynamically at runtime
 * 
 * @author Steve Cook
 */
public class Recompiler {

	private static final String JAVA_SOURCE_FILE_EXTENSION = ".java";
	private static final String JAVA_CLASS_FILE_EXTENSION = ".class";

	private String sourceRootPath = ""; // root path to source files
	private String canonicalClassName = ""; // canonical name of source class
	private JavaCompiler compiler = null; // reference to java toolchain

	/**
	 * 
	 * Create instance of this type, which can be recompiled/reloaded on the
	 * fly. Pass the root path and full class name of source file.
	 * 
	 * @param sourceRootPath
	 *            Root path to source file
	 * @param canonicalClassName
	 *            Full canonical class name (e.g. com.example.MyClass)
	 * 
	 * @throws ClassNotFoundException
	 * @throws ReloadException
	 */
	public Recompiler(String sourceRootPath, String canonicalClassName)
			throws ReloadException {

		// store source location & class name
		this.sourceRootPath = sourceRootPath;
		this.canonicalClassName = canonicalClassName;

		// get the compiler from the javax.tools ToolProvider class
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			throw new ReloadException(
					"Couldnt find JDK compiler on path (JDK tools.jar)");

		// compile current copy of source code
		recompile();
	}

	/**
	 * Recompiles and reloads this class from source
	 * 
	 * @throws ClassNotFoundException
	 * @throws ReloadException
	 */
	public void recompile() throws ReloadException {

		// convert class name into expected path
		String sourceFilename = canonicalClassName.replace(".", File.separator);

		// open source file
		String sourceFullPath = sourceFilename + JAVA_SOURCE_FILE_EXTENSION;
		File sourceFile = new File(sourceRootPath, sourceFullPath);
		if (!sourceFile.exists()) {
			throw new ReloadException("Source file " + sourceFullPath
					+ " not found");
		}

		// compile source on the fly, using javax.tools.JavaCompiler
		ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
		int errorCode = compiler.run(null, null, errorStream, sourceFile
				.getAbsoluteFile().toString());

		if (errorCode != 0) {
			String errorDetails = errorStream.toString();
			throw new ReloadException("Couldnt compile source for "
					+ sourceFullPath + ": " + errorDetails);
		}

		try {

			// read newly compiled bytecode from disk
			File compiledFile = new File(sourceRootPath, sourceFilename
					+ JAVA_CLASS_FILE_EXTENSION);
			byte[] newByteCode = Files.readAllBytes(compiledFile.toPath());

			// call instrumentation to reload class
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(
					canonicalClassName);
			InstrumentationLoader.reload(clazz, newByteCode);

		} catch (IOException e) {
			throw new ReloadException(
					"Source file could not be read, or class file written", e);
		} catch (ClassNotFoundException e) {
			throw new ReloadException("Class not found", e);
		}

	}

}

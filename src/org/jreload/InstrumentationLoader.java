package org.jreload;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Hook into the instrumentation API to allow class reloading.
 * 
 * NB. To use instrumentation API: 
 * 1. "Can-Redefine-Classes=true" and 
 *    "Premain-Class=org.jreload.InstrumentationLoader" MUST be defined in that
 *    order in the jar MANIFEST.MF file 
 * 2. The program must be started with the -javaagent flag, including this 
 *    class on the jarpath
 * 
 * see https://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-
 * summary.html for more details
 * 
 * @author Steve Cook
 */
public class InstrumentationLoader {

	private static Instrumentation instrumentationInstance = null;

	/**
	 * premain() method is called directly by the JVM at launch time when
	 * MANIFEST and -javaagent are set correctly
	 * 
	 * @param agentArguments
	 * @param instrumentation
	 */
	public static void premain(String agentArguments,
			Instrumentation instrumentation) {
		instrumentationInstance = instrumentation;
	}

	/**
	 * Force JVM to overwrite class definition with new compiled byte code
	 * 
	 * @param classType
	 * @param newByteCode
	 * @throws ReloadException
	 * 
	 * @throws ClassNotFoundException
	 * @throws UnmodifiableClassException
	 */
	public static void reload(Class<?> classType, byte[] newByteCode)
			throws ReloadException {
		if (instrumentationInstance == null) {
			throw new ReloadException(
					"Instrumenation API not loaded - class reload not successful");
		} else {
			ClassDefinition definition = new ClassDefinition(classType,
					newByteCode);
			try {
				instrumentationInstance.redefineClasses(definition);
			} catch (ClassNotFoundException e) {
				throw new ReloadException("Class not found during reload", e);
			} catch (UnmodifiableClassException e) {
				throw new ReloadException("Class cannot be modified", e);
			}
		}
	}
}

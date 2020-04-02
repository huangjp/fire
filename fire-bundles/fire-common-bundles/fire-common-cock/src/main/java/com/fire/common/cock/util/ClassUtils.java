package com.fire.common.cock.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AccessControlException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * Operates on classes without using reflection.
 * </p>
 *
 * <p>
 * This class handles invalid <code>null</code> inputs as best it can. Each
 * method documents its behaviour in more detail.
 * </p>
 *
 * <p>
 * The notion of a <code>canonical name</code> includes the human readable name
 * for the type, for example <code>int[]</code>. The non-canonical method
 * variants work with the JVM names, such as <code>[I</code>.
 * </p>
 *
 * @author Apache Software Foundation
 * @author Gary Gregory
 * @author Norm Deane
 * @author Alban Peignier
 * @author Tomasz Blachowicz
 * @since 2.0
 * @version $Id: ClassUtils.java 1057072 2011-01-10 01:55:57Z niallp $
 */
public class ClassUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClassUtils.class);

	/**
	 * <p>
	 * The package separator character: <code>'&#x2e;' == {@value}</code>.
	 * </p>
	 */
	public static final char PACKAGE_SEPARATOR_CHAR = '.';

	/**
	 * <p>
	 * The package separator String: <code>"&#x2e;"</code>.
	 * </p>
	 */
	public static final String PACKAGE_SEPARATOR = String
			.valueOf(PACKAGE_SEPARATOR_CHAR);

	/**
	 * <p>
	 * The inner class separator character: <code>'$' == {@value}</code>.
	 * </p>
	 */
	public static final char INNER_CLASS_SEPARATOR_CHAR = '$';

	/**
	 * <p>
	 * The inner class separator String: <code>"$"</code>.
	 * </p>
	 */
	public static final String INNER_CLASS_SEPARATOR = String
			.valueOf(INNER_CLASS_SEPARATOR_CHAR);

	/**
	 * Maps primitive <code>Class</code>es to their corresponding wrapper
	 * <code>Class</code>.
	 */
	private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
	static {
		primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
		primitiveWrapperMap.put(Byte.TYPE, Byte.class);
		primitiveWrapperMap.put(Character.TYPE, Character.class);
		primitiveWrapperMap.put(Short.TYPE, Short.class);
		primitiveWrapperMap.put(Integer.TYPE, Integer.class);
		primitiveWrapperMap.put(Long.TYPE, Long.class);
		primitiveWrapperMap.put(Double.TYPE, Double.class);
		primitiveWrapperMap.put(Float.TYPE, Float.class);
		primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
	}

	/**
	 * Maps wrapper <code>Class</code>es to their corresponding primitive types.
	 */
	private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<Class<?>, Class<?>>();

	static {
		for (Iterator<Class<?>> it = primitiveWrapperMap.keySet().iterator(); it
				.hasNext();) {
			Class<?> primitiveClass = (Class<?>) it.next();
			Class<?> wrapperClass = (Class<?>) primitiveWrapperMap
					.get(primitiveClass);
			if (!primitiveClass.equals(wrapperClass)) {
				wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
			}
		}
	}

	/**
	 * Maps a primitive class name to its corresponding abbreviation used in
	 * array class names.
	 */
	private static final Map<String, String> abbreviationMap = new HashMap<String, String>();

	/**
	 * Maps an abbreviation used in array class names to corresponding primitive
	 * class name.
	 */
	private static final Map<String, String> reverseAbbreviationMap = new HashMap<String, String>();
	/**
	 * Feed abbreviation maps
	 */
	static {
		addAbbreviation("int", "I");
		addAbbreviation("boolean", "Z");
		addAbbreviation("float", "F");
		addAbbreviation("long", "J");
		addAbbreviation("short", "S");
		addAbbreviation("byte", "B");
		addAbbreviation("double", "D");
		addAbbreviation("char", "C");
	}

	/** Suffix for array class names: "[]" */
	public static final String ARRAY_SUFFIX = "[]";

	/** Prefix for internal array class names: "[" */
	private static final String INTERNAL_ARRAY_PREFIX = "[";

	/** Prefix for internal non-primitive array class names: "[L" */
	private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

	/** The CGLIB class separator character "$$" */
	public static final String CGLIB_CLASS_SEPARATOR = "$$";

	/** The ".class" file suffix */
	public static final String CLASS_FILE_SUFFIX = ".class";

	/**
	 * Map with primitive wrapper type as key and corresponding primitive type
	 * as value, for example: Integer.class -> int.class.
	 */
	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(
			8);

	/**
	 * Map with primitive type as key and corresponding wrapper type as value,
	 * for example: int.class -> Integer.class.
	 */
	private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new HashMap<Class<?>, Class<?>>(
			8);

	/**
	 * Map with primitive type name as key and corresponding primitive type as
	 * value, for example: "int" -> "int.class".
	 */
	private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(
			32);

	/**
	 * Map with common "java.lang" class name as key and corresponding Class as
	 * value. Primarily for efficient deserialization of remote invocations.
	 */
	private static final Map<String, Class<?>> commonClassCache = new HashMap<String, Class<?>>(
			32);

	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);

		for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap
				.entrySet()) {
			primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
			registerCommonClasses(entry.getKey());
		}

		Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(32);
		primitiveTypes.addAll(primitiveWrapperTypeMap.values());
		primitiveTypes.addAll(Arrays.asList(new Class<?>[] { boolean[].class,
				byte[].class, char[].class, double[].class, float[].class,
				int[].class, long[].class, short[].class }));
		primitiveTypes.add(void.class);
		for (Class<?> primitiveType : primitiveTypes) {
			primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
		}

		registerCommonClasses(Boolean[].class, Byte[].class, Character[].class,
				Double[].class, Float[].class, Integer[].class, Long[].class,
				Short[].class);
		registerCommonClasses(Number.class, Number[].class, String.class,
				String[].class, Object.class, Object[].class, Class.class,
				Class[].class);
		registerCommonClasses(Throwable.class, Exception.class,
				RuntimeException.class, Error.class, StackTraceElement.class,
				StackTraceElement[].class);
	}

	/**
	 * Add primitive type abbreviation to maps of abbreviations.
	 *
	 * @param primitive
	 *            Canonical name of primitive type
	 * @param abbreviation
	 *            Corresponding abbreviation of primitive type
	 */
	private static void addAbbreviation(String primitive, String abbreviation) {
		abbreviationMap.put(primitive, abbreviation);
		reverseAbbreviationMap.put(abbreviation, primitive);
	}

	/**
	 * Return a path suitable for use with {@code ClassLoader.getResource} (also
	 * suitable for use with {@code Class.getResource} by prepending a slash
	 * ('/') to the return value). Built by taking the package of the specified
	 * class file, converting all dots ('.') to slashes ('/'), adding a trailing
	 * slash if necessary, and concatenating the specified resource name to
	 * this. <br/>
	 * As such, this function may be used to build a path suitable for loading a
	 * resource file that is in the same package as a class file, although
	 * {@link org.springframework.core.io.ClassPathResource} is usually even
	 * more convenient.
	 * 
	 * @param clazz
	 *            the Class whose package will be used as the base
	 * @param resourceName
	 *            the resource name to append. A leading slash is optional.
	 * @return the built-up resource path
	 * @see ClassLoader#getResource
	 * @see Class#getResource
	 */
	public static String addResourcePathToPackagePath(Class<?> clazz,
			String resourceName) {
		Assert.notNull(resourceName, "Resource name must not be null");
		if (!resourceName.startsWith("/")) {
			return classPackageAsResourcePath(clazz) + "/" + resourceName;
		}
		return classPackageAsResourcePath(clazz) + resourceName;
	}

	/**
	 * Build a String that consists of the names of the classes/interfaces in
	 * the given array.
	 * <p>
	 * Basically like {@code AbstractCollection.toString()}, but stripping the
	 * "class "/"interface " prefix before every class name.
	 * 
	 * @param classes
	 *            a Collection of Class objects (may be {@code null})
	 * @return a String of form "[com.foo.Bar, com.foo.Baz]"
	 * @see AbstractCollection#toString()
	 */
	public static String classNamesToString(Class<?>... classes) {
		return classNamesToString(Arrays.asList(classes));
	}

	/**
	 * Build a String that consists of the names of the classes/interfaces in
	 * the given collection.
	 * <p>
	 * Basically like {@code AbstractCollection.toString()}, but stripping the
	 * "class "/"interface " prefix before every class name.
	 *
	 * @param classes
	 *            a Collection of Class objects (may be {@code null})
	 * @return a String of form "[com.foo.Bar, com.foo.Baz]"
	 * @see AbstractCollection#toString()
	 */
	public static String classNamesToString(Collection<Class<?>> classes) {
		if (CollectionUtils.isEmpty(classes)) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (Iterator<Class<?>> it = classes.iterator(); it.hasNext();) {
			Class<?> clazz = it.next();
			sb.append(clazz.getName());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Given an input class object, return a string which consists of the
	 * class's package name as a pathname, i.e., all dots ('.') are replaced by
	 * slashes ('/'). Neither a leading nor trailing slash is added. The result
	 * could be concatenated with a slash and the name of a resource and fed
	 * directly to {@code ClassLoader.getResource()}. For it to be fed to
	 * {@code Class.getResource} instead, a leading slash would also have to be
	 * prepended to the returned value.
	 *
	 * @param clazz
	 *            the input class. A {@code null} value or the default (empty)
	 *            package will result in an empty string ("") being returned.
	 * @return a path which represents the package name
	 * @see ClassLoader#getResource
	 * @see Class#getResource
	 */
	public static String classPackageAsResourcePath(Class<?> clazz) {
		if (clazz == null) {
			return "";
		}
		String className = clazz.getName();
		int packageEndIndex = className.lastIndexOf('.');
		if (packageEndIndex == -1) {
			return "";
		}
		String packageName = className.substring(0, packageEndIndex);
		return packageName.replace('.', '/');
	}

	/**
	 * <p>
	 * Given a <code>List</code> of <code>Class</code> objects, this method
	 * converts them into class names.
	 * </p>
	 *
	 * <p>
	 * A new <code>List</code> is returned. <code>null</code> objects will be
	 * copied into the returned list as <code>null</code>.
	 * </p>
	 *
	 * @param classes
	 *            the classes to change
	 * @return a <code>List</code> of class names corresponding to the Class
	 *         objects, <code>null</code> if null input
	 * @throws ClassCastException
	 *             if <code>classes</code> contains a non-<code>Class</code>
	 *             entry
	 */
	public static List<Object> convertClassesToClassNames(List<?> classes) {
		if (classes == null) {
			return null;
		}
		List<Object> classNames = new ArrayList<Object>(classes.size());
		for (Iterator<?> it = classes.iterator(); it.hasNext();) {
			Class<?> cls = (Class<?>) it.next();
			if (cls == null) {
				classNames.add(null);
			} else {
				classNames.add(cls.getName());
			}
		}
		return classNames;
	}

	// Convert list
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Given a <code>List</code> of class names, this method converts them into
	 * classes.
	 * </p>
	 *
	 * <p>
	 * A new <code>List</code> is returned. If the class name cannot be found,
	 * <code>null</code> is stored in the <code>List</code>. If the class name
	 * in the <code>List</code> is <code>null</code>, <code>null</code> is
	 * stored in the output <code>List</code>.
	 * </p>
	 *
	 * @param classNames
	 *            the classNames to change
	 * @return a <code>List</code> of Class objects corresponding to the class
	 *         names, <code>null</code> if null input
	 * @throws ClassCastException
	 *             if classNames contains a non String entry
	 */
	public static List<Class<?>> convertClassNamesToClasses(List<?> classNames) {
		if (classNames == null) {
			return null;
		}
		List<Class<?>> classes = new ArrayList<Class<?>>(classNames.size());
		for (Iterator<?> it = classNames.iterator(); it.hasNext();) {
			String className = (String) it.next();
			try {
				classes.add(Class.forName(className));
			} catch (Exception ex) {
				classes.add(null);
			}
		}
		return classes;
	}

	/**
	 * Convert a "."-based fully qualified class name to a "/"-based resource
	 * path.
	 *
	 * @param className
	 *            the fully qualified class name
	 * @return the corresponding resource path, pointing to the class
	 */
	public static String convertClassNameToResourcePath(String className) {
		Assert.notNull(className, "Class name must not be null");
		return className.replace('.', '/');
	}

	/**
	 * Convert a "/"-based resource path to a "."-based fully qualified class
	 * name.
	 *
	 * @param resourcePath
	 *            the resource path pointing to a class
	 * @return the corresponding fully qualified class name
	 */
	public static String convertResourcePathToClassName(String resourcePath) {
		Assert.notNull(resourcePath, "Resource path must not be null");
		return resourcePath.replace('/', '.');
	}

	/**
	 * Create a composite interface Class for the given interfaces, implementing
	 * the given interfaces in one single Class.
	 * <p>
	 * This implementation builds a JDK proxy class for the given interfaces.
	 *
	 * @param interfaces
	 *            the interfaces to merge
	 * @param classLoader
	 *            the ClassLoader to create the composite Class in
	 * @return the merged interface as Class
	 * @see Proxy#getProxyClass
	 */
	public static Class<?> createCompositeInterface(Class<?>[] interfaces,
			ClassLoader classLoader) {
		Assert.notEmpty(interfaces, "Interfaces must not be empty");
		Assert.notNull(classLoader, "ClassLoader must not be null");
		return Proxy.getProxyClass(classLoader, interfaces);
	}

	/**
	 * Determine the common ancestor of the given classes, if any.
	 *
	 * @param clazz1
	 *            the class to introspect
	 * @param clazz2
	 *            the other class to introspect
	 * @return the common ancestor (i.e. common superclass, one interface
	 *         extending the other), or {@code null} if none found. If any of
	 *         the given classes is {@code null}, the other class will be
	 *         returned.
	 * @since 3.2.6
	 */
	public static Class<?> determineCommonAncestor(Class<?> clazz1,
			Class<?> clazz2) {
		if (clazz1 == null) {
			return clazz2;
		}
		if (clazz2 == null) {
			return clazz1;
		}
		if (clazz1.isAssignableFrom(clazz2)) {
			return clazz1;
		}
		if (clazz2.isAssignableFrom(clazz1)) {
			return clazz2;
		}
		Class<?> ancestor = clazz1;
		do {
			ancestor = ancestor.getSuperclass();
			if (ancestor == null || Object.class.equals(ancestor)) {
				return null;
			}
		} while (!ancestor.isAssignableFrom(clazz2));
		return ancestor;
	}

	public static void findClassInPackageByFile(String packageName,
			String filePath, final boolean recursive, List<Class<?>> clazzs) {
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		// 在给定的目录下找到所有的文件，并且进行条件过滤
		File[] dirFiles = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				boolean acceptDir = recursive && file.isDirectory();// 接受dir目录
				boolean acceptClass = file.getName().endsWith("class");// 接受class文件
				return acceptDir || acceptClass;
			}
		});

		if (null != dirFiles) {
			for (File file : dirFiles) {
				if (file.isDirectory()) {
					findClassInPackageByFile(
							packageName + "." + file.getName(),
							file.getAbsolutePath(), recursive, clazzs);
				} else {
					String className = file.getName().substring(0,
							file.getName().length() - 6);
					try {
						clazzs.add(Thread.currentThread()
								.getContextClassLoader()
								.loadClass(packageName + "." + className));
					} catch (Exception e) {
						LOG.error("{}", e);
					}
				}
			}
		}
	}

	/**
	 * Replacement for {@code Class.forName()} that also returns Class instances
	 * for primitives (e.g."int") and array class names (e.g. "String[]").
	 * Furthermore, it is also capable of resolving inner class names in Java
	 * source style (e.g. "java.lang.Thread.State" instead of
	 * "java.lang.Thread$State").
	 *
	 * @param name
	 *            the name of the Class
	 * @param classLoader
	 *            the class loader to use (may be {@code null}, which indicates
	 *            the default class loader)
	 * @return Class instance for the supplied name
	 * @throws ClassNotFoundException
	 *             if the class was not found
	 * @throws LinkageError
	 *             if the class file could not be loaded
	 * @see Class#forName(String, boolean, ClassLoader)
	 */
	public static Class<?> forName(String name, ClassLoader classLoader)
			throws ClassNotFoundException, LinkageError {
		Assert.notNull(name, "Name must not be null");

		Class<?> clazz = resolvePrimitiveClassName(name);
		if (clazz == null) {
			clazz = commonClassCache.get(name);
		}
		if (clazz != null) {
			return clazz;
		}

		// "java.lang.String[]" style arrays
		if (name.endsWith(ARRAY_SUFFIX)) {
			String elementClassName = name.substring(0, name.length()
					- ARRAY_SUFFIX.length());
			Class<?> elementClass = forName(elementClassName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		// "[Ljava.lang.String;" style arrays
		if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
			String elementName = name.substring(
					NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
			Class<?> elementClass = forName(elementName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		// "[[I" or "[[Ljava.lang.String;" style arrays
		if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
			String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
			Class<?> elementClass = forName(elementName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		ClassLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = getDefaultClassLoader();
		}
		try {
			return classLoaderToUse.loadClass(name);
		} catch (ClassNotFoundException ex) {
			int lastDotIndex = name.lastIndexOf('.');
			if (lastDotIndex != -1) {
				String innerClassName = name.substring(0, lastDotIndex) + '$'
						+ name.substring(lastDotIndex + 1);
				try {
					return classLoaderToUse.loadClass(innerClassName);
				} catch (ClassNotFoundException ex2) {
					// swallow - let original exception get through
				}
			}
			throw ex;
		}
	}

	/**
	 * <p>
	 * Gets a <code>List</code> of all interfaces implemented by the given class
	 * and its superclasses.
	 * </p>
	 *
	 * <p>
	 * The order is determined by looking through each interface in turn as
	 * declared in the source file and following its hierarchy up. Then each
	 * superclass is considered in the same way. Later duplicates are ignored,
	 * so the order is maintained.
	 * </p>
	 *
	 * @param cls
	 *            the class to look up, may be <code>null</code>
	 * @return the <code>List</code> of interfaces in order, <code>null</code>
	 *         if null input
	 */
	public static List<Class<?>> getAllInterfaces(Class<?> cls) {
		if (cls == null) {
			return null;
		}

		List<Class<?>> interfacesFound = new ArrayList<Class<?>>();
		getAllInterfaces(cls, interfacesFound);

		return interfacesFound;
	}

	/**
	 * Get the interfaces for the specified class.
	 *
	 * @param cls
	 *            the class to look up, may be <code>null</code>
	 * @param interfacesFound
	 *            the <code>Set</code> of interfaces for the class
	 */
	private static void getAllInterfaces(Class<?> cls,
			List<Class<?>> interfacesFound) {
		while (cls != null) {
			Class<?>[] interfaces = cls.getInterfaces();

			for (int i = 0; i < interfaces.length; i++) {
				if (!interfacesFound.contains(interfaces[i])) {
					interfacesFound.add(interfaces[i]);
					getAllInterfaces(interfaces[i], interfacesFound);
				}
			}

			cls = cls.getSuperclass();
		}
	}

	/**
	 * Return all interfaces that the given instance implements as array,
	 * including ones implemented by superclasses.
	 *
	 * @param instance
	 *            the instance to analyze for interfaces
	 * @return all interfaces that the given instance implements as array
	 */
	public static Class<?>[] getAllInterfaces(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getAllInterfacesForClass(instance.getClass());
	}

	/**
	 * Return all interfaces that the given instance implements as Set,
	 * including ones implemented by superclasses.
	 *
	 * @param instance
	 *            the instance to analyze for interfaces
	 * @return all interfaces that the given instance implements as Set
	 */
	public static Set<Class<?>> getAllInterfacesAsSet(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getAllInterfacesForClassAsSet(instance.getClass());
	}

	/**
	 * Return all interfaces that the given class implements as array, including
	 * ones implemented by superclasses.
	 * <p>
	 * If the class itself is an interface, it gets returned as sole interface.
	 *
	 * @param clazz
	 *            the class to analyze for interfaces
	 * @return all interfaces that the given object implements as array
	 */
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
		return getAllInterfacesForClass(clazz, null);
	}

	/**
	 * Return all interfaces that the given class implements as array, including
	 * ones implemented by superclasses.
	 * <p>
	 * If the class itself is an interface, it gets returned as sole interface.
	 *
	 * @param clazz
	 *            the class to analyze for interfaces
	 * @param classLoader
	 *            the ClassLoader that the interfaces need to be visible in (may
	 *            be {@code null} when accepting all declared interfaces)
	 * @return all interfaces that the given object implements as array
	 */
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz,
			ClassLoader classLoader) {
		Set<Class<?>> ifcs = getAllInterfacesForClassAsSet(clazz, classLoader);
		return ifcs.toArray(new Class<?>[ifcs.size()]);
	}

	/**
	 * Return all interfaces that the given class implements as Set, including
	 * ones implemented by superclasses.
	 * <p>
	 * If the class itself is an interface, it gets returned as sole interface.
	 *
	 * @param clazz
	 *            the class to analyze for interfaces
	 * @return all interfaces that the given object implements as Set
	 */
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz) {
		return getAllInterfacesForClassAsSet(clazz, null);
	}

	/**
	 * Return all interfaces that the given class implements as Set, including
	 * ones implemented by superclasses.
	 * <p>
	 * If the class itself is an interface, it gets returned as sole interface.
	 *
	 * @param clazz
	 *            the class to analyze for interfaces
	 * @param classLoader
	 *            the ClassLoader that the interfaces need to be visible in (may
	 *            be {@code null} when accepting all declared interfaces)
	 * @return all interfaces that the given object implements as Set
	 */
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz,
			ClassLoader classLoader) {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface() && isVisible(clazz, classLoader)) {
			return Collections.<Class<?>> singleton(clazz);
		}
		Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
		while (clazz != null) {
			Class<?>[] ifcs = clazz.getInterfaces();
			for (Class<?> ifc : ifcs) {
				interfaces.addAll(getAllInterfacesForClassAsSet(ifc,
						classLoader));
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}

	// Superclasses/Superinterfaces
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Gets a <code>List</code> of superclasses for the given class.
	 * </p>
	 *
	 * @param cls
	 *            the class to look up, may be <code>null</code>
	 * @return the <code>List</code> of superclasses in order going up from this
	 *         one <code>null</code> if null input
	 */
	public static List<Class<?>> getAllSuperclasses(Class<?> cls) {
		if (cls == null) {
			return null;
		}
		List<Class<?>> classes = new ArrayList<Class<?>>();
		Class<?> superclass = cls.getSuperclass();
		while (superclass != null) {
			classes.add(superclass);
			superclass = superclass.getSuperclass();
		}
		return classes;
	}

	/**
	 * <p>
	 * Converts a given name of class into canonical format. If name of class is
	 * not a name of array class it returns unchanged name.
	 * </p>
	 * <p>
	 * Example:
	 * <ul>
	 * <li><code>getCanonicalName("[I") = "int[]"</code></li>
	 * <li>
	 * <code>getCanonicalName("[Ljava.lang.String;") = "java.lang.String[]"</code>
	 * </li>
	 * <li>
	 * <code>getCanonicalName("java.lang.String") = "java.lang.String"</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param className
	 *            the name of class
	 * @return canonical form of class name
	 * @since 2.4
	 */
	private static String getCanonicalName(String className) {
		className = StringUtils.deleteWhitespace(className);
		if (className == null) {
			return null;
		} else {
			int dim = 0;
			while (className.startsWith("[")) {
				dim++;
				className = className.substring(1);
			}
			if (dim < 1) {
				return className;
			} else {
				if (className.startsWith("L")) {
					className = className.substring(1,
							className.endsWith(";") ? className.length() - 1
									: className.length());
				} else {
					if (className.length() > 0) {
						className = (String) reverseAbbreviationMap
								.get(className.substring(0, 1));
					}
				}
				StrBuilder canonicalClassNameBuffer = new StrBuilder(className);
				for (int i = 0; i < dim; i++) {
					canonicalClassNameBuffer.append("[]");
				}
				return canonicalClassNameBuffer.toString();
			}
		}
	}

	/**
	 * Returns the (initialized) class represented by <code>className</code>
	 * using the <code>classLoader</code>. This implementation supports the
	 * syntaxes "<code>java.util.Map.Entry[]</code>", "
	 * <code>java.util.Map$Entry[]</code>", "<code>[Ljava.util.Map.Entry;</code>
	 * ", and "<code>[Ljava.util.Map$Entry;</code>".
	 *
	 * @param classLoader
	 *            the class loader to use to load the class
	 * @param className
	 *            the class name
	 * @return the class represented by <code>className</code> using the
	 *         <code>classLoader</code>
	 * @throws ClassNotFoundException
	 *             if the class is not found
	 */
	public static Class<?> getClass(ClassLoader classLoader, String className)
			throws ClassNotFoundException {
		return getClass(classLoader, className, true);
	}

	// Class loading
	// ----------------------------------------------------------------------
	/**
	 * Returns the class represented by <code>className</code> using the
	 * <code>classLoader</code>. This implementation supports the syntaxes "
	 * <code>java.util.Map.Entry[]</code>", "<code>java.util.Map$Entry[]</code>
	 * ", "<code>[Ljava.util.Map.Entry;</code>", and "
	 * <code>[Ljava.util.Map$Entry;</code>".
	 *
	 * @param classLoader
	 *            the class loader to use to load the class
	 * @param className
	 *            the class name
	 * @param initialize
	 *            whether the class must be initialized
	 * @return the class represented by <code>className</code> using the
	 *         <code>classLoader</code>
	 * @throws ClassNotFoundException
	 *             if the class is not found
	 */
	public static Class<?> getClass(ClassLoader classLoader, String className,
			boolean initialize) throws ClassNotFoundException {
		try {
			Class<?> clazz;
			if (abbreviationMap.containsKey(className)) {
				String clsName = "[" + abbreviationMap.get(className);
				clazz = Class.forName(clsName, initialize, classLoader)
						.getComponentType();
			} else {
				clazz = Class.forName(toCanonicalName(className), initialize,
						classLoader);
			}
			return clazz;
		} catch (ClassNotFoundException ex) {
			// allow path separators (.) as inner class name separators
			int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);

			if (lastDotIndex != -1) {
				try {
					return getClass(
							classLoader,
							className.substring(0, lastDotIndex)
									+ INNER_CLASS_SEPARATOR_CHAR
									+ className.substring(lastDotIndex + 1),
							initialize);
				} catch (ClassNotFoundException ex2) {
				}
			}

			throw ex;
		}
	}

	/**
	 * Returns the (initialized) class represented by <code>className</code>
	 * using the current thread's context class loader. This implementation
	 * supports the syntaxes "<code>java.util.Map.Entry[]</code>", "
	 * <code>java.util.Map$Entry[]</code>", "<code>[Ljava.util.Map.Entry;</code>
	 * ", and "<code>[Ljava.util.Map$Entry;</code>".
	 *
	 * @param className
	 *            the class name
	 * @return the class represented by <code>className</code> using the current
	 *         thread's context class loader
	 * @throws ClassNotFoundException
	 *             if the class is not found
	 */
	public static Class<?> getClass(String className)
			throws ClassNotFoundException {
		return getClass(className, true);
	}

	/**
	 * Returns the class represented by <code>className</code> using the current
	 * thread's context class loader. This implementation supports the syntaxes
	 * "<code>java.util.Map.Entry[]</code>", "<code>java.util.Map$Entry[]</code>
	 * ", "<code>[Ljava.util.Map.Entry;</code>", and "
	 * <code>[Ljava.util.Map$Entry;</code>".
	 *
	 * @param className
	 *            the class name
	 * @param initialize
	 *            whether the class must be initialized
	 * @return the class represented by <code>className</code> using the current
	 *         thread's context class loader
	 * @throws ClassNotFoundException
	 *             if the class is not found
	 */
	public static Class<?> getClass(String className, boolean initialize)
			throws ClassNotFoundException {
		ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
		ClassLoader loader = contextCL == null ? ClassUtils.class
				.getClassLoader() : contextCL;
		return getClass(loader, className, initialize);
	}

	/**
	 * Determine the name of the class file, relative to the containing package:
	 * e.g. "String.class"
	 *
	 * @param clazz
	 *            the class
	 * @return the file name of the ".class" file
	 */
	public static String getClassFileName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
	}

	public static List<Class<?>> getClasssFromJarFile(String jarPaht,
			String filePaht) {
		List<Class<?>> clazzs = new ArrayList<Class<?>>();

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarPaht);
			List<JarEntry> jarEntryList = new ArrayList<JarEntry>();

			Enumeration<JarEntry> ee = jarFile.entries();
			while (ee.hasMoreElements()) {
				JarEntry entry = (JarEntry) ee.nextElement();
				// 过滤我们出满足我们需求的东西
				if (entry.getName().startsWith(filePaht)
						&& entry.getName().endsWith(".class")) {
					jarEntryList.add(entry);
				}
			}
			for (JarEntry entry : jarEntryList) {
				String className = entry.getName().replace('/', '.');
				className = className.substring(0, className.length() - 6);

				try {
					clazzs.add(Thread.currentThread().getContextClassLoader()
							.loadClass(className));
				} catch (ClassNotFoundException e) {
					LOG.error("{}", e);
				}
			}

		} catch (IOException e1) {
			LOG.error("{}", e1);
		}
		return clazzs;
	}

	public static List<Class<?>> getClasssFromPackage(String pack) {
		List<Class<?>> clazzs = new ArrayList<Class<?>>();

		// 是否循环搜索子包
		boolean recursive = true;

		// 包名字
		String packageName = pack;
		// 包名对应的路径名称
		String packageDirName = packageName.replace('.', '/');

		Enumeration<URL> dirs;

		try {
			dirs = Thread.currentThread().getContextClassLoader()
					.getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();

				String protocol = url.getProtocol();

				if ("file".equals(protocol)) {
					System.out.println("file类型的扫描");
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findClassInPackageByFile(packageName, filePath, recursive,
							clazzs);
				} else if ("jar".equals(protocol)) {
					System.out.println("jar类型的扫描");
				}
			}

		} catch (Exception e) {
			LOG.error("{}", e);
		}

		return clazzs;
	}

	/**
	 * Determine whether the given class has a public constructor with the given
	 * signature, and return it if available (else return {@code null}).
	 * <p>
	 * Essentially translates {@code NoSuchMethodException} to {@code null}.
	 *
	 * @param clazz
	 *            the clazz to analyze
	 * @param paramTypes
	 *            the parameter types of the method
	 * @return the constructor, or {@code null} if not found
	 * @see Class#getConstructor
	 */
	public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz,
			Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		try {
			return clazz.getConstructor(paramTypes);
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	/**
	 * Return the default ClassLoader to use: typically the thread context
	 * ClassLoader, if available; the ClassLoader that loaded the ClassUtils
	 * class will be used as fallback.
	 * <p>
	 * Call this method if you intend to use the thread context ClassLoader in a
	 * scenario where you absolutely need a non-null ClassLoader reference: for
	 * example, for class path resource loading (but not necessarily for
	 * {@code Class.forName}, which accepts a {@code null} ClassLoader reference
	 * as well).
	 *
	 * @return the default ClassLoader (never {@code null})
	 * @see Thread#getContextClassLoader()
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system
			// class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ClassUtils.class.getClassLoader();
		}
		return cl;
	}

	/**
	 * Return a descriptive name for the given object's type: usually simply the
	 * class name, but component type class name + "[]" for arrays, and an
	 * appended list of implemented interfaces for JDK proxies.
	 *
	 * @param value
	 *            the value to introspect
	 * @return the qualified name of the class
	 */
	public static String getDescriptiveType(Object value) {
		if (value == null) {
			return null;
		}
		Class<?> clazz = value.getClass();
		if (Proxy.isProxyClass(clazz)) {
			StringBuilder result = new StringBuilder(clazz.getName());
			result.append(" implementing ");
			Class<?>[] ifcs = clazz.getInterfaces();
			for (int i = 0; i < ifcs.length; i++) {
				result.append(ifcs[i].getName());
				if (i < ifcs.length - 1) {
					result.append(',');
				}
			}
			return result.toString();
		} else if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		} else {
			return clazz.getName();
		}
	}

	/** The package separator character '.' */
	// private static final char PACKAGE_SEPARATOR = '.';

	/** The inner class separator character '$' */
	// private static final char INNER_CLASS_SEPARATOR = '$';

	/**
	 * Determine whether the given class has a public method with the given
	 * signature, and return it if available (else throws an
	 * {@code IllegalStateException}).
	 * <p>
	 * In case of any signature specified, only returns the method if there is a
	 * unique candidate, i.e. a single public method with the specified name.
	 * <p>
	 * Essentially translates {@code NoSuchMethodException} to
	 * {@code IllegalStateException}.
	 *
	 * @param clazz
	 *            the clazz to analyze
	 * @param methodName
	 *            the name of the method
	 * @param paramTypes
	 *            the parameter types of the method (may be {@code null} to
	 *            indicate any signature)
	 * @return the method (never {@code null})
	 * @throws IllegalStateException
	 *             if the method has not been found
	 * @see Class#getMethod
	 */
	public static Method getMethod(Class<?> clazz, String methodName,
			Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		if (paramTypes != null) {
			try {
				return clazz.getMethod(methodName, paramTypes);
			} catch (NoSuchMethodException ex) {
				throw new IllegalStateException("Expected method not found: "
						+ ex);
			}
		} else {
			Set<Method> candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (methodName.equals(method.getName())) {
					candidates.add(method);
				}
			}
			if (candidates.size() == 1) {
				return candidates.iterator().next();
			} else if (candidates.isEmpty()) {
				throw new IllegalStateException("Expected method not found: "
						+ clazz + "." + methodName);
			} else {
				throw new IllegalStateException("No unique method found: "
						+ clazz + "." + methodName);
			}
		}
	}

	/**
	 * Return the number of methods with a given name (with any argument types),
	 * for the given class and/or its superclasses. Includes non-public methods.
	 *
	 * @param clazz
	 *            the clazz to check
	 * @param methodName
	 *            the name of the method
	 * @return the number of methods with the given name
	 */
	public static int getMethodCountForName(Class<?> clazz, String methodName) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		int count = 0;
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (methodName.equals(method.getName())) {
				count++;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for (Class<?> ifc : ifcs) {
			count += getMethodCountForName(ifc, methodName);
		}
		if (clazz.getSuperclass() != null) {
			count += getMethodCountForName(clazz.getSuperclass(), methodName);
		}
		return count;
	}

	/**
	 * Determine whether the given class has a public method with the given
	 * signature, and return it if available (else return {@code null}).
	 * <p>
	 * In case of any signature specified, only returns the method if there is a
	 * unique candidate, i.e. a single public method with the specified name.
	 * <p>
	 * Essentially translates {@code NoSuchMethodException} to {@code null}.
	 *
	 * @param clazz
	 *            the clazz to analyze
	 * @param methodName
	 *            the name of the method
	 * @param paramTypes
	 *            the parameter types of the method (may be {@code null} to
	 *            indicate any signature)
	 * @return the method, or {@code null} if not found
	 * @see Class#getMethod
	 */
	public static Method getMethodIfAvailable(Class<?> clazz,
			String methodName, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		if (paramTypes != null) {
			try {
				return clazz.getMethod(methodName, paramTypes);
			} catch (NoSuchMethodException ex) {
				return null;
			}
		} else {
			Set<Method> candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (methodName.equals(method.getName())) {
					candidates.add(method);
				}
			}
			if (candidates.size() == 1) {
				return candidates.iterator().next();
			}
			return null;
		}
	}

	/**
	 * Given a method, which may come from an interface, and a target class used
	 * in the current reflective invocation, find the corresponding target
	 * method if there is one. E.g. the method may be {@code IFoo.bar()} and the
	 * target class may be {@code DefaultFoo}. In this case, the method may be
	 * {@code DefaultFoo.bar()}. This enables attributes on that method to be
	 * found.
	 * <p>
	 * <b>NOTE:</b> In contrast to
	 * this method does <i>not</i> resolve Java 5 bridge methods automatically.
	 * Call
	 * {@link org.springframework.core.BridgeMethodResolver#findBridgedMethod}
	 * if bridge method resolution is desirable (e.g. for obtaining metadata
	 * from the original method definition).
	 * <p>
	 * <b>NOTE:</b> Since Spring 3.1.1, if Java security settings disallow
	 * reflective access (e.g. calls to {@code Class#getDeclaredMethods} etc,
	 * this implementation will fall back to returning the originally provided
	 * method.
	 *
	 * @param method
	 *            the method to be invoked, which may come from an interface
	 * @param targetClass
	 *            the target class for the current invocation. May be
	 *            {@code null} or may not even implement the method.
	 * @return the specific target method, or the original method if the
	 *         {@code targetClass} doesn't implement it or is {@code null}
	 */
	public static Method getMostSpecificMethod(Method method,
			Class<?> targetClass) {
		if (method != null && isOverridable(method, targetClass)
				&& targetClass != null
				&& !targetClass.equals(method.getDeclaringClass())) {
			try {
				if (Modifier.isPublic(method.getModifiers())) {
					try {
						return targetClass.getMethod(method.getName(),
								method.getParameterTypes());
					} catch (NoSuchMethodException ex) {
						return method;
					}
				} else {
					Method specificMethod = ReflectionUtils.findMethod(
							targetClass, method.getName(),
							method.getParameterTypes());
					return (specificMethod != null ? specificMethod : method);
				}
			} catch (AccessControlException ex) {
				// Security settings are disallowing reflective access; fall
				// back to 'method' below.
			}
		}
		return method;
	}

	/**
	 * <p>
	 * Gets the package name from the canonical name of a <code>Class</code>.
	 * </p>
	 *
	 * @param cls
	 *            the class to get the package name for, may be
	 *            <code>null</code>.
	 * @return the package name or an empty string
	 * @since 2.4
	 */
	public static String getPackageCanonicalName(Class<?> cls) {
		if (cls == null) {
			return StringUtils.EMPTY;
		}
		return getPackageCanonicalName(cls.getName());
	}

	// Package name
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Gets the package name from the canonical name of an <code>Object</code>.
	 * </p>
	 *
	 * @param object
	 *            the class to get the package name for, may be null
	 * @param valueIfNull
	 *            the value to return if null
	 * @return the package name of the object, or the null value
	 * @since 2.4
	 */
	public static String getPackageCanonicalName(Object object,
			String valueIfNull) {
		if (object == null) {
			return valueIfNull;
		}
		return getPackageCanonicalName(object.getClass().getName());
	}

	/**
	 * <p>
	 * Gets the package name from the canonical name.
	 * </p>
	 *
	 * <p>
	 * The string passed in is assumed to be a canonical name - it is not
	 * checked.
	 * </p>
	 * <p>
	 * If the class is unpackaged, return an empty string.
	 * </p>
	 *
	 * @param canonicalName
	 *            the canonical name to get the package name for, may be
	 *            <code>null</code>
	 * @return the package name or an empty string
	 * @since 2.4
	 */
	public static String getPackageCanonicalName(String canonicalName) {
		return ClassUtils.getPackageName(getCanonicalName(canonicalName));
	}

	/**
	 * <p>
	 * Gets the package name of a <code>Class</code>.
	 * </p>
	 *
	 * @param cls
	 *            the class to get the package name for, may be
	 *            <code>null</code>.
	 * @return the package name or an empty string
	 */
	public static String getPackageName(Class<?> cls) {
		if (cls == null) {
			return StringUtils.EMPTY;
		}
		return getPackageName(cls.getName());
	}

	// Package name
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Gets the package name of an <code>Object</code>.
	 * </p>
	 *
	 * @param object
	 *            the class to get the package name for, may be null
	 * @param valueIfNull
	 *            the value to return if null
	 * @return the package name of the object, or the null value
	 */
	public static String getPackageName(Object object, String valueIfNull) {
		if (object == null) {
			return valueIfNull;
		}
		return getPackageName(object.getClass());
	}

	/**
	 * <p>
	 * Gets the package name from a <code>String</code>.
	 * </p>
	 *
	 * <p>
	 * The string passed in is assumed to be a class name - it is not checked.
	 * </p>
	 * <p>
	 * If the class is unpackaged, return an empty string.
	 * </p>
	 *
	 * @param className
	 *            the className to get the package name for, may be
	 *            <code>null</code>
	 * @return the package name or an empty string
	 */
	public static String getPackageName(String className) {
		if (className == null || className.length() == 0) {
			return StringUtils.EMPTY;
		}

		// Strip array encoding
		while (className.charAt(0) == '[') {
			className = className.substring(1);
		}
		// Strip Object type encoding
		if (className.charAt(0) == 'L'
				&& className.charAt(className.length() - 1) == ';') {
			className = className.substring(1);
		}

		int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
		if (i == -1) {
			return StringUtils.EMPTY;
		}
		return className.substring(0, i);
	}

	// Public method
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Returns the desired Method much like <code>Class.getMethod</code>,
	 * however it ensures that the returned Method is from a public class or
	 * interface and not from an anonymous inner class. This means that the
	 * Method is invokable and doesn't fall foul of Java bug <a
	 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4071957"
	 * >4071957</a>).
	 *
	 * <code><pre>Set set = Collections.unmodifiableSet(...);
	 *  Method method = ClassUtils.getPublicMethod(set.getClass(), "isEmpty",  new Class[0]);
	 *  Object result = method.invoke(set, new Object[]);</pre></code>
	 * </p>
	 *
	 * @param cls
	 *            the class to check, not null
	 * @param methodName
	 *            the name of the method
	 * @param parameterTypes
	 *            the list of parameters
	 * @return the method
	 * @throws NullPointerException
	 *             if the class is null
	 * @throws SecurityException
	 *             if a a security violation occured
	 * @throws NoSuchMethodException
	 *             if the method is not found in the given class or if the
	 *             metothod doen't conform with the requirements
	 */
	public static Method getPublicMethod(Class<?> cls, String methodName,
			Class<?> parameterTypes[]) throws SecurityException,
			NoSuchMethodException {

		Method declaredMethod = cls.getMethod(methodName, parameterTypes);
		if (Modifier
				.isPublic(declaredMethod.getDeclaringClass().getModifiers())) {
			return declaredMethod;
		}

		List<Class<?>> candidateClasses = new ArrayList<Class<?>>();
		candidateClasses.addAll(getAllInterfaces(cls));
		candidateClasses.addAll(getAllSuperclasses(cls));

		for (Iterator<?> it = candidateClasses.iterator(); it.hasNext();) {
			Class<?> candidateClass = (Class<?>) it.next();
			if (!Modifier.isPublic(candidateClass.getModifiers())) {
				continue;
			}
			Method candidateMethod;
			try {
				candidateMethod = candidateClass.getMethod(methodName,
						parameterTypes);
			} catch (NoSuchMethodException ex) {
				continue;
			}
			if (Modifier.isPublic(candidateMethod.getDeclaringClass()
					.getModifiers())) {
				return candidateMethod;
			}
		}

		throw new NoSuchMethodException("Can't find a public method for "
				+ methodName + " " + ArrayUtils.toString(parameterTypes));
	}

	/**
	 * Return the qualified name of the given method, consisting of fully
	 * qualified interface/class name + "." + method name.
	 *
	 * @param method
	 *            the method
	 * @return the qualified name of the method
	 */
	public static String getQualifiedMethodName(Method method) {
		Assert.notNull(method, "Method must not be null");
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

	/**
	 * Return the qualified name of the given class: usually simply the class
	 * name, but component type class name + "[]" for arrays.
	 *
	 * @param clazz
	 *            the class
	 * @return the qualified name of the class
	 */
	public static String getQualifiedName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		} else {
			return clazz.getName();
		}
	}

	/**
	 * Build a nice qualified name for an array: component type class name +
	 * "[]".
	 *
	 * @param clazz
	 *            the array class
	 * @return a qualified name for the array class
	 */
	private static String getQualifiedNameForArray(Class<?> clazz) {
		StringBuilder result = new StringBuilder();
		while (clazz.isArray()) {
			clazz = clazz.getComponentType();
			result.append(ClassUtils.ARRAY_SUFFIX);
		}
		result.insert(0, clazz.getName());
		return result.toString();
	}

	/**
	 * <p>
	 * Gets the canonical name minus the package name from a <code>Class</code>.
	 * </p>
	 *
	 * @param cls
	 *            the class to get the short name for.
	 * @return the canonical name without the package name or an empty string
	 * @since 2.4
	 */
	public static String getShortCanonicalName(Class<?> cls) {
		if (cls == null) {
			return StringUtils.EMPTY;
		}
		return getShortCanonicalName(cls.getName());
	}

	// Short canonical name
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Gets the canonical name minus the package name for an <code>Object</code>
	 * .
	 * </p>
	 *
	 * @param object
	 *            the class to get the short name for, may be null
	 * @param valueIfNull
	 *            the value to return if null
	 * @return the canonical name of the object without the package name, or the
	 *         null value
	 * @since 2.4
	 */
	public static String getShortCanonicalName(Object object, String valueIfNull) {
		if (object == null) {
			return valueIfNull;
		}
		return getShortCanonicalName(object.getClass().getName());
	}

	/**
	 * <p>
	 * Gets the canonical name minus the package name from a String.
	 * </p>
	 *
	 * <p>
	 * The string passed in is assumed to be a canonical name - it is not
	 * checked.
	 * </p>
	 *
	 * @param canonicalName
	 *            the class name to get the short name for
	 * @return the canonical name of the class without the package name or an
	 *         empty string
	 * @since 2.4
	 */
	public static String getShortCanonicalName(String canonicalName) {
		return ClassUtils.getShortClassName(getCanonicalName(canonicalName));
	}

	/**
	 * <p>
	 * Gets the class name minus the package name from a <code>Class</code>.
	 * </p>
	 *
	 * @param cls
	 *            the class to get the short name for.
	 * @return the class name without the package name or an empty string
	 */
	public static String getShortClassName(Class<?> cls) {
		if (cls == null) {
			return StringUtils.EMPTY;
		}
		return getShortClassName(cls.getName());
	}

	// Short class name
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Gets the class name minus the package name for an <code>Object</code>.
	 * </p>
	 *
	 * @param object
	 *            the class to get the short name for, may be null
	 * @param valueIfNull
	 *            the value to return if null
	 * @return the class name of the object without the package name, or the
	 *         null value
	 */
	public static String getShortClassName(Object object, String valueIfNull) {
		if (object == null) {
			return valueIfNull;
		}
		return getShortClassName(object.getClass());
	}

	/**
	 * <p>
	 * Gets the class name minus the package name from a String.
	 * </p>
	 *
	 * <p>
	 * The string passed in is assumed to be a class name - it is not checked.
	 * </p>
	 *
	 * @param className
	 *            the className to get the short name for
	 * @return the class name of the class without the package name or an empty
	 *         string
	 */
	public static String getShortClassName(String className) {
		if (className == null) {
			return StringUtils.EMPTY;
		}
		if (className.length() == 0) {
			return StringUtils.EMPTY;
		}

		StrBuilder arrayPrefix = new StrBuilder();

		// Handle array encoding
		if (className.startsWith("[")) {
			while (className.charAt(0) == '[') {
				className = className.substring(1);
				arrayPrefix.append("[]");
			}
			// Strip Object type encoding
			if (className.charAt(0) == 'L'
					&& className.charAt(className.length() - 1) == ';') {
				className = className.substring(1, className.length() - 1);
			}
		}

		if (reverseAbbreviationMap.containsKey(className)) {
			className = (String) reverseAbbreviationMap.get(className);
		}

		int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
		int innerIdx = className.indexOf(INNER_CLASS_SEPARATOR_CHAR,
				lastDotIdx == -1 ? 0 : lastDotIdx + 1);
		String out = className.substring(lastDotIdx + 1);
		if (innerIdx != -1) {
			out = out.replace(INNER_CLASS_SEPARATOR_CHAR,
					PACKAGE_SEPARATOR_CHAR);
		}
		return out + arrayPrefix;
	}

	/**
	 * Get the class name without the qualified package name.
	 *
	 * @param clazz
	 *            the class to get the short name for
	 * @return the class name of the class without the package name
	 */
	public static String getShortName(Class<?> clazz) {
		return getShortName(getQualifiedName(clazz));
	}

	/**
	 * Get the class name without the qualified package name.
	 *
	 * @param className
	 *            the className to get the short name for
	 * @return the class name of the class without the package name
	 * @throws IllegalArgumentException
	 *             if the className is empty
	 */
	public static String getShortName(String className) {
		Assert.hasLength(className, "Class name must not be empty");
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
		if (nameEndIndex == -1) {
			nameEndIndex = className.length();
		}
		String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
		shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
		return shortName;
	}

	/**
	 * Return the short string name of a Java class in uncapitalized JavaBeans
	 * property format. Strips the outer class name in case of an inner class.
	 *
	 * @param clazz
	 *            the class
	 * @return the short name rendered in a standard JavaBeans property format
	 * @see Introspector#decapitalize(String)
	 */
	public static String getShortNameAsProperty(Class<?> clazz) {
		String shortName = ClassUtils.getShortName(clazz);
		int dotIndex = shortName.lastIndexOf('.');
		shortName = (dotIndex != -1 ? shortName.substring(dotIndex + 1)
				: shortName);
		return Introspector.decapitalize(shortName);
	}

	/**
	 * Return a public static method of a class.
	 *
	 * @param methodName
	 *            the static method name
	 * @param clazz
	 *            the class which defines the method
	 * @param args
	 *            the parameter types to the method
	 * @return the static method, or {@code null} if no static method was found
	 * @throws IllegalArgumentException
	 *             if the method name is blank or the clazz is null
	 */
	public static Method getStaticMethod(Class<?> clazz, String methodName,
			Class<?>... args) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		try {
			Method method = clazz.getMethod(methodName, args);
			return Modifier.isStatic(method.getModifiers()) ? method : null;
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	/**
	 * Return the user-defined class for the given class: usually simply the
	 * given class, but the original class in case of a CGLIB-generated
	 * subclass.
	 *
	 * @param clazz
	 *            the class to check
	 * @return the user-defined class
	 */
	public static Class<?> getUserClass(Class<?> clazz) {
		if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	/**
	 * Return the user-defined class for the given instance: usually simply the
	 * class of the given instance, but the original class in case of a
	 * CGLIB-generated subclass.
	 *
	 * @param instance
	 *            the instance to check
	 * @return the user-defined class
	 */
	public static Class<?> getUserClass(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getUserClass(instance.getClass());
	}

	/**
	 * Does the given class or one of its superclasses at least have one or more
	 * methods with the supplied name (with any argument types)? Includes
	 * non-public methods.
	 *
	 * @param clazz
	 *            the clazz to check
	 * @param methodName
	 *            the name of the method
	 * @return whether there is at least one method with the given name
	 */
	public static boolean hasAtLeastOneMethodWithName(Class<?> clazz,
			String methodName) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (method.getName().equals(methodName)) {
				return true;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for (Class<?> ifc : ifcs) {
			if (hasAtLeastOneMethodWithName(ifc, methodName)) {
				return true;
			}
		}
		return (clazz.getSuperclass() != null && hasAtLeastOneMethodWithName(
				clazz.getSuperclass(), methodName));
	}

	/**
	 * Determine whether the given class has a public constructor with the given
	 * signature.
	 * <p>
	 * Essentially translates {@code NoSuchMethodException} to "false".
	 *
	 * @param clazz
	 *            the clazz to analyze
	 * @param paramTypes
	 *            the parameter types of the method
	 * @return whether the class has a corresponding constructor
	 * @see Class#getMethod
	 */
	public static boolean hasConstructor(Class<?> clazz, Class<?>... paramTypes) {
		return (getConstructorIfAvailable(clazz, paramTypes) != null);
	}

	/**
	 * Determine whether the given class has a public method with the given
	 * signature.
	 * <p>
	 * Essentially translates {@code NoSuchMethodException} to "false".
	 *
	 * @param clazz
	 *            the clazz to analyze
	 * @param methodName
	 *            the name of the method
	 * @param paramTypes
	 *            the parameter types of the method
	 * @return whether the class has a corresponding method
	 * @see Class#getMethod
	 */
	public static boolean hasMethod(Class<?> clazz, String methodName,
			Class<?>... paramTypes) {
		return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
	}

	/**
	 * <p>
	 * Checks if one <code>Class</code> can be assigned to a variable of another
	 * <code>Class</code>.
	 * </p>
	 *
	 * <p>
	 * Unlike the {@link Class#isAssignableFrom(Class)} method, this
	 * method takes into account widenings of primitive classes and
	 * <code>null</code>s.
	 * </p>
	 *
	 * <p>
	 * Primitive widenings allow an int to be assigned to a long, float or
	 * double. This method returns the correct result for these cases.
	 * </p>
	 *
	 * <p>
	 * <code>Null</code> may be assigned to any reference type. This method will
	 * return <code>true</code> if <code>null</code> is passed in and the
	 * toClass is non-primitive.
	 * </p>
	 *
	 * <p>
	 * Specifically, this method tests whether the type represented by the
	 * specified <code>Class</code> parameter can be converted to the type
	 * represented by this <code>Class</code> object via an identity conversion
	 * widening primitive or widening reference conversion. See
	 * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em>
	 * , sections 5.1.1, 5.1.2 and 5.1.4 for details.
	 * </p>
	 *
	 * @param cls
	 *            the Class to check, may be null
	 * @param toClass
	 *            the Class to try to assign into, returns false if null
	 * @return <code>true</code> if assignment possible
	 */
	public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
		return isAssignable(cls, toClass, false);
	}

	/**
	 * <p>
	 * Checks if one <code>Class</code> can be assigned to a variable of another
	 * <code>Class</code>.
	 * </p>
	 *
	 * <p>
	 * Unlike the {@link Class#isAssignableFrom(Class)} method, this
	 * method takes into account widenings of primitive classes and
	 * <code>null</code>s.
	 * </p>
	 *
	 * <p>
	 * Primitive widenings allow an int to be assigned to a long, float or
	 * double. This method returns the correct result for these cases.
	 * </p>
	 *
	 * <p>
	 * <code>Null</code> may be assigned to any reference type. This method will
	 * return <code>true</code> if <code>null</code> is passed in and the
	 * toClass is non-primitive.
	 * </p>
	 *
	 * <p>
	 * Specifically, this method tests whether the type represented by the
	 * specified <code>Class</code> parameter can be converted to the type
	 * represented by this <code>Class</code> object via an identity conversion
	 * widening primitive or widening reference conversion. See
	 * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em>
	 * , sections 5.1.1, 5.1.2 and 5.1.4 for details.
	 * </p>
	 *
	 * @param cls
	 *            the Class to check, may be null
	 * @param toClass
	 *            the Class to try to assign into, returns false if null
	 * @param autoboxing
	 *            whether to use implicit autoboxing/unboxing between primitives
	 *            and wrappers
	 * @return <code>true</code> if assignment possible
	 * @since 2.5
	 */
	public static boolean isAssignable(Class<?> cls, Class<?> toClass,
			boolean autoboxing) {
		if (toClass == null) {
			return false;
		}
		// have to check for null, as isAssignableFrom doesn't
		if (cls == null) {
			return !(toClass.isPrimitive());
		}
		// autoboxing:
		if (autoboxing) {
			if (cls.isPrimitive() && !toClass.isPrimitive()) {
				cls = primitiveToWrapper(cls);
				if (cls == null) {
					return false;
				}
			}
			if (toClass.isPrimitive() && !cls.isPrimitive()) {
				cls = wrapperToPrimitive(cls);
				if (cls == null) {
					return false;
				}
			}
		}
		if (cls.equals(toClass)) {
			return true;
		}
		if (cls.isPrimitive()) {
			if (toClass.isPrimitive() == false) {
				return false;
			}
			if (Integer.TYPE.equals(cls)) {
				return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
						|| Double.TYPE.equals(toClass);
			}
			if (Long.TYPE.equals(cls)) {
				return Float.TYPE.equals(toClass)
						|| Double.TYPE.equals(toClass);
			}
			if (Boolean.TYPE.equals(cls)) {
				return false;
			}
			if (Double.TYPE.equals(cls)) {
				return false;
			}
			if (Float.TYPE.equals(cls)) {
				return Double.TYPE.equals(toClass);
			}
			if (Character.TYPE.equals(cls)) {
				return Integer.TYPE.equals(toClass)
						|| Long.TYPE.equals(toClass)
						|| Float.TYPE.equals(toClass)
						|| Double.TYPE.equals(toClass);
			}
			if (Short.TYPE.equals(cls)) {
				return Integer.TYPE.equals(toClass)
						|| Long.TYPE.equals(toClass)
						|| Float.TYPE.equals(toClass)
						|| Double.TYPE.equals(toClass);
			}
			if (Byte.TYPE.equals(cls)) {
				return Short.TYPE.equals(toClass)
						|| Integer.TYPE.equals(toClass)
						|| Long.TYPE.equals(toClass)
						|| Float.TYPE.equals(toClass)
						|| Double.TYPE.equals(toClass);
			}
			// should never get here
			return false;
		}
		return toClass.isAssignableFrom(cls);
	}

	// Is assignable
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if an array of Classes can be assigned to another array of
	 * Classes.
	 * </p>
	 *
	 * <p>
	 * This method calls {@link #isAssignable(Class, Class) isAssignable} for
	 * each Class pair in the input arrays. It can be used to check if a set of
	 * arguments (the first parameter) are suitably compatible with a set of
	 * method parameter types (the second parameter).
	 * </p>
	 *
	 * <p>
	 * Unlike the {@link Class#isAssignableFrom(Class)} method, this
	 * method takes into account widenings of primitive classes and
	 * <code>null</code>s.
	 * </p>
	 *
	 * <p>
	 * Primitive widenings allow an int to be assigned to a <code>long</code>,
	 * <code>float</code> or <code>double</code>. This method returns the
	 * correct result for these cases.
	 * </p>
	 *
	 * <p>
	 * <code>Null</code> may be assigned to any reference type. This method will
	 * return <code>true</code> if <code>null</code> is passed in and the
	 * toClass is non-primitive.
	 * </p>
	 *
	 * <p>
	 * Specifically, this method tests whether the type represented by the
	 * specified <code>Class</code> parameter can be converted to the type
	 * represented by this <code>Class</code> object via an identity conversion
	 * widening primitive or widening reference conversion. See
	 * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em>
	 * , sections 5.1.1, 5.1.2 and 5.1.4 for details.
	 * </p>
	 *
	 * @param classArray
	 *            the array of Classes to check, may be <code>null</code>
	 * @param toClassArray
	 *            the array of Classes to try to assign into, may be
	 *            <code>null</code>
	 * @return <code>true</code> if assignment possible
	 */
	public static boolean isAssignable(Class<?>[] classArray,
			Class<?>[] toClassArray) {
		return isAssignable(classArray, toClassArray, false);
	}

	/**
	 * <p>
	 * Checks if an array of Classes can be assigned to another array of
	 * Classes.
	 * </p>
	 *
	 * <p>
	 * This method calls {@link #isAssignable(Class, Class) isAssignable} for
	 * each Class pair in the input arrays. It can be used to check if a set of
	 * arguments (the first parameter) are suitably compatible with a set of
	 * method parameter types (the second parameter).
	 * </p>
	 *
	 * <p>
	 * Unlike the {@link Class#isAssignableFrom(Class)} method, this
	 * method takes into account widenings of primitive classes and
	 * <code>null</code>s.
	 * </p>
	 *
	 * <p>
	 * Primitive widenings allow an int to be assigned to a <code>long</code>,
	 * <code>float</code> or <code>double</code>. This method returns the
	 * correct result for these cases.
	 * </p>
	 *
	 * <p>
	 * <code>Null</code> may be assigned to any reference type. This method will
	 * return <code>true</code> if <code>null</code> is passed in and the
	 * toClass is non-primitive.
	 * </p>
	 *
	 * <p>
	 * Specifically, this method tests whether the type represented by the
	 * specified <code>Class</code> parameter can be converted to the type
	 * represented by this <code>Class</code> object via an identity conversion
	 * widening primitive or widening reference conversion. See
	 * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em>
	 * , sections 5.1.1, 5.1.2 and 5.1.4 for details.
	 * </p>
	 *
	 * @param classArray
	 *            the array of Classes to check, may be <code>null</code>
	 * @param toClassArray
	 *            the array of Classes to try to assign into, may be
	 *            <code>null</code>
	 * @param autoboxing
	 *            whether to use implicit autoboxing/unboxing between primitives
	 *            and wrappers
	 * @return <code>true</code> if assignment possible
	 * @since 2.5
	 */
	public static boolean isAssignable(Class<?>[] classArray,
			Class<?>[] toClassArray, boolean autoboxing) {
		if (ArrayUtils.isSameLength(classArray, toClassArray) == false) {
			return false;
		}
		if (classArray == null) {
			classArray = ArrayUtils.EMPTY_CLASS_ARRAY;
		}
		if (toClassArray == null) {
			toClassArray = ArrayUtils.EMPTY_CLASS_ARRAY;
		}
		for (int i = 0; i < classArray.length; i++) {
			if (isAssignable(classArray[i], toClassArray[i], autoboxing) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine if the given type is assignable from the given value, assuming
	 * setting by reflection. Considers primitive wrapper classes as assignable
	 * to the corresponding primitive types.
	 * 
	 * @param type
	 *            the target type
	 * @param value
	 *            the value that should be assigned to the type
	 * @return if the type is assignable from the value
	 */
	public static boolean isAssignableValue(Class<?> type, Object value) {
		Assert.notNull(type, "Type must not be null");
		return (value != null ? isAssignable(type, value.getClass()) : !type
				.isPrimitive());
	}

	/**
	 * Check whether the given class is cache-safe in the given context, i.e.
	 * whether it is loaded by the given ClassLoader or a parent of it.
	 * 
	 * @param clazz
	 *            the class to analyze
	 * @param classLoader
	 *            the ClassLoader to potentially cache metadata in
	 */
	public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader) {
		Assert.notNull(clazz, "Class must not be null");
		ClassLoader target = clazz.getClassLoader();
		if (target == null) {
			return true;
		}
		ClassLoader cur = classLoader;
		if (cur == target) {
			return true;
		}
		while (cur != null) {
			cur = cur.getParent();
			if (cur == target) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given object is a CGLIB proxy.
	 * 
	 * @param object
	 *            the object to check
	 */
	public static boolean isCglibProxy(Object object) {
		return ClassUtils.isCglibProxyClass(object.getClass());
	}

	/**
	 * Check whether the specified class is a CGLIB-generated class.
	 * 
	 * @param clazz
	 *            the class to check
	 */
	public static boolean isCglibProxyClass(Class<?> clazz) {
		return (clazz != null && isCglibProxyClassName(clazz.getName()));
	}

	/**
	 * Check whether the specified class name is a CGLIB-generated class.
	 * 
	 * @param className
	 *            the class name to check
	 */
	public static boolean isCglibProxyClassName(String className) {
		return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
	}

	private static boolean isGroovyObjectMethod(Method method) {
		return method.getDeclaringClass().getName()
				.equals("groovy.lang.GroovyObject");
	}

	// Inner class
	// ----------------------------------------------------------------------
	/**
	 * <p>
	 * Is the specified class an inner class or static nested class.
	 * </p>
	 *
	 * @param cls
	 *            the class to check, may be null
	 * @return <code>true</code> if the class is an inner or static nested
	 *         class, false if not or <code>null</code>
	 */
	public static boolean isInnerClass(Class<?> cls) {
		if (cls == null) {
			return false;
		}
		return cls.getName().indexOf(INNER_CLASS_SEPARATOR_CHAR) >= 0;
	}

	/**
	 * Determine whether the given method is overridable in the given target
	 * class.
	 * 
	 * @param method
	 *            the method to check
	 * @param targetClass
	 *            the target class to check against
	 */
	private static boolean isOverridable(Method method, Class<?> targetClass) {
		if (Modifier.isPrivate(method.getModifiers())) {
			return false;
		}
		if (Modifier.isPublic(method.getModifiers())
				|| Modifier.isProtected(method.getModifiers())) {
			return true;
		}
		return getPackageName(method.getDeclaringClass()).equals(
				getPackageName(targetClass));
	}

	/**
	 * Determine whether the {@link Class} identified by the supplied name is
	 * present and can be loaded. Will return {@code false} if either the class
	 * or one of its dependencies is not present or cannot be loaded.
	 * 
	 * @param className
	 *            the name of the class to check
	 * @param classLoader
	 *            the class loader to use (may be {@code null}, which indicates
	 *            the default class loader)
	 * @return whether the specified class is present
	 */
	public static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			forName(className, classLoader);
			return true;
		} catch (Throwable ex) {
			// Class or one of its dependencies is not present...
			return false;
		}
	}

	/**
	 * Check if the given class represents an array of primitives, i.e. boolean,
	 * byte, char, short, int, long, float, or double.
	 * 
	 * @param clazz
	 *            the class to check
	 * @return whether the given class is a primitive array class
	 */
	public static boolean isPrimitiveArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isArray() && clazz.getComponentType().isPrimitive());
	}

	/**
	 * Check if the given class represents a primitive (i.e. boolean, byte,
	 * char, short, int, long, float, or double) or a primitive wrapper (i.e.
	 * Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
	 * 
	 * @param clazz
	 *            the class to check
	 * @return whether the given class is a primitive or primitive wrapper class
	 */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}

	/**
	 * Check if the given class represents a primitive wrapper, i.e. Boolean,
	 * Byte, Character, Short, Integer, Long, Float, or Double.
	 * 
	 * @param clazz
	 *            the class to check
	 * @return whether the given class is a primitive wrapper class
	 */
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return primitiveWrapperTypeMap.containsKey(clazz);
	}

	/**
	 * Check if the given class represents an array of primitive wrappers, i.e.
	 * Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
	 * 
	 * @param clazz
	 *            the class to check
	 * @return whether the given class is a primitive wrapper array class
	 */
	public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
	}

	/**
	 * Determine whether the given method is declared by the user or at least
	 * pointing to a user-declared method.
	 * <p>
	 * Checks {@link Method#isSynthetic()} (for implementation methods) as well
	 * as the {@code GroovyObject} interface (for interface methods; on an
	 * implementation class, implementations of the {@code GroovyObject} methods
	 * will be marked as synthetic anyway). Note that, despite being synthetic,
	 * bridge methods ({@link Method#isBridge()}) are considered as user-level
	 * methods since they are eventually pointing to a user-declared generic
	 * method.
	 * 
	 * @param method
	 *            the method to check
	 * @return {@code true} if the method can be considered as user-declared;
	 *         [@code false} otherwise
	 */
	public static boolean isUserLevelMethod(Method method) {
		Assert.notNull(method, "Method must not be null");
		return (method.isBridge() || (!method.isSynthetic() && !isGroovyObjectMethod(method)));
	}

	/**
	 * Check whether the given class is visible in the given ClassLoader.
	 * 
	 * @param clazz
	 *            the class to check (typically an interface)
	 * @param classLoader
	 *            the ClassLoader to check against (may be {@code null}, in
	 *            which case this method will always return {@code true})
	 */
	public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
		if (classLoader == null) {
			return true;
		}
		try {
			Class<?> actualClass = classLoader.loadClass(clazz.getName());
			return (clazz == actualClass);
			// Else: different interface class found...
		} catch (ClassNotFoundException ex) {
			// No interface class found...
			return false;
		}
	}

	/**
	 * 基本类型、基本类型的包装类型、字符串类型，返回true，其它false
	 * @param clz
	 * @return
	 */
	public static boolean isWrapClass(Class<?> clz) {
		try {
			if (clz.equals(String.class)) {
				return true;
			}
			if (clz.isPrimitive()){
				return true;
			}
			return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check whether the given class matches the user-specified type name.
	 * 
	 * @param clazz
	 *            the class to check
	 * @param typeName
	 *            the type name to match
	 */
	public static boolean matchesTypeName(Class<?> clazz, String typeName) {
		return (typeName != null && (typeName.equals(clazz.getName())
				|| typeName.equals(clazz.getSimpleName()) || (clazz.isArray() && typeName
				.equals(getQualifiedNameForArray(clazz)))));
	}

	/**
	 * Override the thread context ClassLoader with the environment's bean
	 * ClassLoader if necessary, i.e. if the bean ClassLoader is not equivalent
	 * to the thread context ClassLoader already.
	 * 
	 * @param classLoaderToUse
	 *            the actual ClassLoader to use for the thread context
	 * @return the original thread context ClassLoader, or {@code null} if not
	 *         overridden
	 */
	public static ClassLoader overrideThreadContextClassLoader(
			ClassLoader classLoaderToUse) {
		Thread currentThread = Thread.currentThread();
		ClassLoader threadContextClassLoader = currentThread
				.getContextClassLoader();
		if (classLoaderToUse != null
				&& !classLoaderToUse.equals(threadContextClassLoader)) {
			currentThread.setContextClassLoader(classLoaderToUse);
			return threadContextClassLoader;
		} else {
			return null;
		}
	}

	/**
	 * <p>
	 * Converts the specified array of primitive Class objects to an array of
	 * its corresponding wrapper Class objects.
	 * </p>
	 *
	 * @param classes
	 *            the class array to convert, may be null or empty
	 * @return an array which contains for each given class, the wrapper class
	 *         or the original class if class is not a primitive.
	 *         <code>null</code> if null input. Empty array if an empty array
	 *         passed in.
	 * @since 2.1
	 */
	public static Class<?>[] primitivesToWrappers(Class<?>[] classes) {
		if (classes == null) {
			return null;
		}

		if (classes.length == 0) {
			return classes;
		}

		Class<?>[] convertedClasses = new Class[classes.length];
		for (int i = 0; i < classes.length; i++) {
			convertedClasses[i] = primitiveToWrapper(classes[i]);
		}
		return convertedClasses;
	}

	/**
	 * <p>
	 * Converts the specified primitive Class object to its corresponding
	 * wrapper Class object.
	 * </p>
	 *
	 * <p>
	 * NOTE: From v2.2, this method handles <code>Void.TYPE</code>, returning
	 * <code>Void.TYPE</code>.
	 * </p>
	 *
	 * @param cls
	 *            the class to convert, may be null
	 * @return the wrapper class for <code>cls</code> or <code>cls</code> if
	 *         <code>cls</code> is not a primitive. <code>null</code> if null
	 *         input.
	 * @since 2.1
	 */
	public static Class<?> primitiveToWrapper(Class<?> cls) {
		Class<?> convertedClass = cls;
		if (cls != null && cls.isPrimitive()) {
			convertedClass = (Class<?>) primitiveWrapperMap.get(cls);
		}
		return convertedClass;
	}

	/**
	 * Register the given common classes with the ClassUtils cache.
	 */
	private static void registerCommonClasses(Class<?>... commonClasses) {
		for (Class<?> clazz : commonClasses) {
			commonClassCache.put(clazz.getName(), clazz);
		}
	}

	/**
	 * Resolve the given class name into a Class instance. Supports primitives
	 * (like "int") and array class names (like "String[]").
	 * <p>
	 * This is effectively equivalent to the {@code forName} method with the
	 * same arguments, with the only difference being the exceptions thrown in
	 * case of class loading failure.
	 * 
	 * @param className
	 *            the name of the Class
	 * @param classLoader
	 *            the class loader to use (may be {@code null}, which indicates
	 *            the default class loader)
	 * @return Class instance for the supplied name
	 * @throws IllegalArgumentException
	 *             if the class name was not resolvable (that is, the class
	 *             could not be found or the class file could not be loaded)
	 * @see #forName(String, ClassLoader)
	 */
	public static Class<?> resolveClassName(String className,
			ClassLoader classLoader) throws IllegalArgumentException {
		try {
			return forName(className, classLoader);
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Cannot find class ["
					+ className + "]", ex);
		} catch (LinkageError ex) {
			throw new IllegalArgumentException("Error loading class ["
					+ className
					+ "]: problem with class file or dependent class.", ex);
		}
	}

	/**
	 * Resolve the given class name as primitive class, if appropriate,
	 * according to the JVM's naming rules for primitive classes.
	 * <p>
	 * Also supports the JVM's internal class names for primitive arrays. Does
	 * <i>not</i> support the "[]" suffix notation for primitive arrays; this is
	 * only supported by {@link #forName(String, ClassLoader)}.
	 * 
	 * @param name
	 *            the name of the potentially primitive class
	 * @return the primitive class, or {@code null} if the name does not denote
	 *         a primitive class or primitive array class
	 */
	public static Class<?> resolvePrimitiveClassName(String name) {
		Class<?> result = null;
		// Most class names will be quite long, considering that they
		// SHOULD sit in a package, so a length check is worthwhile.
		if (name != null && name.length() <= 8) {
			// Could be a primitive - likely.
			result = primitiveTypeNameMap.get(name);
		}
		return result;
	}

	/**
	 * Resolve the given class if it is a primitive class, returning the
	 * corresponding primitive wrapper type instead.
	 * 
	 * @param clazz
	 *            the class to check
	 * @return the original class, or a primitive wrapper for the original
	 *         primitive type
	 */
	public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap
				.get(clazz) : clazz);
	}

	// ----------------------------------------------------------------------
	/**
	 * Converts a class name to a JLS style class name.
	 *
	 * @param className
	 *            the class name
	 * @return the converted name
	 */
	private static String toCanonicalName(String className) {
		className = StringUtils.deleteWhitespace(className);
		if (className == null) {
			throw new NullArgumentException("className");
		} else if (className.endsWith("[]")) {
			StrBuilder classNameBuffer = new StrBuilder();
			while (className.endsWith("[]")) {
				className = className.substring(0, className.length() - 2);
				classNameBuffer.append("[");
			}
			String abbreviation = (String) abbreviationMap.get(className);
			if (abbreviation != null) {
				classNameBuffer.append(abbreviation);
			} else {
				classNameBuffer.append("L").append(className).append(";");
			}
			className = classNameBuffer.toString();
		}
		return className;
	}

	/**
	 * <p>
	 * Converts an array of <code>Object</code> in to an array of
	 * <code>Class</code> objects. If any of these objects is null, a null
	 * element will be inserted into the array.
	 * </p>
	 *
	 * <p>
	 * This method returns <code>null</code> for a <code>null</code> input
	 * array.
	 * </p>
	 *
	 * @param array
	 *            an <code>Object</code> array
	 * @return a <code>Class</code> array, <code>null</code> if null array input
	 * @since 2.4
	 */
	public static Class<?>[] toClass(Object[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return ArrayUtils.EMPTY_CLASS_ARRAY;
		}
		Class<?>[] classes = new Class[array.length];
		for (int i = 0; i < array.length; i++) {
			classes[i] = array[i] == null ? null : array[i].getClass();
		}
		return classes;
	}

	/**
	 * Copy the given Collection into a Class array. The Collection must contain
	 * Class elements only.
	 * 
	 * @param collection
	 *            the Collection to copy
	 * @return the Class array ({@code null} if the passed-in Collection was
	 *         {@code null})
	 */
	public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new Class<?>[collection.size()]);
	}

	/**
	 * <p>
	 * Converts the specified array of wrapper Class objects to an array of its
	 * corresponding primitive Class objects.
	 * </p>
	 *
	 * <p>
	 * This method invokes <code>wrapperToPrimitive()</code> for each element of
	 * the passed in array.
	 * </p>
	 *
	 * @param classes
	 *            the class array to convert, may be null or empty
	 * @return an array which contains for each given class, the primitive class
	 *         or <b>null</b> if the original class is not a wrapper class.
	 *         <code>null</code> if null input. Empty array if an empty array
	 *         passed in.
	 * @see #wrapperToPrimitive(Class)
	 * @since 2.4
	 */
	public static Class<?>[] wrappersToPrimitives(Class<?>[] classes) {
		if (classes == null) {
			return null;
		}

		if (classes.length == 0) {
			return classes;
		}

		Class<?>[] convertedClasses = new Class[classes.length];
		for (int i = 0; i < classes.length; i++) {
			convertedClasses[i] = wrapperToPrimitive(classes[i]);
		}
		return convertedClasses;
	}

	/**
	 * <p>
	 * Converts the specified wrapper class to its corresponding primitive
	 * class.
	 * </p>
	 *
	 * <p>
	 * This method is the counter part of <code>primitiveToWrapper()</code>. If
	 * the passed in class is a wrapper class for a primitive type, this
	 * primitive type will be returned (e.g. <code>Integer.TYPE</code> for
	 * <code>Integer.class</code>). For other classes, or if the parameter is
	 * <b>null</b>, the return value is <b>null</b>.
	 * </p>
	 *
	 * @param cls
	 *            the class to convert, may be <b>null</b>
	 * @return the corresponding primitive type if <code>cls</code> is a wrapper
	 *         class, <b>null</b> otherwise
	 * @see #primitiveToWrapper(Class)
	 * @since 2.4
	 */
	public static Class<?> wrapperToPrimitive(Class<?> cls) {
		return (Class<?>) wrapperPrimitiveMap.get(cls);
	}

	/**
	 * <p>
	 * ClassUtils instances should NOT be constructed in standard programming.
	 * Instead, the class should be used as
	 * <code>ClassUtils.getShortClassName(cls)</code>.
	 * </p>
	 *
	 * <p>
	 * This constructor is public to permit tools that require a JavaBean
	 * instance to operate.
	 * </p>
	 */
	public ClassUtils() {
		super();
	}

}

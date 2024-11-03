/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package java.lang;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import sun.misc.CompoundEnumeration;
import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.misc.VM;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;
import sun.security.util.SecurityConstants;

/**
 * A class loader is an object that is responsible for loading classes. The
 * class <tt>ClassLoader</tt> is an abstract class.  Given the <a
 * href="#name">binary name</a> of a class, a class loader should attempt to
 * locate or generate data that constitutes a definition for the class.  A
 * typical strategy is to transform the name into a file name and then read a
 * "class file" of that name from a file system.
 *
 * <p> Every {@link Class <tt>Class</tt>} object contains a {@link
 * Class#getClassLoader() reference} to the <tt>ClassLoader</tt> that defined
 * it.
 *
 * <p> <tt>Class</tt> objects for array classes are not created by class
 * loaders, but are created automatically as required by the Java runtime.
 * The class loader for an array class, as returned by {@link
 * Class#getClassLoader()} is the same as the class loader for its element
 * type; if the element type is a primitive type, then the array class has no
 * class loader.
 *
 * <p> Applications implement subclasses of <tt>ClassLoader</tt> in order to
 * extend the manner in which the Java virtual machine dynamically loads
 * classes.
 *
 * <p> Class loaders may typically be used by security managers to indicate
 * security domains.
 *
 * <p> The <tt>ClassLoader</tt> class uses a delegation model to search for
 * classes and resources.  Each instance of <tt>ClassLoader</tt> has an
 * associated parent class loader.  When requested to find a class or
 * resource, a <tt>ClassLoader</tt> instance will delegate the search for the
 * class or resource to its parent class loader before attempting to find the
 * class or resource itself.  The virtual machine's built-in class loader,
 * called the "bootstrap class loader", does not itself have a parent but may
 * serve as the parent of a <tt>ClassLoader</tt> instance.
 *
 * <p> Class loaders that support concurrent loading of classes are known as
 * <em>parallel capable</em> class loaders and are required to register
 * themselves at their class initialization time by invoking the
 * {@link
 * #registerAsParallelCapable <tt>ClassLoader.registerAsParallelCapable</tt>}
 * method. Note that the <tt>ClassLoader</tt> class is registered as parallel
 * capable by default. However, its subclasses still need to register themselves
 * if they are parallel capable. <br>
 * In environments in which the delegation model is not strictly
 * hierarchical, class loaders need to be parallel capable, otherwise class
 * loading can lead to deadlocks because the loader lock is held for the
 * duration of the class loading process (see {@link #loadClass
 * <tt>loadClass</tt>} methods).
 *
 * <p> Normally, the Java virtual machine loads classes from the local file
 * system in a platform-dependent manner.  For example, on UNIX systems, the
 * virtual machine loads classes from the directory defined by the
 * <tt>CLASSPATH</tt> environment variable.
 *
 * <p> However, some classes may not originate from a file; they may originate
 * from other sources, such as the network, or they could be constructed by an
 * application.  The method {@link #defineClass(String, byte[], int, int)
 * <tt>defineClass</tt>} converts an array of bytes into an instance of class
 * <tt>Class</tt>. Instances of this newly defined class can be created using
 * {@link Class#newInstance <tt>Class.newInstance</tt>}.
 *
 * <p> The methods and constructors of objects created by a class loader may
 * reference other classes.  To determine the class(es) referred to, the Java
 * virtual machine invokes the {@link #loadClass <tt>loadClass</tt>} method of
 * the class loader that originally created the class.
 *
 * <p> For example, an application could create a network class loader to
 * download class files from a server.  Sample code might look like:
 *
 * <blockquote><pre>
 *   ClassLoader loader&nbsp;= new NetworkClassLoader(host,&nbsp;port);
 *   Object main&nbsp;= loader.loadClass("Main", true).newInstance();
 *       &nbsp;.&nbsp;.&nbsp;.
 * </pre></blockquote>
 *
 * <p> The network class loader subclass must define the methods {@link
 * #findClass <tt>findClass</tt>} and <tt>loadClassData</tt> to load a class
 * from the network.  Once it has downloaded the bytes that make up the class,
 * it should use the method {@link #defineClass <tt>defineClass</tt>} to
 * create a class instance.  A sample implementation is:
 *
 * <blockquote><pre>
 *     class NetworkClassLoader extends ClassLoader {
 *         String host;
 *         int port;
 *
 *         public Class findClass(String name) {
 *             byte[] b = loadClassData(name);
 *             return defineClass(name, b, 0, b.length);
 *         }
 *
 *         private byte[] loadClassData(String name) {
 *             // load the class data from the connection
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote>
 *
 * <h3> <a name="name">Binary names</a> </h3>
 *
 * <p> Any class name provided as a {@link String} parameter to methods in
 * <tt>ClassLoader</tt> must be a binary name as defined by
 * <cite>The Java&trade; Language Specification</cite>.
 *
 * <p> Examples of valid class names include:
 * <blockquote><pre>
 *   "java.lang.String"
 *   "javax.swing.JSpinner$DefaultEditor"
 *   "java.security.KeyStore$Builder$FileBuilder$1"
 *   "java.net.URLClassLoader$3$1"
 * </pre></blockquote>
 *
 * @see      #resolveClass(Class)
 * @since 1.0
 */
/*
 * 所有类加载器的祖先，主要用于加载类和加载资源
 *
 * 加载类分为查找类和定义类两个方面
 * 开始加载一个类时，先从子级类加载器向上搜索看该类是否已加载过
 * 如果该类还未加载，则从父级类加载器向下定义类（将class文件的二进制流转换为JVM类对象）
 * 向上搜索类时，只要搜索到目标类，就将结果返回
 * 向下定义类时，只要某个类加载器有能力完成加载任务，它就将加载任务揽下来
 */
public abstract class ClassLoader {

    // system class loader，可能是内置的AppClassLoader(默认)，也可能是自定义的类加载器
    private static native void registerNatives();
    static {
        registerNatives();
    }

    // The parent class loader for delegation
    // Note: VM hardcoded the offset of this field, thus all new fields
    // must be added *after* it.
    // 父级类加载器
    private final ClassLoader parent;

    /**
     * Encapsulates the set of parallel capable loader types.
     */
    /**
     * 封装一组"并行"的类加载器：允许多个线程同时使用该并行类加载器加载类
     */
    private static class ParallelLoaders {
        private ParallelLoaders() {}

        // 存储一组具有并行能力的类加载器
        // the set of parallel capable loader types
        private static final Set<Class<? extends ClassLoader>> loaderTypes =
            Collections.newSetFromMap(
                new WeakHashMap<Class<? extends ClassLoader>, Boolean>());
        static {
            // 注册所有类加载器的祖先ClassLoader并行
            synchronized (loaderTypes) { loaderTypes.add(ClassLoader.class); }
        }

        /**
         * Registers the given class loader type as parallel capabale.
         * Returns {@code true} is successfully registered; {@code false} if
         * loader's super class is not registered.
         */
        // 注册该类加载器为并行
        static boolean register(Class<? extends ClassLoader> c) {
            synchronized (loaderTypes) {
                // 确保该类加载器的父类型也已注册为并行，否则不会注册成功
                if (loaderTypes.contains(c.getSuperclass())) {
                    // register the class loader as parallel capable
                    // if and only if all of its super classes are.
                    // Note: given current classloading sequence, if
                    // the immediate super class is parallel capable,
                    // all the super classes higher up must be too.
                    loaderTypes.add(c);
                    return true;
                } else {
                    return false;
                }
            }
        }

        /**
         * Returns {@code true} if the given class loader type is
         * registered as parallel capable.
         */
        // 判断当前的类加载器是否为并行
        static boolean isRegistered(Class<? extends ClassLoader> c) {
            synchronized (loaderTypes) {
                return loaderTypes.contains(c);
            }
        }
    }

    // Maps class name to the corresponding lock object when the current
    // class loader is parallel capable.
    // Note: VM also uses this field to decide if the current class loader
    // is parallel capable and the appropriate lock object for class loading.
    // 当前类加载器具有并行功能时，将其下的类名映射到锁对象
    private final ConcurrentHashMap<String, Object> parallelLockMap;

    // Hashtable that maps packages to certs
    // 将包名映射到身份证书
    private final Map <String, Certificate[]> package2certs;

    // Shared among all packages with unsigned classes
    private static final Certificate[] nocerts = new Certificate[0];

    // The classes loaded by this class loader. The only purpose of this table
    // is to keep the classes from being GC'ed until the loader is GC'ed.
    // 记录当前类加载器加载的类
    private final Vector<Class<?>> classes = new Vector<>();

    // The "default" domain. Set as the default ProtectionDomain on newly
    // created classes.
    private final ProtectionDomain defaultDomain =
        new ProtectionDomain(new CodeSource(null, (Certificate[]) null),
                             null, this, null);

    // Invoked by the VM to record every loaded class with this loader.
    void addClass(Class<?> c) {
        classes.addElement(c);
    }

    // The packages defined in this class loader.  Each package name is mapped
    // to its corresponding Package object.
    // @GuardedBy("itself")
    // 记录当前类加载器定义的包
    private final HashMap<String, Package> packages = new HashMap<>();

    private static Void checkCreateClassLoader() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        return null;
    }

    private ClassLoader(Void unused, ClassLoader parent) {
        this.parent = parent;
        if (ParallelLoaders.isRegistered(this.getClass())) {
            parallelLockMap = new ConcurrentHashMap<>();
            package2certs = new ConcurrentHashMap<>();
            assertionLock = new Object();
        } else {
            // no finer-grained lock; lock on the classloader instance
            parallelLockMap = null;
            package2certs = new Hashtable<>();
            assertionLock = this;
        }
    }

    /**
     * Creates a new class loader using the specified parent class loader for
     * delegation.
     *
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @param  parent
     *         The parent class loader
     *
     * @throws  SecurityException
     *          If a security manager exists and its
     *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *          of a new class loader.
     *
     * @since  1.2
     */
    protected ClassLoader(ClassLoader parent) {
        this(checkCreateClassLoader(), parent);
    }

    /**
     * Creates a new class loader using the <tt>ClassLoader</tt> returned by
     * the method {@link #getSystemClassLoader()
     * <tt>getSystemClassLoader()</tt>} as the parent class loader.
     *
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @throws  SecurityException
     *          If a security manager exists and its
     *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *          of a new class loader.
     */
    protected ClassLoader() {
        this(checkCreateClassLoader(), getSystemClassLoader());
    }

    // -- Class --

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.
     * This method searches for classes in the same manner as the {@link
     * #loadClass(String, boolean)} method.  It is invoked by the Java virtual
     * machine to resolve class references.  Invoking this method is equivalent
     * to invoking {@link #loadClass(String, boolean) <tt>loadClass(name,
     * false)</tt>}.
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class was not found
     */
    // 根据给定类的全名加载类
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.  The
     * default implementation of this method searches for classes in the
     * following order:
     *
     * <ol>
     *
     *   <li><p> Invoke {@link #findLoadedClass(String)} to check if the class
     *   has already been loaded.  </p></li>
     *
     *   <li><p> Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method
     *   on the parent class loader.  If the parent is <tt>null</tt> the class
     *   loader built-in to the virtual machine is used, instead.  </p></li>
     *
     *   <li><p> Invoke the {@link #findClass(String)} method to find the
     *   class.  </p></li>
     *
     * </ol>
     *
     * <p> If the class was found using the above steps, and the
     * <tt>resolve</tt> flag is true, this method will then invoke the {@link
     * #resolveClass(Class)} method on the resulting <tt>Class</tt> object.
     *
     * <p> Subclasses of <tt>ClassLoader</tt> are encouraged to override {@link
     * #findClass(String)}, rather than this method.  </p>
     *
     * <p> Unless overridden, this method synchronizes on the result of
     * {@link #getClassLoadingLock <tt>getClassLoadingLock</tt>} method
     * during the entire class loading process.
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @param  resolve
     *         If <tt>true</tt> then resolve the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     */
    /**
     * 根据给定类的全名加载类，resolve指示是否链接类
     * 1、获取类加载锁：
     *      确保在多线程环境下类加载的原子性。
     * 2、检查类是否已加载：
     *      避免重复加载类，提高性能。
     * 3、委托父类加载器：
     *      遵循类加载的委托模型，优先由父类加载器加载类。
     * 4、查找引导类加载器：
     *      如果父类加载器不存在，尝试使用引导类加载器加载类。
     * 5、自定义类加载：
     *      如果类未被父类加载器或引导类加载器找到，则调用 findClass 方法自定义加载类。
     * 6、记录统计信息：
     *      记录类加载的时间和次数，用于性能监控。
     * 7、解析类：
     *      如果需要解析类，则调用 resolveClass 方法解析类，确保类的链接过程完成。
     *
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    // 委托父类加载器：遵循类加载的委托模型，优先由父类加载器加载类。
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        // 如果父类加载器不存在，尝试使用引导类加载器加载类。
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }
                // 此类还没被加载
                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    long t1 = System.nanoTime();
                    // 查找定义类，如果此类在模块中，则在模块中查找该类，否则在类路径下查找（如果待查找的类存在，则会加载字节码并交给虚拟机去定义）
                    c = findClass(name);

                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    /**
     * Returns the lock object for class loading operations.
     * For backward compatibility, the default implementation of this method
     * behaves as follows. If this ClassLoader object is registered as
     * parallel capable, the method returns a dedicated object associated
     * with the specified class name. Otherwise, the method returns this
     * ClassLoader object.
     *
     * @param  className
     *         The name of the to-be-loaded class
     *
     * @return the lock for class loading operations
     *
     * @throws NullPointerException
     *         If registered as parallel capable and <tt>className</tt> is null
     *
     * @see #loadClass(String, boolean)
     *
     * @since  1.7
     */
    // 返回类加载操作中使用的锁对象
    protected Object getClassLoadingLock(String className) {
        Object lock = this;
        if (parallelLockMap != null) {
            Object newLock = new Object();
            lock = parallelLockMap.putIfAbsent(className, newLock);
            if (lock == null) {
                lock = newLock;
            }
        }
        return lock;
    }

    // This method is invoked by the virtual machine to load a class.
    // 根据类的名称加载类
    // 1、兼容性处理，如果当前类加载器不支持并行加载，则使用this作为锁对象进行同步
    // 2、直接调用loadClass方法加载类
    private Class<?> loadClassInternal(String name)
        throws ClassNotFoundException
    {
        // For backward compatibility, explicitly lock on 'this' when
        // the current class loader is not parallel capable.
        if (parallelLockMap == null) {
            synchronized (this) {
                 return loadClass(name);
            }
        } else {
            return loadClass(name);
        }
    }

    // Invoked by the VM after loading class with this loader.
    private void checkPackageAccess(Class<?> cls, ProtectionDomain pd) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (ReflectUtil.isNonPublicProxyClass(cls)) {
                for (Class<?> intf: cls.getInterfaces()) {
                    checkPackageAccess(intf, pd);
                }
                return;
            }

            final String name = cls.getName();
            final int i = name.lastIndexOf('.');
            if (i != -1) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        sm.checkPackageAccess(name.substring(0, i));
                        return null;
                    }
                }, new AccessControlContext(new ProtectionDomain[] {pd}));
            }
        }
    }

    /**
     * Finds the class with the specified <a href="#name">binary name</a>.
     * This method should be overridden by class loader implementations that
     * follow the delegation model for loading classes, and will be invoked by
     * the {@link #loadClass <tt>loadClass</tt>} method after checking the
     * parent class loader for the requested class.  The default implementation
     * throws a <tt>ClassNotFoundException</tt>.
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     *
     * @since  1.2
     */
    // [子类覆盖]查找(定义)类，如果该类在模块中，则在模块中查找该类，
    // 否则在类路径下查找（如果待查找的类存在，则会加载器字节码，
    // 并交给虚拟机去定义）
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    /**
     * Converts an array of bytes into an instance of class <tt>Class</tt>.
     * Before the <tt>Class</tt> can be used it must be resolved.  This method
     * is deprecated in favor of the version that takes a <a
     * href="#name">binary name</a> as its first argument, and is more secure.
     *
     * @param  b
     *         The bytes that make up the class data.  The bytes in positions
     *         <tt>off</tt> through <tt>off+len-1</tt> should have the format
     *         of a valid class file as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @param  off
     *         The start offset in <tt>b</tt> of the class data
     *
     * @param  len
     *         The length of the class data
     *
     * @return  The <tt>Class</tt> object that was created from the specified
     *          class data
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class
     *
     * @throws  IndexOutOfBoundsException
     *          If either <tt>off</tt> or <tt>len</tt> is negative, or if
     *          <tt>off+len</tt> is greater than <tt>b.length</tt>.
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class, or if an attempt is made
     *          to define a class in a package with a fully-qualified name
     *          that starts with "{@code java.}".
     *
     * @see  #loadClass(String, boolean)
     * @see  #resolveClass(Class)
     *
     * @deprecated  Replaced by {@link #defineClass(String, byte[], int, int)
     * defineClass(String, byte[], int, int)}
     */
    // 利用存储在缓冲区中的字节码去定义类（无保护域）
    @Deprecated
    protected final Class<?> defineClass(byte[] b, int off, int len)
        throws ClassFormatError
    {
        return defineClass(null, b, off, len, null);
    }

    /**
     * Converts an array of bytes into an instance of class <tt>Class</tt>.
     * Before the <tt>Class</tt> can be used it must be resolved.
     *
     * <p> This method assigns a default {@link java.security.ProtectionDomain
     * <tt>ProtectionDomain</tt>} to the newly defined class.  The
     * <tt>ProtectionDomain</tt> is effectively granted the same set of
     * permissions returned when {@link
     * java.security.Policy#getPermissions(java.security.CodeSource)
     * <tt>Policy.getPolicy().getPermissions(new CodeSource(null, null))</tt>}
     * is invoked.  The default domain is created on the first invocation of
     * {@link #defineClass(String, byte[], int, int) <tt>defineClass</tt>},
     * and re-used on subsequent invocations.
     *
     * <p> To assign a specific <tt>ProtectionDomain</tt> to the class, use
     * the {@link #defineClass(String, byte[], int, int,
     * java.security.ProtectionDomain) <tt>defineClass</tt>} method that takes a
     * <tt>ProtectionDomain</tt> as one of its arguments.  </p>
     *
     * @param  name
     *         The expected <a href="#name">binary name</a> of the class, or
     *         <tt>null</tt> if not known
     *
     * @param  b
     *         The bytes that make up the class data.  The bytes in positions
     *         <tt>off</tt> through <tt>off+len-1</tt> should have the format
     *         of a valid class file as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @param  off
     *         The start offset in <tt>b</tt> of the class data
     *
     * @param  len
     *         The length of the class data
     *
     * @return  The <tt>Class</tt> object that was created from the specified
     *          class data.
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class
     *
     * @throws  IndexOutOfBoundsException
     *          If either <tt>off</tt> or <tt>len</tt> is negative, or if
     *          <tt>off+len</tt> is greater than <tt>b.length</tt>.
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class (which is unsigned), or if
     *          <tt>name</tt> begins with "<tt>java.</tt>".
     *
     * @see  #loadClass(String, boolean)
     * @see  #resolveClass(Class)
     * @see  java.security.CodeSource
     * @see  java.security.SecureClassLoader
     *
     * @since  1.1
     */
    // 利用存储在字节数组中的字节码去定义类（无保护域）
    protected final Class<?> defineClass(String name, byte[] b, int off, int len)
        throws ClassFormatError
    {
        return defineClass(name, b, off, len, null);
    }

    /* Determine protection domain, and check that:
        - not define java.* class,
        - signer of this class matches signers for the rest of the classes in
          package.
    */
    // 预定义类，主要是进行一些安全检查和证书设置
    private ProtectionDomain preDefineClass(String name,
                                            ProtectionDomain pd)
    {
        if (!checkName(name))
            throw new NoClassDefFoundError("IllegalName: " + name);

        // Note:  Checking logic in java.lang.invoke.MemberName.checkForTypeAlias
        // relies on the fact that spoofing is impossible if a class has a name
        // of the form "java.*"
        // 禁止app class loader 以及一些自定义的类加载器加载亿java开头的报名的类
        if ((name != null) && name.startsWith("java.")) {
            throw new SecurityException
                ("Prohibited package name: " +
                 name.substring(0, name.lastIndexOf('.')));
        }
        if (pd == null) {
            pd = defaultDomain;
        }

        // 为className这个类所在的包设置身份证书
        if (name != null) checkCerts(name, pd.getCodeSource());

        return pd;
    }

    // 从保护域中获取代码源的位置信息
    private String defineClassSourceLocation(ProtectionDomain pd)
    {
        CodeSource cs = pd.getCodeSource();
        String source = null;
        if (cs != null && cs.getLocation() != null) {
            source = cs.getLocation().toString();
        }
        return source;
    }

    // 在类定义完之后的一些收尾操作，主要是定义NamedPackage和设置签名
    private void postDefineClass(Class<?> c, ProtectionDomain pd)
    {
        if (pd.getCodeSource() != null) {
            Certificate certs[] = pd.getCodeSource().getCertificates();
            if (certs != null)
                setSigners(c, certs);
        }
    }

    /**
     * Converts an array of bytes into an instance of class <tt>Class</tt>,
     * with an optional <tt>ProtectionDomain</tt>.  If the domain is
     * <tt>null</tt>, then a default domain will be assigned to the class as
     * specified in the documentation for {@link #defineClass(String, byte[],
     * int, int)}.  Before the class can be used it must be resolved.
     *
     * <p> The first class defined in a package determines the exact set of
     * certificates that all subsequent classes defined in that package must
     * contain.  The set of certificates for a class is obtained from the
     * {@link java.security.CodeSource <tt>CodeSource</tt>} within the
     * <tt>ProtectionDomain</tt> of the class.  Any classes added to that
     * package must contain the same set of certificates or a
     * <tt>SecurityException</tt> will be thrown.  Note that if
     * <tt>name</tt> is <tt>null</tt>, this check is not performed.
     * You should always pass in the <a href="#name">binary name</a> of the
     * class you are defining as well as the bytes.  This ensures that the
     * class you are defining is indeed the class you think it is.
     *
     * <p> The specified <tt>name</tt> cannot begin with "<tt>java.</tt>", since
     * all classes in the "<tt>java.*</tt> packages can only be defined by the
     * bootstrap class loader.  If <tt>name</tt> is not <tt>null</tt>, it
     * must be equal to the <a href="#name">binary name</a> of the class
     * specified by the byte array "<tt>b</tt>", otherwise a {@link
     * NoClassDefFoundError <tt>NoClassDefFoundError</tt>} will be thrown. </p>
     *
     * @param  name
     *         The expected <a href="#name">binary name</a> of the class, or
     *         <tt>null</tt> if not known
     *
     * @param  b
     *         The bytes that make up the class data. The bytes in positions
     *         <tt>off</tt> through <tt>off+len-1</tt> should have the format
     *         of a valid class file as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @param  off
     *         The start offset in <tt>b</tt> of the class data
     *
     * @param  len
     *         The length of the class data
     *
     * @param  protectionDomain
     *         The ProtectionDomain of the class
     *
     * @return  The <tt>Class</tt> object created from the data,
     *          and optional <tt>ProtectionDomain</tt>.
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class
     *
     * @throws  NoClassDefFoundError
     *          If <tt>name</tt> is not equal to the <a href="#name">binary
     *          name</a> of the class specified by <tt>b</tt>
     *
     * @throws  IndexOutOfBoundsException
     *          If either <tt>off</tt> or <tt>len</tt> is negative, or if
     *          <tt>off+len</tt> is greater than <tt>b.length</tt>.
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class, or if <tt>name</tt> begins with
     *          "<tt>java.</tt>".
     */
    // 利用存储在字节数组中的字节码去定义类
    protected final Class<?> defineClass(String name, byte[] b, int off, int len,
                                         ProtectionDomain protectionDomain)
        throws ClassFormatError
    {
        // 预定义类，主要是进行一些安全检查和证书设置
        protectionDomain = preDefineClass(name, protectionDomain);
        // 从保护域中获取代码源的位置信息
        String source = defineClassSourceLocation(protectionDomain);
        // 由虚拟机调用，用来定义类（将class字节码加载到JVM）
        Class<?> c = defineClass1(name, b, off, len, protectionDomain, source);
        // 在类定义完之后的一些收尾操作，主要是定义NamedPackage和设置签名
        postDefineClass(c, protectionDomain);
        return c;
    }

    /**
     * Converts a {@link java.nio.ByteBuffer <tt>ByteBuffer</tt>}
     * into an instance of class <tt>Class</tt>,
     * with an optional <tt>ProtectionDomain</tt>.  If the domain is
     * <tt>null</tt>, then a default domain will be assigned to the class as
     * specified in the documentation for {@link #defineClass(String, byte[],
     * int, int)}.  Before the class can be used it must be resolved.
     *
     * <p>The rules about the first class defined in a package determining the
     * set of certificates for the package, and the restrictions on class names
     * are identical to those specified in the documentation for {@link
     * #defineClass(String, byte[], int, int, ProtectionDomain)}.
     *
     * <p> An invocation of this method of the form
     * <i>cl</i><tt>.defineClass(</tt><i>name</i><tt>,</tt>
     * <i>bBuffer</i><tt>,</tt> <i>pd</i><tt>)</tt> yields exactly the same
     * result as the statements
     *
     *<p> <tt>
     * ...<br>
     * byte[] temp = new byte[bBuffer.{@link
     * java.nio.ByteBuffer#remaining remaining}()];<br>
     *     bBuffer.{@link java.nio.ByteBuffer#get(byte[])
     * get}(temp);<br>
     *     return {@link #defineClass(String, byte[], int, int, ProtectionDomain)
     * cl.defineClass}(name, temp, 0,
     * temp.length, pd);<br>
     * </tt></p>
     *
     * @param  name
     *         The expected <a href="#name">binary name</a>. of the class, or
     *         <tt>null</tt> if not known
     *
     * @param  b
     *         The bytes that make up the class data. The bytes from positions
     *         <tt>b.position()</tt> through <tt>b.position() + b.limit() -1
     *         </tt> should have the format of a valid class file as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @param  protectionDomain
     *         The ProtectionDomain of the class, or <tt>null</tt>.
     *
     * @return  The <tt>Class</tt> object created from the data,
     *          and optional <tt>ProtectionDomain</tt>.
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class.
     *
     * @throws  NoClassDefFoundError
     *          If <tt>name</tt> is not equal to the <a href="#name">binary
     *          name</a> of the class specified by <tt>b</tt>
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class, or if <tt>name</tt> begins with
     *          "<tt>java.</tt>".
     *
     * @see      #defineClass(String, byte[], int, int, ProtectionDomain)
     *
     * @since  1.5
     */
    // 利用存储在缓冲区中的字节码去定义类
    protected final Class<?> defineClass(String name, java.nio.ByteBuffer b,
                                         ProtectionDomain protectionDomain)
        throws ClassFormatError
    {
        // 获取二进制流长度
        int len = b.remaining();

        // Use byte[] if not a direct ByteBufer:
        // 如果不是直接缓冲区，则使用byte[]存储该二进制流
        if (!b.isDirect()) {
            if (b.hasArray()) {
                return defineClass(name, b.array(),
                                   b.position() + b.arrayOffset(), len,
                                   protectionDomain);
            } else {
                // no array, or read-only array
                byte[] tb = new byte[len];
                b.get(tb);  // get bytes out of byte buffer.
                return defineClass(name, tb, 0, len, protectionDomain);
            }
        }

        protectionDomain = preDefineClass(name, protectionDomain);
        String source = defineClassSourceLocation(protectionDomain);
        Class<?> c = defineClass2(name, b, b.position(), len, protectionDomain, source);
        postDefineClass(c, protectionDomain);
        return c;
    }

    private native Class<?> defineClass0(String name, byte[] b, int off, int len,
                                         ProtectionDomain pd);

    private native Class<?> defineClass1(String name, byte[] b, int off, int len,
                                         ProtectionDomain pd, String source);

    private native Class<?> defineClass2(String name, java.nio.ByteBuffer b,
                                         int off, int len, ProtectionDomain pd,
                                         String source);

    // true if the name is null or has the potential to be a valid binary name
    // 校验类名
    private boolean checkName(String name) {
        if ((name == null) || (name.isEmpty()))
            return true;
        if ((name.indexOf('/') != -1)
            || (!VM.allowArraySyntax() && (name.charAt(0) == '[')))
            return false;
        return true;
    }

    // 为className这个类所在的包设置身份证书
    private void checkCerts(String name, CodeSource cs) {
        int i = name.lastIndexOf('.');
        String pname = (i == -1) ? "" : name.substring(0, i);

        Certificate[] certs = null;
        if (cs != null) {
            certs = cs.getCertificates();
        }
        Certificate[] pcerts = null;
        if (parallelLockMap == null) {
            synchronized (this) {
                pcerts = package2certs.get(pname);
                if (pcerts == null) {
                    package2certs.put(pname, (certs == null? nocerts:certs));
                }
            }
        } else {
            pcerts = ((ConcurrentHashMap<String, Certificate[]>)package2certs).
                putIfAbsent(pname, (certs == null? nocerts:certs));
        }
        if (pcerts != null && !compareCerts(pcerts, certs)) {
            throw new SecurityException("class \""+ name +
                 "\"'s signer information does not match signer information of other classes in the same package");
        }
    }

    /**
     * check to make sure the certs for the new class (certs) are the same as
     * the certs for the first class inserted in the package (pcerts)
     */
    // 比较身份信息
    private boolean compareCerts(Certificate[] pcerts,
                                 Certificate[] certs)
    {
        // certs can be null, indicating no certs.
        if ((certs == null) || (certs.length == 0)) {
            return pcerts.length == 0;
        }

        // the length must be the same at this point
        if (certs.length != pcerts.length)
            return false;

        // go through and make sure all the certs in one array
        // are in the other and vice-versa.
        boolean match;
        for (int i = 0; i < certs.length; i++) {
            match = false;
            for (int j = 0; j < pcerts.length; j++) {
                if (certs[i].equals(pcerts[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }

        // now do the same for pcerts
        for (int i = 0; i < pcerts.length; i++) {
            match = false;
            for (int j = 0; j < certs.length; j++) {
                if (pcerts[i].equals(certs[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;
        }

        return true;
    }

    /**
     * Links the specified class.  This (misleadingly named) method may be
     * used by a class loader to link a class.  If the class <tt>c</tt> has
     * already been linked, then this method simply returns. Otherwise, the
     * class is linked as described in the "Execution" chapter of
     * <cite>The Java&trade; Language Specification</cite>.
     *
     * @param  c
     *         The class to link
     *
     * @throws  NullPointerException
     *          If <tt>c</tt> is <tt>null</tt>.
     *
     * @see  #defineClass(String, byte[], int, int)
     */
    // 链接类
    protected final void resolveClass(Class<?> c) {
        resolveClass0(c);
    }

    private native void resolveClass0(Class<?> c);

    /**
     * Finds a class with the specified <a href="#name">binary name</a>,
     * loading it if necessary.
     *
     * <p> This method loads the class through the system class loader (see
     * {@link #getSystemClassLoader()}).  The <tt>Class</tt> object returned
     * might have more than one <tt>ClassLoader</tt> associated with it.
     * Subclasses of <tt>ClassLoader</tt> need not usually invoke this method,
     * because most class loaders need to override just {@link
     * #findClass(String)}.  </p>
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @return  The <tt>Class</tt> object for the specified <tt>name</tt>
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     *
     * @see  #ClassLoader(ClassLoader)
     * @see  #getParent()
     */
    // 使用system类加载器加载指定的类
    protected final Class<?> findSystemClass(String name)
        throws ClassNotFoundException
    {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            if (!checkName(name))
                throw new ClassNotFoundException(name);
            Class<?> cls = findBootstrapClass(name);
            if (cls == null) {
                throw new ClassNotFoundException(name);
            }
            return cls;
        }
        return system.loadClass(name);
    }

    /**
     * Returns a class loaded by the bootstrap class loader;
     * or return null if not found.
     */
    // 查找指定的类，如果该类未被bootstrap类加载器加载，返回null
    private Class<?> findBootstrapClassOrNull(String name)
    {
        if (!checkName(name)) return null;

        return findBootstrapClass(name);
    }

    // return null if not found
    // 查找指定的类，如果该类未被bootstrap类加载器加载，返回null
    private native Class<?> findBootstrapClass(String name);

    /**
     * Returns the class with the given <a href="#name">binary name</a> if this
     * loader has been recorded by the Java virtual machine as an initiating
     * loader of a class with that <a href="#name">binary name</a>.  Otherwise
     * <tt>null</tt> is returned.
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @return  The <tt>Class</tt> object, or <tt>null</tt> if the class has
     *          not been loaded
     *
     * @since  1.1
     */
    // 查找指定的类，如果该类未被当前类加载器加载，返回null
    protected final Class<?> findLoadedClass(String name) {
        if (!checkName(name))
            return null;
        return findLoadedClass0(name);
    }

    // 查找指定的类，如果该类未被当前类加载器加载，返回null
    private native final Class<?> findLoadedClass0(String name);

    /**
     * Sets the signers of a class.  This should be invoked after defining a
     * class.
     *
     * @param  c
     *         The <tt>Class</tt> object
     *
     * @param  signers
     *         The signers for the class
     *
     * @since  1.1
     */
    // 设置签名信息，在类定义完成后调用
    protected final void setSigners(Class<?> c, Object[] signers) {
        c.setSigners(signers);
    }


    // -- Resource --

    /**
     * Finds the resource with the given name.  A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p> The name of a resource is a '<tt>/</tt>'-separated path name that
     * identifies the resource.
     *
     * <p> This method will first search the parent class loader for the
     * resource; if the parent is <tt>null</tt> the path of the class loader
     * built-in to the virtual machine is searched.  That failing, this method
     * will invoke {@link #findResource(String)} to find the resource.  </p>
     *
     * @apiNote When overriding this method it is recommended that an
     * implementation ensures that any delegation is consistent with the {@link
     * #getResources(java.lang.String) getResources(String)} method.
     *
     * @param  name
     *         The resource name
     *
     * @return  A <tt>URL</tt> object for reading the resource, or
     *          <tt>null</tt> if the resource could not be found or the invoker
     *          doesn't have adequate  privileges to get the resource.
     *
     * @since  1.1
     */
    // 自顶向下加载资源，截止到调用此方法的类加载器。返回【首个】匹配到的资源的URL
    public URL getResource(String name) {
        URL url;
        // 先尝试由父级类加载器搜索资源
        if (parent != null) {
            // 如果存在父级类加载器，继续向上搜索
            url = parent.getResource(name);
        } else {
            // 如果不存在父级类加载器,使用bootstrap类加载器搜索
            url = getBootstrapResource(name);
        }
        // 如果父级类加载器没有找到，则尝试由当前类加载器搜索
        if (url == null) {
            // 在当前类加载器可以访问到的模块路径/类路径下搜索首个匹配的资源
            url = findResource(name);
        }
        return url;
    }

    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p>The name of a resource is a <tt>/</tt>-separated path name that
     * identifies the resource.
     *
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @apiNote When overriding this method it is recommended that an
     * implementation ensures that any delegation is consistent with the {@link
     * #getResource(java.lang.String) getResource(String)} method. This should
     * ensure that the first element returned by the Enumeration's
     * {@code nextElement} method is the same resource that the
     * {@code getResource(String)} method would return.
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *          the resource.  If no resources could  be found, the enumeration
     *          will be empty.  Resources that the class loader doesn't have
     *          access to will not be in the enumeration.
     *
     * @throws  IOException
     *          If I/O errors occur
     *
     * @see  #findResources(String)
     *
     * @since  1.2
     */
    // 自顶向下加载资源，截止到调用此方法的类加载器。返回【所有】匹配资源的URL
    public Enumeration<URL> getResources(String name) throws IOException {
        @SuppressWarnings("unchecked")
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        // 先尝试由父级类加载器搜索资源
        if (parent != null) {
            // 如果存在父级类加载器，继续向上搜索
            tmp[0] = parent.getResources(name);
        } else {
            // 如果不存在父级类加载器，说明遇到了bootstrap类加载器
            tmp[0] = getBootstrapResources(name);
        }
        // 在当前类加载器下辖的模块路径/类路径的根目录下搜索所有匹配的资源
        tmp[1] = findResources(name);

        return new CompoundEnumeration<>(tmp);
    }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.
     *
     * @param  name
     *         The resource name
     *
     * @return  A <tt>URL</tt> object for reading the resource, or
     *          <tt>null</tt> if the resource could not be found
     *
     * @since  1.2
     */
    // [子类覆盖]在当前类加载器可以访问到的模块路径/类路径下搜索首个匹配的资源
    protected URL findResource(String name) {
        return null;
    }

    /**
     * Returns an enumeration of {@link java.net.URL <tt>URL</tt>} objects
     * representing all the resources with the given name. Class loader
     * implementations should override this method to specify where to load
     * resources from.
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *          the resources
     *
     * @throws  IOException
     *          If I/O errors occur
     *
     * @since  1.2
     */
    // [子类覆盖]在当前类加载器下辖的模块路径/类路径的根目录下搜索所有匹配的资源
    protected Enumeration<URL> findResources(String name) throws IOException {
        return java.util.Collections.emptyEnumeration();
    }

    /**
     * Registers the caller as parallel capable.
     * The registration succeeds if and only if all of the following
     * conditions are met:
     * <ol>
     * <li> no instance of the caller has been created</li>
     * <li> all of the super classes (except class Object) of the caller are
     * registered as parallel capable</li>
     * </ol>
     * <p>Note that once a class loader is registered as parallel capable, there
     * is no way to change it back.</p>
     *
     * @return  true if the caller is successfully registered as
     *          parallel capable and false if otherwise.
     *
     * @since   1.7
     */
    // 将当前类加载器注册为并行，需要在静态初始化块中进行
    @CallerSensitive
    protected static boolean registerAsParallelCapable() {
        // Reflection.getCallerClass() 获取registerAsParallelCapable()方法的调用者所处的类
        // 如果ClassLoader类型是caller类型的父类/父接口，则返回父类型
        Class<? extends ClassLoader> callerClass =
            Reflection.getCallerClass().asSubclass(ClassLoader.class);
        return ParallelLoaders.register(callerClass);
    }

    /**
     * Find a resource of the specified name from the search path used to load
     * classes.  This method locates the resource through the system class
     * loader (see {@link #getSystemClassLoader()}).
     *
     * @param  name
     *         The resource name
     *
     * @return  A {@link java.net.URL <tt>URL</tt>} object for reading the
     *          resource, or <tt>null</tt> if the resource could not be found
     *
     * @since  1.1
     */
    // 自顶向下加载资源，截止到system类加载器。返回【首个】匹配到的资源的URL
    public static URL getSystemResource(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResource(name);
        }
        return system.getResource(name);
    }

    /**
     * Finds all resources of the specified name from the search path used to
     * load classes.  The resources thus found are returned as an
     * {@link java.util.Enumeration <tt>Enumeration</tt>} of {@link
     * java.net.URL <tt>URL</tt>} objects.
     *
     * <p> The search order is described in the documentation for {@link
     * #getSystemResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of resource {@link java.net.URL <tt>URL</tt>}
     *          objects
     *
     * @throws  IOException
     *          If I/O errors occur

     * @since  1.2
     */
    // 自顶向下加载资源，截止到system类加载器。返回【所有】匹配资源的URL
    public static Enumeration<URL> getSystemResources(String name)
        throws IOException
    {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResources(name);
        }
        return system.getResources(name);
    }

    /**
     * Find resources from the VM's built-in classloader.
     */
    /**
     * 从VM的内置类加载器中查找资源。具体步骤如下：
     * 1、获取引导类路径（URLClassPath）。
     * 2、使用引导类路径查找指定名称的资源。
     * 3、如果找到资源，则返回资源的URL；否则返回null。
     * @param name
     * @return
     */
    private static URL getBootstrapResource(String name) {
        URLClassPath ucp = getBootstrapClassPath();
        Resource res = ucp.getResource(name);
        return res != null ? res.getURL() : null;
    }

    /**
     * Find resources from the VM's built-in classloader.
     */
    /**
     * 从启动类路径中查找资源。具体功能如下：
     * 1、获取资源枚举：
     *      调用 getBootstrapClassPath().getResources(name)
     *      获取启动类路径中指定名称的资源枚举。
     * 2、转换枚举类型：
     *      将 Enumeration<Resource> 转换为 Enumeration<URL>，
     *      以便返回 URL 类型的资源枚举。
     * @param name
     * @return
     * @throws IOException
     */
    private static Enumeration<URL> getBootstrapResources(String name)
        throws IOException
    {
        final Enumeration<Resource> e =
            getBootstrapClassPath().getResources(name);
        return new Enumeration<URL> () {
            public URL nextElement() {
                return e.nextElement().getURL();
            }
            public boolean hasMoreElements() {
                return e.hasMoreElements();
            }
        };
    }

    // Returns the URLClassPath that is used for finding system resources.

    /**
     * 从 sun.misc.Launcher 类中获取引导类路径（Bootstrap Class Path）。
     * 引导类路径包含了 JVM 启动时加载的核心类库的路径信息。
     * @return
     */
    static URLClassPath getBootstrapClassPath() {
        return sun.misc.Launcher.getBootstrapClassPath();
    }


    /**
     * Returns an input stream for reading the specified resource.
     *
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An input stream for reading the resource, or <tt>null</tt>
     *          if the resource could not be found
     *
     * @since  1.1
     */
    /**
     * 从当前类加载器中获取指定名称的资源，并返回一个输入流。
     * 1、获取资源URL：
     *      调用 getResource(name) 方法，尝试从类路径中找到指定名称的资源，并返回其URL。
     * 2、打开输入流：
     *      如果找到了资源URL，则调用 url.openStream() 方法打开输入流并返回。
     *      如果未找到资源或打开输入流时发生异常，则返回 null。
     * @param name
     * @return
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Open for reading, a resource of the specified name from the search path
     * used to load classes.  This method locates the resource through the
     * system class loader (see {@link #getSystemClassLoader()}).
     *
     * @param  name
     *         The resource name
     *
     * @return  An input stream for reading the resource, or <tt>null</tt>
     *          if the resource could not be found
     *
     * @since  1.1
     */
    /**
     * 从系统类加载器中获取指定名称的资源，并返回一个输入流。
     * 1、获取资源URL：
     *      调用 getSystemResource(name) 方法，尝试从系统类加载器中获取指定名称的资源URL。
     * 2、打开输入流：
     *      如果获取到的URL不为null，调用 url.openStream() 方法打开输入流，
     *      如果URL为null或打开过程中发生IO异常，返回null。
     * @param name
     * @return
     */
    public static InputStream getSystemResourceAsStream(String name) {
        URL url = getSystemResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }


    // -- Hierarchy --

    /**
     * Returns the parent class loader for delegation. Some implementations may
     * use <tt>null</tt> to represent the bootstrap class loader. This method
     * will return <tt>null</tt> in such implementations if this class loader's
     * parent is the bootstrap class loader.
     *
     * <p> If a security manager is present, and the invoker's class loader is
     * not <tt>null</tt> and is not an ancestor of this class loader, then this
     * method invokes the security manager's {@link
     * SecurityManager#checkPermission(java.security.Permission)
     * <tt>checkPermission</tt>} method with a {@link
     * RuntimePermission#RuntimePermission(String)
     * <tt>RuntimePermission("getClassLoader")</tt>} permission to verify
     * access to the parent class loader is permitted.  If not, a
     * <tt>SecurityException</tt> will be thrown.  </p>
     *
     * @return  The parent <tt>ClassLoader</tt>
     *
     * @throws  SecurityException
     *          If a security manager exists and its <tt>checkPermission</tt>
     *          method doesn't allow access to this class loader's parent class
     *          loader.
     *
     * @since  1.2
     */
    /**
     * 获取当前类加载器的父类加载器。
     * 1、检查父类加载器是否为 null：
     *      如果 parent 为 null，则直接返回 null。
     * 2、获取安全管理器：
     *      调用 System.getSecurityManager() 获取当前的安全管理器。
     * 3、检查安全管理器是否存在：
     *      如果安全管理器存在，则进行权限检查。
     * 4、权限检查：
     *      调用 checkClassLoaderPermission 方法，检查调用者是否有权限访问父类加载器。
     * 5、返回父类加载器：
     *      如果通过权限检查，返回 parent。
     * @return
     */
    @CallerSensitive
    public final ClassLoader getParent() {
        if (parent == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // Check access to the parent class loader
            // If the caller's class loader is same as this class loader,
            // permission check is performed.
            checkClassLoaderPermission(parent, Reflection.getCallerClass());
        }
        return parent;
    }

    /**
     * Returns the system class loader for delegation.  This is the default
     * delegation parent for new <tt>ClassLoader</tt> instances, and is
     * typically the class loader used to start the application.
     *
     * <p> This method is first invoked early in the runtime's startup
     * sequence, at which point it creates the system class loader and sets it
     * as the context class loader of the invoking <tt>Thread</tt>.
     *
     * <p> The default system class loader is an implementation-dependent
     * instance of this class.
     *
     * <p> If the system property "<tt>java.system.class.loader</tt>" is defined
     * when this method is first invoked then the value of that property is
     * taken to be the name of a class that will be returned as the system
     * class loader.  The class is loaded using the default system class loader
     * and must define a public constructor that takes a single parameter of
     * type <tt>ClassLoader</tt> which is used as the delegation parent.  An
     * instance is then created using this constructor with the default system
     * class loader as the parameter.  The resulting class loader is defined
     * to be the system class loader.
     *
     * <p> If a security manager is present, and the invoker's class loader is
     * not <tt>null</tt> and the invoker's class loader is not the same as or
     * an ancestor of the system class loader, then this method invokes the
     * security manager's {@link
     * SecurityManager#checkPermission(java.security.Permission)
     * <tt>checkPermission</tt>} method with a {@link
     * RuntimePermission#RuntimePermission(String)
     * <tt>RuntimePermission("getClassLoader")</tt>} permission to verify
     * access to the system class loader.  If not, a
     * <tt>SecurityException</tt> will be thrown.  </p>
     *
     * @return  The system <tt>ClassLoader</tt> for delegation, or
     *          <tt>null</tt> if none
     *
     * @throws  SecurityException
     *          If a security manager exists and its <tt>checkPermission</tt>
     *          method doesn't allow access to the system class loader.
     *
     * @throws  IllegalStateException
     *          If invoked recursively during the construction of the class
     *          loader specified by the "<tt>java.system.class.loader</tt>"
     *          property.
     *
     * @throws  Error
     *          If the system property "<tt>java.system.class.loader</tt>"
     *          is defined but the named class could not be loaded, the
     *          provider class does not define the required constructor, or an
     *          exception is thrown by that constructor when it is invoked. The
     *          underlying cause of the error can be retrieved via the
     *          {@link Throwable#getCause()} method.
     *
     * @revised  1.4
     */
    /**
     * 获取系统类加载器
     * 1、初始化系统类加载器：
     *      调用 initSystemClassLoader 方法，确保系统类加载器已经初始化。
     *      如果 scl 已经被设置，则直接返回。
     * 2、检查系统类加载器是否为 null：
     *      如果 scl 为 null，直接返回 null。
     * 3、获取安全管理器：
     *      调用 System.getSecurityManager 获取当前的安全管理器。
     * 4、检查权限：
     *      如果存在安全管理器，调用 checkClassLoaderPermission 方法检查调用者是否有权限访问系统类加载器。
     *      checkClassLoaderPermission 方法会检查调用者的类加载器是否与系统类加载器相同或为其祖先，如果不是，则需要检查 RuntimePermission("getClassLoader") 权限。
     * 5、返回系统类加载器：
     *      如果所有检查都通过，返回系统类加载器 scl。
     * @return
     */
    @CallerSensitive
    public static ClassLoader getSystemClassLoader() {
        initSystemClassLoader();
        if (scl == null) {
            return null;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkClassLoaderPermission(scl, Reflection.getCallerClass());
        }
        return scl;
    }

    /*
     * 初始化系统类加载器(SystemClassLoader)，并将其返回
     */
    private static synchronized void initSystemClassLoader() {
        if (!sclSet) {
            if (scl != null)
                throw new IllegalStateException("recursive invocation");
            sun.misc.Launcher l = sun.misc.Launcher.getLauncher();
            if (l != null) {
                Throwable oops = null;
                scl = l.getClassLoader();
                try {
                    scl = AccessController.doPrivileged(
                        new SystemClassLoaderAction(scl));
                } catch (PrivilegedActionException pae) {
                    oops = pae.getCause();
                    if (oops instanceof InvocationTargetException) {
                        oops = oops.getCause();
                    }
                }
                if (oops != null) {
                    if (oops instanceof Error) {
                        throw (Error) oops;
                    } else {
                        // wrap the exception
                        throw new Error(oops);
                    }
                }
            }
            sclSet = true;
        }
    }

    // Returns true if the specified class loader can be found in this class
    // loader's delegation chain.

    /**
     * 这段代码定义了一个名为 isAncestor 的方法，用于判断传入的类加载器 cl 是否是当前类加载器的祖先类加载器。
     * 1、初始化：
     *      将当前类加载器赋值给 acl。
     * 2、循环：
     *      使用 do-while 循环遍历当前类加载器及其所有父类加载器。
     * 3、条件判断：
     *      在每次循环中，检查 cl 是否等于 acl，如果是则返回 true。
     * 4、更新：
     *      将 acl 更新为其父类加载器。
     * 5、结束：
     *      如果遍历完所有父类加载器仍未找到匹配的 cl，则返回 false。
     * @param cl
     * @return
     */
    boolean isAncestor(ClassLoader cl) {
        ClassLoader acl = this;
        do {
            acl = acl.parent;
            if (cl == acl) {
                return true;
            }
        } while (acl != null);
        return false;
    }

    // Tests if class loader access requires "getClassLoader" permission
    // check.  A class loader 'from' can access class loader 'to' if
    // class loader 'from' is same as class loader 'to' or an ancestor
    // of 'to'.  The class loader in a system domain can access
    // any class loader.

    /**
     * 从 from 类加载器到 to 类加载器是否需要进行权限检查。具体逻辑如下：
     * 1、如果 from 和 to 是同一个类加载器，返回 false。
     * 2、如果 from 为 null，返回 false。
     * 3、否则，返回 to 是否不是 from 的祖先类加载器。：
     *      如果 to 不是 from 的祖先，返回 true。
     *      如果 to 是 from 的祖先，返回 false。
     * @param from
     * @param to
     * @return
     */
    private static boolean needsClassLoaderPermissionCheck(ClassLoader from,
                                                           ClassLoader to)
    {
        if (from == to)
            return false;

        if (from == null)
            return false;

        return !to.isAncestor(from);
    }

    // Returns the class's class loader, or null if none.
    // 返回caller的类加载器
    static ClassLoader getClassLoader(Class<?> caller) {
        // This can be null if the VM is requesting it
        if (caller == null) {
            return null;
        }
        // Circumvent security check since this is package-private
        return caller.getClassLoader0();
    }

    /*
     * Checks RuntimePermission("getClassLoader") permission
     * if caller's class loader is not null and caller's class loader
     * is not the same as or an ancestor of the given cl argument.
     */
    // 访问权限检查（检查的是caller的类加载器对cl的访问权限）
    static void checkClassLoaderPermission(ClassLoader cl, Class<?> caller) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // caller can be null if the VM is requesting it
            // 返回caller的类加载器
            ClassLoader ccl = getClassLoader(caller);
            if (needsClassLoaderPermissionCheck(ccl, cl)) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
        }
    }

    // The class loader for the system
    // @GuardedBy("ClassLoader.class")
    // system class loader，可能是内置的AppClassLoader(默认)，也可能是自定义的类加载器
    private static ClassLoader scl;

    // Set to true once the system class loader has been set
    // @GuardedBy("ClassLoader.class")
    private static boolean sclSet;


    // -- Package --

    /**
     * Defines a package by name in this <tt>ClassLoader</tt>.  This allows
     * class loaders to define the packages for their classes. Packages must
     * be created before the class is defined, and package names must be
     * unique within a class loader and cannot be redefined or changed once
     * created.
     *
     * @param  name
     *         The package name
     *
     * @param  specTitle
     *         The specification title
     *
     * @param  specVersion
     *         The specification version
     *
     * @param  specVendor
     *         The specification vendor
     *
     * @param  implTitle
     *         The implementation title
     *
     * @param  implVersion
     *         The implementation version
     *
     * @param  implVendor
     *         The implementation vendor
     *
     * @param  sealBase
     *         If not <tt>null</tt>, then this package is sealed with
     *         respect to the given code source {@link java.net.URL
     *         <tt>URL</tt>}  object.  Otherwise, the package is not sealed.
     *
     * @return  The newly defined <tt>Package</tt> object
     *
     * @throws  IllegalArgumentException
     *          If package name duplicates an existing package either in this
     *          class loader or one of its ancestors
     *
     * @since  1.2
     */
    // 用于在类加载器中定义一个新的包
    protected Package definePackage(String name, String specTitle,
                                    String specVersion, String specVendor,
                                    String implTitle, String implVersion,
                                    String implVendor, URL sealBase)
        throws IllegalArgumentException
    {
        synchronized (packages) {
            // 检查包是否存在
            Package pkg = getPackage(name);
            if (pkg != null) {
                throw new IllegalArgumentException(name);
            }
            // 创建新包
            pkg = new Package(name, specTitle, specVersion, specVendor,
                              implTitle, implVersion, implVendor,
                              sealBase, this);
            // 将包添加到集合中
            packages.put(name, pkg);
            return pkg;
        }
    }

    /**
     * Returns a <tt>Package</tt> that has been defined by this class loader
     * or any of its ancestors.
     *
     * @param  name
     *         The package name
     *
     * @return  The <tt>Package</tt> corresponding to the given name, or
     *          <tt>null</tt> if not found
     *
     * @since  1.2
     */
    protected Package getPackage(String name) {
        Package pkg;
        synchronized (packages) {
            pkg = packages.get(name);
        }
        if (pkg == null) {
            if (parent != null) {
                pkg = parent.getPackage(name);
            } else {
                pkg = Package.getSystemPackage(name);
            }
            if (pkg != null) {
                synchronized (packages) {
                    Package pkg2 = packages.get(name);
                    if (pkg2 == null) {
                        packages.put(name, pkg);
                    } else {
                        pkg = pkg2;
                    }
                }
            }
        }
        return pkg;
    }

    /**
     * Returns all of the <tt>Packages</tt> defined by this class loader and
     * its ancestors.
     *
     * @return  The array of <tt>Package</tt> objects defined by this
     *          <tt>ClassLoader</tt>
     *
     * @since  1.2
     */
    /**
     * 返回对当前类加载器可视的包对象
     * 1、检查本地缓存：
     *      首先在当前类加载器的 packages 缓存中查找指定名称的包。
     * 2、递归查找父类加载器：
     *      如果本地缓存中没有找到，递归调用父类加载器的 getPackage 方法。
     * 3、系统包查找：
     *      如果父类加载器也没有找到，调用 Package.getSystemPackage 方法查找系统包。
     * 4、缓存结果：
     *      如果在上述步骤中找到了包，将其缓存到当前类加载器的 packages 中，以提高后续查找效率。
     * @return
     */
    protected Package[] getPackages() {
        Map<String, Package> map;
        synchronized (packages) {
            map = new HashMap<>(packages);
        }
        Package[] pkgs;
        if (parent != null) {
            pkgs = parent.getPackages();
        } else {
            pkgs = Package.getSystemPackages();
        }
        if (pkgs != null) {
            for (int i = 0; i < pkgs.length; i++) {
                String pkgName = pkgs[i].getName();
                if (map.get(pkgName) == null) {
                    map.put(pkgName, pkgs[i]);
                }
            }
        }
        return map.values().toArray(new Package[map.size()]);
    }


    // -- Native library access --

    /**
     * Returns the absolute path name of a native library.  The VM invokes this
     * method to locate the native libraries that belong to classes loaded with
     * this class loader. If this method returns <tt>null</tt>, the VM
     * searches the library along the path specified as the
     * "<tt>java.library.path</tt>" property.
     *
     * @param  libname
     *         The library name
     *
     * @return  The absolute path of the native library
     *
     * @see  System#loadLibrary(String)
     * @see  System#mapLibraryName(String)
     *
     * @since  1.2
     */
    // 返回本地库的绝对路径名
    protected String findLibrary(String libname) {
        return null;
    }

    /**
     * The inner class NativeLibrary denotes a loaded native library instance.
     * Every classloader contains a vector of loaded native libraries in the
     * private field <tt>nativeLibraries</tt>.  The native libraries loaded
     * into the system are entered into the <tt>systemNativeLibraries</tt>
     * vector.
     *
     * <p> Every native library requires a particular version of JNI. This is
     * denoted by the private <tt>jniVersion</tt> field.  This field is set by
     * the VM when it loads the library, and used by the VM to pass the correct
     * version of JNI to the native methods.  </p>
     *
     * @see      ClassLoader
     * @since    1.2
     */
    // 已加载的本地库
    static class NativeLibrary {
        // opaque handle to native library, used in native code.
        long handle;
        // the version of JNI environment the native library requires.
        private int jniVersion;
        // the class from which the library is loaded, also indicates
        // the loader this native library belongs.
        // 加载当前本地库的类，也能识别出加载该本地库的类加载器
        private final Class<?> fromClass;
        // the canonicalized name of the native library.
        // or static library name
        // 被加载的本地库的规范路径
        String name;
        // Indicates if the native library is linked into the VM
        // 指示当前本地库是否为链接到VM的静态库
        boolean isBuiltin;
        // Indicates if the native library is loaded
        // 确定某个资源或操作是否已经成功加载。
        boolean loaded;

        private static final boolean loadLibraryOnlyIfPresent = ClassLoaderHelper.loadLibraryOnlyIfPresent();


        native void load(String name, boolean isBuiltin, boolean throwExceptionIfFail);

        native long find(String name);
        native void unload(String name, boolean isBuiltin);

        /**
         * 构造方法 NativeLibrary:
         * 1、初始化 NativeLibrary 对象，设置本地库的名称、
         *  加载该库的类和是否为内置库。
         * @param fromClass
         * @param name
         * @param isBuiltin
         */
        public NativeLibrary(Class<?> fromClass, String name, boolean isBuiltin) {
            this.name = name;
            this.fromClass = fromClass;
            this.isBuiltin = isBuiltin;
        }

        /**
         * 在对象被垃圾回收时调用，确保本地库被正确卸载。
         * 同步 loadedLibraryNames，检查 fromClass 的类加载器是否存在且库已加载。
         * 移除已加载的库名，调用 unload 方法卸载库。
         */
        protected void finalize() {
            synchronized (loadedLibraryNames) {
                if (fromClass.getClassLoader() != null && loaded) {
                    /* remove the native library name */
                    int size = loadedLibraryNames.size();
                    for (int i = 0; i < size; i++) {
                        if (name.equals(loadedLibraryNames.elementAt(i))) {
                            loadedLibraryNames.removeElementAt(i);
                            break;
                        }
                    }
                    /* unload the library. */
                    ClassLoader.nativeLibraryContext.push(this);
                    try {
                        unload(name, isBuiltin);
                    } finally {
                        ClassLoader.nativeLibraryContext.pop();
                    }
                }
            }
        }
        // Invoked in the VM to determine the context class in
        // JNI_Load/JNI_Unload
        // 返回当前上下文中加载本地库的类。
        static Class<?> getFromClass() {
            return ClassLoader.nativeLibraryContext.peek().fromClass;
        }
    }

    // All native library names we've loaded.
    // 所有已加载的本地库名称(规范路径)
    private static Vector<String> loadedLibraryNames = new Vector<>();

    // Native libraries belonging to system classes.
    // 被bootstrap类加载器加载的本地库列表
    private static Vector<NativeLibrary> systemNativeLibraries
        = new Vector<>();

    // Native libraries associated with the class loader.
    // 被当前类加载器加载的本地库列表
    private Vector<NativeLibrary> nativeLibraries = new Vector<>();

    // native libraries being loaded/unloaded.
    // 待加载/卸载的本地库列表(加载/卸载完成后从此列表中移除)
    private static Stack<NativeLibrary> nativeLibraryContext = new Stack<>();

    // The paths searched for libraries
    // 用户本地库路径
    private static String usr_paths[];
    // 系统本地库路径
    private static String sys_paths[];

    // 从路径属性propname中解析出对应的所有路径信息
    private static String[] initializePath(String propname) {
        String ldpath = System.getProperty(propname, "");
        // 路径之间的分隔符：Windows系统上是';'，类Unix系统上是':'
        String ps = File.pathSeparator;
        int ldlen = ldpath.length();
        int i, j, n;
        // Count the separators in the path
        i = ldpath.indexOf(ps);
        n = 0;
        while (i >= 0) {
            n++;
            i = ldpath.indexOf(ps, i + 1);
        }

        // allocate the array of paths - n :'s = n + 1 path elements
        String[] paths = new String[n + 1];

        // Fill the array with paths from the ldpath
        n = i = 0;
        j = ldpath.indexOf(ps);
        while (j >= 0) {
            if (j - i > 0) {
                paths[n++] = ldpath.substring(i, j);
            } else if (j - i == 0) {
                paths[n++] = ".";
            }
            i = j + 1;
            j = ldpath.indexOf(ps, i);
        }
        // 无论如何，把当前目录加进来
        paths[n] = ldpath.substring(i, ldlen);
        return paths;
    }

    // Invoked in the java.lang.Runtime class to implement load and loadLibrary.
    /*
     * 加载指定名称的本地库。
     * isAbsolute用来指示libName是否为绝对路径，如果是绝对路径，可以直接去该路径下加载该资源。
     * fromClass通常是发起加载操作的类型，此处用来提供类加载器对象(可能为null，因为fromClass类可能由bootstrap类加载器加载)。
     */
    static void loadLibrary(Class<?> fromClass, String name,
                            boolean isAbsolute) {
        ClassLoader loader =
            (fromClass == null) ? null : fromClass.getClassLoader();
        if (sys_paths == null) {
            usr_paths = initializePath("java.library.path");
            sys_paths = initializePath("sun.boot.library.path");
        }
        // 如果libName是绝对路径
        if (isAbsolute) {
            // 加载指定的本地库文件
            if (loadLibrary0(fromClass, new File(name))) {
                return;
            }
            throw new UnsatisfiedLinkError("Can't load library: " + name);
        }
        if (loader != null) {
            // 返回本地库的绝对路径名
            String libfilename = loader.findLibrary(name);
            if (libfilename != null) {
                File libfile = new File(libfilename);
                // 确保获取到了本地库的绝对名称
                if (!libfile.isAbsolute()) {
                    throw new UnsatisfiedLinkError(
    "ClassLoader.findLibrary failed to return an absolute path: " + libfilename);
                }
                // 加载指定的本地库文件
                if (loadLibrary0(fromClass, libfile)) {
                    return;
                }
                throw new UnsatisfiedLinkError("Can't load " + libfilename);
            }
        }
        // 遍历系统本地库路径
        for (int i = 0 ; i < sys_paths.length ; i++) {
            // 返回指定名称的本地库在当前平台上的名称，如从"net"映射到"net.dll"
            File libfile = new File(sys_paths[i], System.mapLibraryName(name));
            // 加载指定的本地库文件
            if (loadLibrary0(fromClass, libfile)) {
                return;
            }
            // 获取给定文件的备用路径名
            libfile = ClassLoaderHelper.mapAlternativeName(libfile);
            // 加载指定的本地库文件
            if (libfile != null && loadLibrary0(fromClass, libfile)) {
                return;
            }
        }
        // 遍历用户本地库路径
        if (loader != null) {
            for (int i = 0 ; i < usr_paths.length ; i++) {
                // 返回指定名称的本地库在当前平台上的名称，如从"net"映射到"net.dll"
                File libfile = new File(usr_paths[i],
                                        System.mapLibraryName(name));
                // 加载指定的本地库文件
                if (loadLibrary0(fromClass, libfile)) {
                    return;
                }
                // 获取给定文件的备用路径名
                libfile = ClassLoaderHelper.mapAlternativeName(libfile);
                if (libfile != null && loadLibrary0(fromClass, libfile)) {
                    return;
                }
            }
        }
        // Oops, it failed
        throw new UnsatisfiedLinkError("no " + name + " in java.library.path");
    }

    // 判断待加载的本地库是否为与VM关联的静态库，如果是的话，返回其名称
    private static native String findBuiltinLib(String name);

    // 加载指定的本地库文件
    private static boolean loadLibrary0(Class<?> fromClass, final File file) {
        // Check to see if we're attempting to access a static library
        // 判断待加载的本地库是否为与VM关联的静态库，如果是的话，返回其名称
        String name = findBuiltinLib(file.getName());
        // 是否为静态库
        boolean isBuiltin = (name != null);
        // 如果不是静态库
        if (!isBuiltin) {
            // 获取该动态库文件的规范名称
            boolean exists = AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
                    public Object run() {
                        return file.exists() ? Boolean.TRUE : null;
                    }})
                != null;
            if (NativeLibrary.loadLibraryOnlyIfPresent && !exists) {
                return false;
            }
            try {
                name = file.getCanonicalPath();
            } catch (IOException e) {
                return false;
            }
        }
        // 根据 fromClass 确定类加载器，选择相应的本地库列表。
        ClassLoader loader =
            (fromClass == null) ? null : fromClass.getClassLoader();
        // 在本地库列表中检查该库是否已加载，如果已加载则返回 true。
        Vector<NativeLibrary> libs =
            loader != null ? loader.nativeLibraries : systemNativeLibraries;
        synchronized (libs) {
            int size = libs.size();
            for (int i = 0; i < size; i++) {
                NativeLibrary lib = libs.elementAt(i);
                if (name.equals(lib.name)) {
                    return true;
                }
            }

            // 在 loadedLibraryNames 和 nativeLibraryContext 中检查该库是否正在被其他类加载器加载，如果正在加载则抛出异常。
            synchronized (loadedLibraryNames) {
                if (loadedLibraryNames.contains(name)) {
                    throw new UnsatisfiedLinkError
                        ("Native Library " +
                         name +
                         " already loaded in another classloader");
                }
                /* If the library is being loaded (must be by the same thread,
                 * because Runtime.load and Runtime.loadLibrary are
                 * synchronous). The reason is can occur is that the JNI_OnLoad
                 * function can cause another loadLibrary invocation.
                 *
                 * Thus we can use a static stack to hold the list of libraries
                 * we are loading.
                 *
                 * If there is a pending load operation for the library, we
                 * immediately return success; otherwise, we raise
                 * UnsatisfiedLinkError.
                 */
                int n = nativeLibraryContext.size();
                for (int i = 0; i < n; i++) {
                    NativeLibrary lib = nativeLibraryContext.elementAt(i);
                    if (name.equals(lib.name)) {
                        if (loader == lib.fromClass.getClassLoader()) {
                            return true;
                        } else {
                            throw new UnsatisfiedLinkError
                                ("Native Library " +
                                 name +
                                 " is being loaded in another classloader");
                        }
                    }
                }
                // 创建 NativeLibrary 对象并加载库文件，如果加载成功则更新相关列表并返回 true，否则返回 false。
                NativeLibrary lib = new NativeLibrary(fromClass, name, isBuiltin);
                nativeLibraryContext.push(lib);
                try {
                    lib.load(name, isBuiltin, NativeLibrary.loadLibraryOnlyIfPresent);
                } finally {
                    nativeLibraryContext.pop();
                }
                if (lib.loaded) {
                    loadedLibraryNames.addElement(name);
                    libs.addElement(lib);
                    return true;
                }
                return false;
            }
        }
    }

    // Invoked in the VM class linking code.

    /**
     * 在给定的类加载器中查找指定名称的本地库函数入口地址。
     * 1、获取本地库列表：
     *      如果 loader 不为 null，则使用 loader 的 nativeLibraries 列表。
     *      如果 loader 为 null，则使用 systemNativeLibraries 列表。
     * 2、同步访问本地库列表：
     *      使用 synchronized 关键字确保对本地库列表的访问是线程安全的。
     * 3、遍历本地库列表：
     *      遍历本地库列表，逐个调用每个 NativeLibrary 对象的 find 方法，尝试查找指定名称的函数入口地址。
     * 4、返回结果：
     *      如果在某个 NativeLibrary 中找到了匹配的函数入口地址，则立即返回该地址。
     *      如果遍历完所有本地库仍未找到匹配的函数入口地址，则返回 0。
     */
    static long findNative(ClassLoader loader, String name) {
        Vector<NativeLibrary> libs =
            loader != null ? loader.nativeLibraries : systemNativeLibraries;
        synchronized (libs) {
            int size = libs.size();
            for (int i = 0; i < size; i++) {
                NativeLibrary lib = libs.elementAt(i);
                long entry = lib.find(name);
                if (entry != 0)
                    return entry;
            }
        }
        return 0;
    }


    // -- Assertion management --

    final Object assertionLock;

    // The default toggle for assertion checking.
    // @GuardedBy("assertionLock")
    private boolean defaultAssertionStatus = false;

    // Maps String packageName to Boolean package default assertion status Note
    // that the default package is placed under a null map key.  If this field
    // is null then we are delegating assertion status queries to the VM, i.e.,
    // none of this ClassLoader's assertion status modification methods have
    // been invoked.
    // @GuardedBy("assertionLock")
    private Map<String, Boolean> packageAssertionStatus = null;

    // Maps String fullyQualifiedClassName to Boolean assertionStatus If this
    // field is null then we are delegating assertion status queries to the VM,
    // i.e., none of this ClassLoader's assertion status modification methods
    // have been invoked.
    // @GuardedBy("assertionLock")
    Map<String, Boolean> classAssertionStatus = null;

    /**
     * Sets the default assertion status for this class loader.  This setting
     * determines whether classes loaded by this class loader and initialized
     * in the future will have assertions enabled or disabled by default.
     * This setting may be overridden on a per-package or per-class basis by
     * invoking {@link #setPackageAssertionStatus(String, boolean)} or {@link
     * #setClassAssertionStatus(String, boolean)}.
     *
     * @param  enabled
     *         <tt>true</tt> if classes loaded by this class loader will
     *         henceforth have assertions enabled by default, <tt>false</tt>
     *         if they will have assertions disabled by default.
     *
     * @since  1.4
     */
    public void setDefaultAssertionStatus(boolean enabled) {
        synchronized (assertionLock) {
            if (classAssertionStatus == null)
                initializeJavaAssertionMaps();

            defaultAssertionStatus = enabled;
        }
    }

    /**
     * Sets the package default assertion status for the named package.  The
     * package default assertion status determines the assertion status for
     * classes initialized in the future that belong to the named package or
     * any of its "subpackages".
     *
     * <p> A subpackage of a package named p is any package whose name begins
     * with "<tt>p.</tt>".  For example, <tt>javax.swing.text</tt> is a
     * subpackage of <tt>javax.swing</tt>, and both <tt>java.util</tt> and
     * <tt>java.lang.reflect</tt> are subpackages of <tt>java</tt>.
     *
     * <p> In the event that multiple package defaults apply to a given class,
     * the package default pertaining to the most specific package takes
     * precedence over the others.  For example, if <tt>javax.lang</tt> and
     * <tt>javax.lang.reflect</tt> both have package defaults associated with
     * them, the latter package default applies to classes in
     * <tt>javax.lang.reflect</tt>.
     *
     * <p> Package defaults take precedence over the class loader's default
     * assertion status, and may be overridden on a per-class basis by invoking
     * {@link #setClassAssertionStatus(String, boolean)}.  </p>
     *
     * @param  packageName
     *         The name of the package whose package default assertion status
     *         is to be set. A <tt>null</tt> value indicates the unnamed
     *         package that is "current"
     *         (see section 7.4.2 of
     *         <cite>The Java&trade; Language Specification</cite>.)
     *
     * @param  enabled
     *         <tt>true</tt> if classes loaded by this classloader and
     *         belonging to the named package or any of its subpackages will
     *         have assertions enabled by default, <tt>false</tt> if they will
     *         have assertions disabled by default.
     *
     * @since  1.4
     */
    public void setPackageAssertionStatus(String packageName,
                                          boolean enabled) {
        synchronized (assertionLock) {
            if (packageAssertionStatus == null)
                initializeJavaAssertionMaps();

            packageAssertionStatus.put(packageName, enabled);
        }
    }

    /**
     * Sets the desired assertion status for the named top-level class in this
     * class loader and any nested classes contained therein.  This setting
     * takes precedence over the class loader's default assertion status, and
     * over any applicable per-package default.  This method has no effect if
     * the named class has already been initialized.  (Once a class is
     * initialized, its assertion status cannot change.)
     *
     * <p> If the named class is not a top-level class, this invocation will
     * have no effect on the actual assertion status of any class. </p>
     *
     * @param  className
     *         The fully qualified class name of the top-level class whose
     *         assertion status is to be set.
     *
     * @param  enabled
     *         <tt>true</tt> if the named class is to have assertions
     *         enabled when (and if) it is initialized, <tt>false</tt> if the
     *         class is to have assertions disabled.
     *
     * @since  1.4
     */
    public void setClassAssertionStatus(String className, boolean enabled) {
        synchronized (assertionLock) {
            if (classAssertionStatus == null)
                initializeJavaAssertionMaps();

            classAssertionStatus.put(className, enabled);
        }
    }

    /**
     * Sets the default assertion status for this class loader to
     * <tt>false</tt> and discards any package defaults or class assertion
     * status settings associated with the class loader.  This method is
     * provided so that class loaders can be made to ignore any command line or
     * persistent assertion status settings and "start with a clean slate."
     *
     * @since  1.4
     */
    public void clearAssertionStatus() {
        /*
         * Whether or not "Java assertion maps" are initialized, set
         * them to empty maps, effectively ignoring any present settings.
         */
        synchronized (assertionLock) {
            classAssertionStatus = new HashMap<>();
            packageAssertionStatus = new HashMap<>();
            defaultAssertionStatus = false;
        }
    }

    /**
     * Returns the assertion status that would be assigned to the specified
     * class if it were to be initialized at the time this method is invoked.
     * If the named class has had its assertion status set, the most recent
     * setting will be returned; otherwise, if any package default assertion
     * status pertains to this class, the most recent setting for the most
     * specific pertinent package default assertion status is returned;
     * otherwise, this class loader's default assertion status is returned.
     * </p>
     *
     * @param  className
     *         The fully qualified class name of the class whose desired
     *         assertion status is being queried.
     *
     * @return  The desired assertion status of the specified class.
     *
     * @see  #setClassAssertionStatus(String, boolean)
     * @see  #setPackageAssertionStatus(String, boolean)
     * @see  #setDefaultAssertionStatus(boolean)
     *
     * @since  1.4
     */
    boolean desiredAssertionStatus(String className) {
        synchronized (assertionLock) {
            // assert classAssertionStatus   != null;
            // assert packageAssertionStatus != null;

            // Check for a class entry
            Boolean result = classAssertionStatus.get(className);
            if (result != null)
                return result.booleanValue();

            // Check for most specific package entry
            int dotIndex = className.lastIndexOf(".");
            if (dotIndex < 0) { // default package
                result = packageAssertionStatus.get(null);
                if (result != null)
                    return result.booleanValue();
            }
            while(dotIndex > 0) {
                className = className.substring(0, dotIndex);
                result = packageAssertionStatus.get(className);
                if (result != null)
                    return result.booleanValue();
                dotIndex = className.lastIndexOf(".", dotIndex-1);
            }

            // Return the classloader default
            return defaultAssertionStatus;
        }
    }

    // Set up the assertions with information provided by the VM.
    // Note: Should only be called inside a synchronized block
    private void initializeJavaAssertionMaps() {
        // assert Thread.holdsLock(assertionLock);

        classAssertionStatus = new HashMap<>();
        packageAssertionStatus = new HashMap<>();
        AssertionStatusDirectives directives = retrieveDirectives();

        for(int i = 0; i < directives.classes.length; i++)
            classAssertionStatus.put(directives.classes[i],
                                     directives.classEnabled[i]);

        for(int i = 0; i < directives.packages.length; i++)
            packageAssertionStatus.put(directives.packages[i],
                                       directives.packageEnabled[i]);

        defaultAssertionStatus = directives.deflt;
    }

    // Retrieves the assertion directives from the VM.
    private static native AssertionStatusDirectives retrieveDirectives();
}


class SystemClassLoaderAction
    implements PrivilegedExceptionAction<ClassLoader> {
    private ClassLoader parent;

    SystemClassLoaderAction(ClassLoader parent) {
        this.parent = parent;
    }

    public ClassLoader run() throws Exception {
        String cls = System.getProperty("java.system.class.loader");
        if (cls == null) {
            return parent;
        }

        Constructor<?> ctor = Class.forName(cls, true, parent)
            .getDeclaredConstructor(new Class<?>[] { ClassLoader.class });
        ClassLoader sys = (ClassLoader) ctor.newInstance(
            new Object[] { parent });
        Thread.currentThread().setContextClassLoader(sys);
        return sys;
    }
}

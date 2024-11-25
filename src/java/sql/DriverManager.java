/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.CopyOnWriteArrayList;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;


/**
 * <P>The basic service for managing a set of JDBC drivers.<br>
 * <B>NOTE:</B> The {@link javax.sql.DataSource} interface, new in the
 * JDBC 2.0 API, provides another way to connect to a data source.
 * The use of a <code>DataSource</code> object is the preferred means of
 * connecting to a data source.
 *
 * <P>As part of its initialization, the <code>DriverManager</code> class will
 * attempt to load the driver classes referenced in the "jdbc.drivers"
 * system property. This allows a user to customize the JDBC Drivers
 * used by their applications. For example in your
 * ~/.hotjava/properties file you might specify:
 * <pre>
 * <CODE>jdbc.drivers=foo.bah.Driver:wombat.sql.Driver:bad.taste.ourDriver</CODE>
 * </pre>
 *<P> The <code>DriverManager</code> methods <code>getConnection</code> and
 * <code>getDrivers</code> have been enhanced to support the Java Standard Edition
 * <a href="../../../technotes/guides/jar/jar.html#Service%20Provider">Service Provider</a> mechanism. JDBC 4.0 Drivers must
 * include the file <code>META-INF/services/java.sql.Driver</code>. This file contains the name of the JDBC drivers
 * implementation of <code>java.sql.Driver</code>.  For example, to load the <code>my.sql.Driver</code> class,
 * the <code>META-INF/services/java.sql.Driver</code> file would contain the entry:
 * <pre>
 * <code>my.sql.Driver</code>
 * </pre>
 *
 * <P>Applications no longer need to explicitly load JDBC drivers using <code>Class.forName()</code>. Existing programs
 * which currently load JDBC drivers using <code>Class.forName()</code> will continue to work without
 * modification.
 *
 * <P>When the method <code>getConnection</code> is called,
 * the <code>DriverManager</code> will attempt to
 * locate a suitable driver from amongst those loaded at
 * initialization and those loaded explicitly using the same classloader
 * as the current applet or application.
 *
 * <P>
 * Starting with the Java 2 SDK, Standard Edition, version 1.3, a
 * logging stream can be set only if the proper
 * permission has been granted.  Normally this will be done with
 * the tool PolicyTool, which can be used to grant <code>permission
 * java.sql.SQLPermission "setLog"</code>.
 * @see Driver
 * @see Connection
 */
// 用于管理和加载JDBC驱动
// https://blog.csdn.net/m0_45067620/article/details/138703264
public class DriverManager {


    // List of registered JDBC drivers
    private final static CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<>();
    private static volatile int loginTimeout = 0;
    private static volatile java.io.PrintWriter logWriter = null;
    private static volatile java.io.PrintStream logStream = null;
    // Used in println() to synchronize logWriter
    private final static  Object logSync = new Object();

    /* Prevent the DriverManager class from being instantiated. */
    private DriverManager(){}


    /**
     * Load the initial JDBC drivers by checking the System property
     * jdbc.properties and then use the {@code ServiceLoader} mechanism
     */
    static {
        loadInitialDrivers();
        println("JDBC DriverManager initialized");
    }

    /**
     * The <code>SQLPermission</code> constant that allows the
     * setting of the logging stream.
     * @since 1.3
     */
    final static SQLPermission SET_LOG_PERMISSION =
        new SQLPermission("setLog");

    /**
     * The {@code SQLPermission} constant that allows the
     * un-register a registered JDBC driver.
     * @since 1.8
     */
    final static SQLPermission DEREGISTER_DRIVER_PERMISSION =
        new SQLPermission("deregisterDriver");

    //--------------------------JDBC 2.0-----------------------------

    /**
     * Retrieves the log writer.
     *
     * The <code>getLogWriter</code> and <code>setLogWriter</code>
     * methods should be used instead
     * of the <code>get/setlogStream</code> methods, which are deprecated.
     * @return a <code>java.io.PrintWriter</code> object
     * @see #setLogWriter
     * @since 1.2
     */
    public static java.io.PrintWriter getLogWriter() {
            return logWriter;
    }

    /**
     * Sets the logging/tracing <code>PrintWriter</code> object
     * that is used by the <code>DriverManager</code> and all drivers.
     * <P>
     * There is a minor versioning problem created by the introduction
     * of the method <code>setLogWriter</code>.  The
     * method <code>setLogWriter</code> cannot create a <code>PrintStream</code> object
     * that will be returned by <code>getLogStream</code>---the Java platform does
     * not provide a backward conversion.  As a result, a new application
     * that uses <code>setLogWriter</code> and also uses a JDBC 1.0 driver that uses
     * <code>getLogStream</code> will likely not see debugging information written
     * by that driver.
     *<P>
     * Starting with the Java 2 SDK, Standard Edition, version 1.3 release, this method checks
     * to see that there is an <code>SQLPermission</code> object before setting
     * the logging stream.  If a <code>SecurityManager</code> exists and its
     * <code>checkPermission</code> method denies setting the log writer, this
     * method throws a <code>java.lang.SecurityException</code>.
     *
     * @param out the new logging/tracing <code>PrintStream</code> object;
     *      <code>null</code> to disable logging and tracing
     * @throws SecurityException
     *    if a security manager exists and its
     *    <code>checkPermission</code> method denies
     *    setting the log writer
     *
     * @see SecurityManager#checkPermission
     * @see #getLogWriter
     * @since 1.2
     */
    public static void setLogWriter(java.io.PrintWriter out) {

        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_LOG_PERMISSION);
        }
            logStream = null;
            logWriter = out;
    }


    //---------------------------------------------------------------

    /**
     * Attempts to establish a connection to the given database URL.
     * The <code>DriverManager</code> attempts to select an appropriate driver from
     * the set of registered JDBC drivers.
     *<p>
     * <B>Note:</B> If a property is specified as part of the {@code url} and
     * is also specified in the {@code Properties} object, it is
     * implementation-defined as to which value will take precedence.
     * For maximum portability, an application should only specify a
     * property once.
     *
     * @param url a database url of the form
     * <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param info a list of arbitrary string tag/value pairs as
     * connection arguments; normally at least a "user" and
     * "password" property should be included
     * @return a Connection to the URL
     * @exception SQLException if a database access error occurs or the url is
     * {@code null}
     * @throws SQLTimeoutException  when the driver has determined that the
     * timeout value specified by the {@code setLoginTimeout} method
     * has been exceeded and has at least tried to cancel the
     * current database connection attempt
     */
    // 根据 Url 和 Properties 参数创建连接
    @CallerSensitive
    public static Connection getConnection(String url,
        java.util.Properties info) throws SQLException {

        return (getConnection(url, info, Reflection.getCallerClass()));
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * The <code>DriverManager</code> attempts to select an appropriate driver from
     * the set of registered JDBC drivers.
     *<p>
     * <B>Note:</B> If the {@code user} or {@code password} property are
     * also specified as part of the {@code url}, it is
     * implementation-defined as to which value will take precedence.
     * For maximum portability, an application should only specify a
     * property once.
     *
     * @param url a database url of the form
     * <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param user the database user on whose behalf the connection is being
     *   made
     * @param password the user's password
     * @return a connection to the URL
     * @exception SQLException if a database access error occurs or the url is
     * {@code null}
     * @throws SQLTimeoutException  when the driver has determined that the
     * timeout value specified by the {@code setLoginTimeout} method
     * has been exceeded and has at least tried to cancel the
     * current database connection attempt
     */
    // 根据 Url 、 用户名 、密码 创建连接
    @CallerSensitive
    public static Connection getConnection(String url,
        String user, String password) throws SQLException {
        java.util.Properties info = new java.util.Properties();

        if (user != null) {
            info.put("user", user);
        }
        if (password != null) {
            info.put("password", password);
        }

        return (getConnection(url, info, Reflection.getCallerClass()));
    }

    /**
     * Attempts to establish a connection to the given database URL.
     * The <code>DriverManager</code> attempts to select an appropriate driver from
     * the set of registered JDBC drivers.
     *
     * @param url a database url of the form
     *  <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @return a connection to the URL
     * @exception SQLException if a database access error occurs or the url is
     * {@code null}
     * @throws SQLTimeoutException  when the driver has determined that the
     * timeout value specified by the {@code setLoginTimeout} method
     * has been exceeded and has at least tried to cancel the
     * current database connection attempt
     */
    // 根据url创建数据库连接
    @CallerSensitive
    public static Connection getConnection(String url)
        throws SQLException {

        java.util.Properties info = new java.util.Properties();
        return (getConnection(url, info, Reflection.getCallerClass()));
    }

    /**
     * Attempts to locate a driver that understands the given URL.
     * The <code>DriverManager</code> attempts to select an appropriate driver from
     * the set of registered JDBC drivers.
     *
     * @param url a database URL of the form
     *     <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @return a <code>Driver</code> object representing a driver
     * that can connect to the given URL
     * @exception SQLException if a database access error occurs
     */
    // 根据给定的数据库URL获取相应的数据库驱动程序
    @CallerSensitive
    public static Driver getDriver(String url)
        throws SQLException {

        println("DriverManager.getDriver(\"" + url + "\")");

        Class<?> callerClass = Reflection.getCallerClass();

        // Walk through the loaded registeredDrivers attempting to locate someone
        // who understands the given URL.
        for (DriverInfo aDriver : registeredDrivers) {
            // If the caller does not have permission to load the driver then
            // skip it.
            if(isDriverAllowed(aDriver.driver, callerClass)) {
                try {
                    if(aDriver.driver.acceptsURL(url)) {
                        // Success!
                        println("getDriver returning " + aDriver.driver.getClass().getName());
                    return (aDriver.driver);
                    }

                } catch(SQLException sqe) {
                    // Drop through and try the next driver.
                }
            } else {
                println("    skipping: " + aDriver.driver.getClass().getName());
            }

        }

        println("getDriver: no suitable driver");
        throw new SQLException("No suitable driver", "08001");
    }


    /**
     * Registers the given driver with the {@code DriverManager}.
     * A newly-loaded driver class should call
     * the method {@code registerDriver} to make itself
     * known to the {@code DriverManager}. If the driver is currently
     * registered, no action is taken.
     *
     * @param driver the new JDBC Driver that is to be registered with the
     *               {@code DriverManager}
     * @exception SQLException if a database access error occurs
     * @exception NullPointerException if {@code driver} is null
     */
    // 注册驱动的方法
    public static synchronized void registerDriver(java.sql.Driver driver)
        throws SQLException {
        // 调用下面的方法
        registerDriver(driver, null);
    }

    /**
     * Registers the given driver with the {@code DriverManager}.
     * A newly-loaded driver class should call
     * the method {@code registerDriver} to make itself
     * known to the {@code DriverManager}. If the driver is currently
     * registered, no action is taken.
     *
     * @param driver the new JDBC Driver that is to be registered with the
     *               {@code DriverManager}
     * @param da     the {@code DriverAction} implementation to be used when
     *               {@code DriverManager#deregisterDriver} is called
     * @exception SQLException if a database access error occurs
     * @exception NullPointerException if {@code driver} is null
     * @since 1.8
     */
    // 注册驱动的方法
    public static synchronized void registerDriver(java.sql.Driver driver,
            DriverAction da)
        throws SQLException {

        /* Register the driver if it has not already been added to our list */
        if(driver != null) {
            registeredDrivers.addIfAbsent(new DriverInfo(driver, da));
        } else {
            // This is for compatibility with the original DriverManager
            throw new NullPointerException();
        }

        println("registerDriver: " + driver);

    }

    /**
     * Removes the specified driver from the {@code DriverManager}'s list of
     * registered drivers.
     * <p>
     * If a {@code null} value is specified for the driver to be removed, then no
     * action is taken.
     * <p>
     * If a security manager exists and its {@code checkPermission} denies
     * permission, then a {@code SecurityException} will be thrown.
     * <p>
     * If the specified driver is not found in the list of registered drivers,
     * then no action is taken.  If the driver was found, it will be removed
     * from the list of registered drivers.
     * <p>
     * If a {@code DriverAction} instance was specified when the JDBC driver was
     * registered, its deregister method will be called
     * prior to the driver being removed from the list of registered drivers.
     *
     * @param driver the JDBC Driver to remove
     * @exception SQLException if a database access error occurs
     * @throws SecurityException if a security manager exists and its
     * {@code checkPermission} method denies permission to deregister a driver.
     *
     * @see SecurityManager#checkPermission
     */
    // 注销driver方法，用于从DriverManager中注销指定的数据库驱动程序
    @CallerSensitive
    public static synchronized void deregisterDriver(Driver driver)
        throws SQLException {
        if (driver == null) {
            return;
        }

        // 安全检查，如果存在安全管理器，则调用其checkPermission方法
        // 检查是否有deregisterDriver（注销驱动）权限
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(DEREGISTER_DRIVER_PERMISSION);
        }

        // 记录日志
        println("DriverManager.deregisterDriver: " + driver);

        // 查找并移除驱动
        DriverInfo aDriver = new DriverInfo(driver, null);
        if(registeredDrivers.contains(aDriver)) {
            if (isDriverAllowed(driver, Reflection.getCallerClass())) {
                DriverInfo di = registeredDrivers.get(registeredDrivers.indexOf(aDriver));
                 // If a DriverAction was specified, Call it to notify the
                 // driver that it has been deregistered
                 if(di.action() != null) {
                     di.action().deregister();
                 }
                 registeredDrivers.remove(aDriver);
            } else {
                // If the caller does not have permission to load the driver then
                // throw a SecurityException.
                throw new SecurityException();
            }
        } else {
            println("    couldn't find driver to unload");
        }
    }

    /**
     * Retrieves an Enumeration with all of the currently loaded JDBC drivers
     * to which the current caller has access.
     *
     * <P><B>Note:</B> The classname of a driver can be found using
     * <CODE>d.getClass().getName()</CODE>
     *
     * @return the list of JDBC Drivers loaded by the caller's class loader
     */
    // 获取全部已加载的去驱动程序
    @CallerSensitive
    public static java.util.Enumeration<Driver> getDrivers() {
        java.util.Vector<Driver> result = new java.util.Vector<>();

        Class<?> callerClass = Reflection.getCallerClass();

        // Walk through the loaded registeredDrivers.
        for(DriverInfo aDriver : registeredDrivers) {
            // If the caller does not have permission to load the driver then
            // skip it.
            if(isDriverAllowed(aDriver.driver, callerClass)) {
                result.addElement(aDriver.driver);
            } else {
                println("    skipping: " + aDriver.getClass().getName());
            }
        }
        return (result.elements());
    }


    /**
     * Sets the maximum time in seconds that a driver will wait
     * while attempting to connect to a database once the driver has
     * been identified.
     *
     * @param seconds the login time limit in seconds; zero means there is no limit
     * @see #getLoginTimeout
     */
    // 设置识别驱动程序后，驱动程序在尝试连接到数据库时将等待的最长时间（以秒为单位）。
    public static void setLoginTimeout(int seconds) {
        loginTimeout = seconds;
    }

    /**
     * Gets the maximum time in seconds that a driver can wait
     * when attempting to log in to a database.
     *
     * @return the driver login time limit in seconds
     * @see #setLoginTimeout
     */
    // 获取驱动程序在尝试登录数据库时可以等待的最长时间（以秒为单位）。
    public static int getLoginTimeout() {
        return (loginTimeout);
    }

    /**
     * Sets the logging/tracing PrintStream that is used
     * by the <code>DriverManager</code>
     * and all drivers.
     *<P>
     * In the Java 2 SDK, Standard Edition, version 1.3 release, this method checks
     * to see that there is an <code>SQLPermission</code> object before setting
     * the logging stream.  If a <code>SecurityManager</code> exists and its
     * <code>checkPermission</code> method denies setting the log writer, this
     * method throws a <code>java.lang.SecurityException</code>.
     *
     * @param out the new logging/tracing PrintStream; to disable, set to <code>null</code>
     * @deprecated Use {@code setLogWriter}
     * @throws SecurityException if a security manager exists and its
     *    <code>checkPermission</code> method denies setting the log stream
     *
     * @see SecurityManager#checkPermission
     * @see #getLogStream
     */
    @Deprecated
    public static void setLogStream(java.io.PrintStream out) {

        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_LOG_PERMISSION);
        }

        logStream = out;
        if ( out != null )
            logWriter = new java.io.PrintWriter(out);
        else
            logWriter = null;
    }

    /**
     * Retrieves the logging/tracing PrintStream that is used by the <code>DriverManager</code>
     * and all drivers.
     *
     * @return the logging/tracing PrintStream; if disabled, is <code>null</code>
     * @deprecated  Use {@code getLogWriter}
     * @see #setLogStream
     */
    @Deprecated
    public static java.io.PrintStream getLogStream() {
        return logStream;
    }

    /**
     * Prints a message to the current JDBC log stream.
     *
     * @param message a log or tracing message
     */
    public static void println(String message) {
        synchronized (logSync) {
            if (logWriter != null) {
                logWriter.println(message);

                // automatic flushing is never enabled, so we must do it ourselves
                logWriter.flush();
            }
        }
    }

    //------------------------------------------------------------------------

    // Indicates whether the class object that would be created if the code calling
    // DriverManager is accessible.
    // 校验已经注册的驱动的合法性
    private static boolean isDriverAllowed(Driver driver, Class<?> caller) {
        ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
        return isDriverAllowed(driver, callerCL);
    }

    // isDriverAllowed 方法的目的就是为了校验已经注册的驱动的合法性，
    // 如果驱动被注册时使用的类加载器和调用 getConnection()所使用类的的类加载不是同一个，
    // 那么驱动将不被允许使用。
    // 检查给定的Driver对象是否在指定的ClassLoader中时允许的
    // 1、参数检查：
    //      如果 driver 为 null，则直接返回 false。
    // 2、类加载：
    //      尝试使用 ClassLoader 加载 driver 对象的类。
    // 3、异常处理：
    //      如果加载过程中发生异常，则返回 false。
    // 4、类比较：
    //      比较加载的类与 driver 对象的类是否相同，相同则返回 true，否则返回 false。
    private static boolean isDriverAllowed(Driver driver, ClassLoader classLoader) {
        boolean result = false;
        if(driver != null) {
            Class<?> aClass = null;
            try {
                // 获取isDriverAllowed中第一个参数的全类名，并设置初始化该类（会执行静态方法），拿到class
                aClass =  Class.forName(driver.getClass().getName(), true, classLoader);
            } catch (Exception ex) {
                result = false;
            }

            // 判断拿到的class是否与该方法的第一个参数相同
            // 其目的是为了确保事先注册的驱动与当前的驱动是通过同一个类加载器加载的
            // 为什么需要判断是否通过同一个类加载器加载？
            // 1、类加载器的作用：
            //      类加载器负责将类加载到JVM中。不同的类加载器可能会加载同一个类
            //      的不同版本，或者加载完全不同的类。
            //      （例如一个应用程序可能有两个版本的lib.v1.com.example.MyClass和lib.v2.com.example.MyClass，一个在lib/v1目录下，另外一个在lib/v2目录下）
            // 2、确保类的一致性：
            //      通过同一个类加载器加载的类在JVM中被视为相同的类
            // 3、防止类冲突：
            //      在多模块或多应用环境中，不同的模块可能使用不同的类加载器。
            //      如果 driver 类通过不同的类加载器加载，
            //      可能会导致类冲突或不一致的行为。
            // 4、安全性：
            //      确保 driver 类是由预期的类加载器加载的，
            //      可以提高系统的安全性，防止恶意代码通过不同的类加载器加载相同的类。
            result = ( aClass == driver.getClass() ) ? true : false;
        }

        return result;
    }

    // 需要注意的是，从JDBC4.0开始，Java SE 6及更高版本中的JDBC驱动程序已经支持自动加载，
    // 不再需要显式调用Class.forName(driverName)。
    // 因此，在使用较新的JDBC驱动程序版本和Java SE 6及更高版本时，
    // 通常不需要为jdbc驱动程序指定Class.forName(driverName)。
    // 完成对厂商驱动的加载，尝试驱动会利用驱动中的代码实现对自己驱动对象的实例化，
    // 即注册到DriverManager中的registeredDrivers集合中。
    // 驱动的注册过程有两种：
    // 1、利用ServiceLoader的规则扫描特定的文件完成对文件中指定类的加载和初始化
    // 2、通过将驱动的类路径写入到java的系统属性值jdbc.drivers中，显示的使用Class.forName完成对指定的加载和初始化
    private static void loadInitialDrivers() {
        String drivers;
        try {
            // 使用 AccessController.doPrivileged() 方法执行一些可能具有安全权限限制的代码块
            // 这个写法确实比较严谨啊
            drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    // 获取Java的系统属性值`jdbc.drivers`,目的后面再讲
                    // 通过将驱动的类路径写入到Java的系统属性值jdbc.drivers中,
                    // 显示的使用Class.forName完成对指定类的加载和初始化
                    return System.getProperty("jdbc.drivers");
                }
            });
        } catch (Exception ex) {
            drivers = null;
        }
        // If the driver is packaged as a Service Provider, load it.
        // Get all the drivers through the classloader
        // exposed as a java.sql.Driver.class service.
        // ServiceLoader.load() replaces the sun.misc.Providers()

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                // 下面方法中的代码就是ServiceLoader完成对驱动厂商提供的驱动的加载，即寻找META-INF/services/java.sql.Drive
                // 并完成对java.sql.Drive中配置的类的加载。
                // 具体的实现原理就不细讲了，最终会通过Class.forName和newInstance()完成对类的加载，源码大概是这么做的
                // Class<?> c = Class.forName(cn, false, loader);
                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
                Iterator<Driver> driversIterator = loadedDrivers.iterator();

                /* Load these drivers, so that they can be instantiated.
                 * It may be the case that the driver class may not be there
                 * i.e. there may be a packaged driver with the service class
                 * as implementation of java.sql.Driver but the actual class
                 * may be missing. In that case a java.util.ServiceConfigurationError
                 * will be thrown at runtime by the VM trying to locate
                 * and load the service.
                 *
                 * Adding a try catch block to catch those runtime errors
                 * if driver not available in classpath but it's
                 * packaged as service and that service is there in classpath.
                 */
                try{
                    while(driversIterator.hasNext()) {
                        driversIterator.next();
                    }
                } catch(Throwable t) {
                // Do nothing
                }
                return null;
            }
        });

        println("DriverManager.initialize: jdbc.drivers = " + drivers);

        if (drivers == null || drivers.equals("")) {
            return;
        }
        // 下面的代码就是对Java的系统属性值`jdbc.drivers`，的内容进行切分，并对切分的内容进行逐个的使用
        // Class.forName 完成对目标对象的加载，由此可见我们可以通过将驱动类路径使用":"拼接并存储到
        // Java的系统属性'jdbc.drivers'中，DriverManager也是可以完成对驱动的加载的。
        String[] driversList = drivers.split(":");
        println("number of Drivers:" + driversList.length);
        for (String aDriver : driversList) {
            try {
                println("DriverManager.Initialize: loading " + aDriver);
                // 注意第二个参数是true，表明会进行对类的初始化，即类中的静态代码块会被执行。
                Class.forName(aDriver, true,
                        java.lang.ClassLoader.getSystemClassLoader());
            } catch (Exception ex) {
                println("DriverManager.Initialize: load failed: " + ex);
            }
        }
    }


    //  Worker method called by the public getConnection() methods.
    // 根据 Url 和 Properties 参数使用调用者创建连接
    // 1、检查调用者类加载器：
    //    如果caller不为null，使用其类加载器；否则，同步获取当前线程的上下文类加载器。
    // 2、验证URL：
    //    如果URL为null，抛出SQLException，提示URL不能为null。
    // 2、尝试连接：
    //    遍历已注册的驱动程序，对于每个驱动程序，检查调用者是否有权限加载该驱动。
    //    如果有权限，尝试使用该驱动连接数据库。
    //    如果连接成功，返回连接对象。
    //    如果连接失败，记录第一个发生的SQLException。
    // 2、处理异常：
    //    如果所有驱动程序都无法连接，检查是否记录了异常。
    //    如果记录了异常，抛出该异常；否则，抛出“无合适驱动”异常。
    private static Connection getConnection(
        String url, java.util.Properties info, Class<?> caller) throws SQLException {
        /*
         * When callerCl is null, we should check the application's
         * (which is invoking this class indirectly)
         * classloader, so that the JDBC driver class outside rt.jar
         * can be loaded from here.
         */
        // 获取类加载器
        ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
        synchronized(DriverManager.class) {
            // synchronize loading of the correct classloader.
            // 如果类加载器为空，那么获取当前线程的类加载器
            if (callerCL == null) {
                callerCL = Thread.currentThread().getContextClassLoader();
            }
        }

        if(url == null) {
            throw new SQLException("The url cannot be null", "08001");
        }

        println("DriverManager.getConnection(\"" + url + "\")");

        // Walk through the loaded registeredDrivers attempting to make a connection.
        // Remember the first exception that gets raised so we can reraise it.
        // 记录异常
        SQLException reason = null;

        // 遍历已经注册过的驱动，这个地方大家可能会存在疑惑：如果没有使用Class.forName("com.taosdata.jdbc.rs.RestfulDriver")
		// 去主动的注册驱动，那么DriverManager中的registeredDrivers是怎么完成注册的？
		// 在 DriverManager 中存在一段静态代码块loadInitialDrivers()，其目的就是完成对厂商驱动的加载，厂商驱动会利用驱动中的代码代码块实现对自己驱动的实例化对象的注册，即注册到 DriverManager 中的 registeredDrivers。
        for(DriverInfo aDriver : registeredDrivers) {
            // If the caller does not have permission to load the driver then
            // skip it.
            // 检测驱动是不是被允许，这里只是检测registeredDrivers是否真的存在于上下文代码中
            if(isDriverAllowed(aDriver.driver, callerCL)) {
                try {
                    println("    trying " + aDriver.driver.getClass().getName());
                    // 调用注册到registeredDrivers中的驱动的connect方法（即各个厂商提供的驱动程序）
                    Connection con = aDriver.driver.connect(url, info);
                    // 如果con不为null，则说明成功获取的数据库连接。
                    // 上文中我们说过getConnection方法会动态的获取合适的驱动，就是通过此处代码片段实现的
                    // 首先，在getConnection中尝试遍历了所有的已经注册的驱动，并调用了驱动的connect方法，尝试获取了连接
                    // 如果connect方法的返回值为null，或者抛出了异常，那么说明该驱动不是合适的驱动
                    // 有因为该段代码块被try-catch，所以即时抛出异常，循环也会继续，直到找到合适的驱动或循环结束。
                    // 说白了，DriverManager 取了个巧，通过校验厂商的connect方法会不会抛出异常或返回null，来判断驱动是否合适。
                    if (con != null) {
                        // Success!
                        println("getConnection returning " + aDriver.driver.getClass().getName());
                        return (con);
                    }
                } catch (SQLException ex) {
                    if (reason == null) {
                        reason = ex;
                    }
                }

            } else {
                println("    skipping: " + aDriver.getClass().getName());
            }

        }

        // if we got here nobody could connect.
        // 能到这，说明已经获取连接失败了
        if (reason != null)    {
            println("getConnection failed: " + reason);
            throw reason;
        }

        println("getConnection: no suitable driver found for "+ url);
        throw new SQLException("No suitable driver found for "+ url, "08001");
    }


}

/*
 * Wrapper class for registered Drivers in order to not expose Driver.equals()
 * to avoid the capture of the Driver it being compared to as it might not
 * normally have access.
 */
class DriverInfo {

    final Driver driver;
    DriverAction da;
    DriverInfo(Driver driver, DriverAction action) {
        this.driver = driver;
        da = action;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof DriverInfo)
                && this.driver == ((DriverInfo) other).driver;
    }

    @Override
    public int hashCode() {
        return driver.hashCode();
    }

    @Override
    public String toString() {
        return ("driver[className="  + driver + "]");
    }

    DriverAction action() {
        return da;
    }
}

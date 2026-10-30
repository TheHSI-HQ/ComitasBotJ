package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The static rule set of the plugin sandbox.
 * <p>
 * Everything is denied by default. A plugin may only reference its own classes and classes
 * explicitly allowed here. On top of that, single members of allowed classes can be denied
 * (or, for some classes, only a few members are allowed).
 * <p>
 * The Discord token lives in core / JDA objects, so the core is never reachable:
 * neither by name (class loader + this policy), nor through reflection, method handles,
 * serialization, native code, processes, JMX or the file system.
 */
final class SandboxPolicy {
    enum Access {ALLOW, DENY, NETWORK}

    /**
     * Class name prefixes, which are never loaded from a plugin jar.
     */
    private static final List<String> RESERVED_PREFIXES = List.of(
            "java/", "javax/", "jdk/", "sun/", "com/sun/",
            "cloud/thehsi/ComitasBotJ/", "cloud/thehsi/ComitasBasePlugin/",
            "net/dv8tion/", "org/slf4j/", "org/jetbrains/annotations/"
    );

    /**
     * Keys ending in '/' are packages (including sub packages), all others classes (including nested classes).
     * The most specific rule wins.
     */
    private static final Map<String, Access> CLASS_RULES = new HashMap<>();
    private static final Map<String, MemberRule> MEMBER_RULES = new HashMap<>();
    private static final Set<String> ALLOWED_BOOTSTRAPS = Set.of(
            "java/lang/invoke/LambdaMetafactory.metafactory",
            "java/lang/invoke/LambdaMetafactory.altMetafactory",
            "java/lang/invoke/StringConcatFactory.makeConcatWithConstants",
            "java/lang/invoke/StringConcatFactory.makeConcat",
            "java/lang/runtime/ObjectMethods.bootstrap",
            "java/lang/runtime/SwitchBootstraps.typeSwitch",
            "java/lang/runtime/SwitchBootstraps.enumSwitch"
    );

    static {
        // --- java.lang ---
        allow("java/lang/");
        deny("java/lang/ClassLoader", "java/lang/Module", "java/lang/ModuleLayer", "java/lang/Package",
                "java/lang/Process", "java/lang/ProcessBuilder", "java/lang/ProcessHandle",
                "java/lang/SecurityManager", "java/lang/StackWalker", "java/lang/ThreadGroup",
                "java/lang/System$Logger", "java/lang/System$LoggerFinder");
        deny("java/lang/invoke/", "java/lang/foreign/", "java/lang/instrument/", "java/lang/management/",
                "java/lang/module/", "java/lang/constant/", "java/lang/runtime/", "java/lang/classfile/",
                "java/lang/reflect/");
        // Reflection is only possible on the plugin's own classes, see SandboxRuntime
        allow("java/lang/reflect/Array", "java/lang/reflect/Modifier", "java/lang/reflect/Field",
                "java/lang/reflect/Method", "java/lang/reflect/Constructor", "java/lang/reflect/Executable",
                "java/lang/reflect/Parameter", "java/lang/reflect/RecordComponent",
                "java/lang/reflect/AccessibleObject", "java/lang/reflect/Member",
                "java/lang/reflect/AnnotatedElement", "java/lang/reflect/Type",
                "java/lang/reflect/ParameterizedType", "java/lang/reflect/GenericArrayType",
                "java/lang/reflect/TypeVariable", "java/lang/reflect/WildcardType",
                "java/lang/reflect/GenericDeclaration", "java/lang/reflect/AnnotatedType",
                "java/lang/reflect/InvocationTargetException", "java/lang/reflect/UndeclaredThrowableException",
                "java/lang/reflect/MalformedParameterizedTypeException",
                "java/lang/reflect/MalformedParametersException", "java/lang/reflect/GenericSignatureFormatError");

        allowOnly("java/lang/Class",
                "getName", "getSimpleName", "getTypeName", "getCanonicalName", "getPackageName",
                "isInstance", "isAssignableFrom", "isInterface", "isArray", "isPrimitive", "isAnnotation",
                "isSynthetic", "isEnum", "isRecord", "isSealed", "isHidden", "isAnonymousClass", "isLocalClass",
                "isMemberClass", "getSuperclass", "getGenericSuperclass", "getInterfaces", "getGenericInterfaces",
                "getComponentType", "componentType", "arrayType", "getModifiers", "getEnumConstants", "cast",
                "asSubclass", "desiredAssertionStatus", "getAnnotation", "getAnnotations",
                "getDeclaredAnnotations", "isAnnotationPresent", "getAnnotationsByType", "getDeclaredAnnotation",
                "getDeclaredAnnotationsByType", "getTypeParameters", "getEnclosingClass", "getDeclaringClass",
                "getDeclaredClasses", "getClasses", "toString", "toGenericString", "hashCode", "equals",
                "getClass", "forName(Ljava/lang/String;)");
        allowOnly("java/lang/System",
                "currentTimeMillis", "nanoTime", "arraycopy", "identityHashCode", "lineSeparator", "out", "err");
        allowOnly("java/lang/Runtime",
                "getRuntime", "availableProcessors", "freeMemory", "totalMemory", "maxMemory", "version", "gc");
        denyMembers("java/lang/Thread",
                "getAllStackTraces", "setDefaultUncaughtExceptionHandler", "getDefaultUncaughtExceptionHandler",
                "setContextClassLoader", "getContextClassLoader", "stop", "suspend", "resume",
                "getThreadGroup", "enumerate", "activeCount", "checkAccess");
        // These read system properties
        denyMembers("java/lang/Boolean", "getBoolean");
        denyMembers("java/lang/Integer", "getInteger");
        denyMembers("java/lang/Long", "getLong");

        // --- java.util ---
        allow("java/util/");
        deny("java/util/ServiceLoader", "java/util/ResourceBundle", "java/util/prefs/", "java/util/logging/",
                "java/util/spi/", "java/util/jar/JarFile", "java/util/zip/ZipFile",
                "java/util/concurrent/atomic/AtomicIntegerFieldUpdater",
                "java/util/concurrent/atomic/AtomicLongFieldUpdater",
                "java/util/concurrent/atomic/AtomicReferenceFieldUpdater");
        // XML parsing resolves external entities (file:// reads)
        denyMembers("java/util/Properties", "loadFromXML", "storeToXML");
        // Formatter(String fileName, ...) opens a file
        denyMembers("java/util/Formatter", "<init>(Ljava/lang/String;");
        denyMembers("java/util/Locale", "setDefault");
        denyMembers("java/util/TimeZone", "setDefault");

        // --- java.io: no direct file access, files go through the plugin's sandboxed Path ---
        allow("java/io/");
        deny("java/io/File", "java/io/FileInputStream", "java/io/FileOutputStream", "java/io/FileReader",
                "java/io/FileWriter", "java/io/RandomAccessFile", "java/io/FileDescriptor",
                "java/io/FilePermission", "java/io/Console", "java/io/ObjectInputStream",
                "java/io/ObjectOutputStream");
        denyMembers("java/io/PrintStream", "<init>(Ljava/lang/String;");
        denyMembers("java/io/PrintWriter", "<init>(Ljava/lang/String;");

        // --- java.nio ---
        allow("java/nio/");
        deny("java/nio/file/Paths", "java/nio/file/FileSystems", "java/nio/file/spi/",
                "java/nio/channels/spi/", "java/nio/charset/spi/");
        denyMembers("java/nio/file/Path", "of");
        // Only the variants without a directory use the host's temp directory
        denyMembers("java/nio/file/Files",
                "createTempFile(Ljava/lang/String;", "createTempDirectory(Ljava/lang/String;");
        network("java/nio/channels/SocketChannel", "java/nio/channels/ServerSocketChannel",
                "java/nio/channels/DatagramChannel", "java/nio/channels/AsynchronousSocketChannel",
                "java/nio/channels/AsynchronousServerSocketChannel", "java/nio/channels/AsynchronousChannelGroup",
                "java/nio/channels/NetworkChannel", "java/nio/channels/MulticastChannel",
                "java/nio/channels/MembershipKey");

        // --- misc pure libraries ---
        allow("java/math/", "java/text/", "java/time/", "javax/crypto/");
        deny("java/text/spi/", "java/time/zone/ZoneRulesProvider");
        allow("java/security/");
        deny("java/security/Security", "java/security/Policy", "java/security/PolicySpi",
                "java/security/AccessController", "java/security/AccessControlContext",
                "java/security/ProtectionDomain", "java/security/CodeSource", "java/security/SecureClassLoader");

        // --- imaging (embeds, generated pictures) ---
        allow("java/awt/Color", "java/awt/Font", "java/awt/FontMetrics", "java/awt/Graphics",
                "java/awt/Graphics2D", "java/awt/BasicStroke", "java/awt/Stroke", "java/awt/Shape",
                "java/awt/Rectangle", "java/awt/Point", "java/awt/Dimension", "java/awt/Polygon",
                "java/awt/Insets", "java/awt/AlphaComposite", "java/awt/Composite", "java/awt/GradientPaint",
                "java/awt/LinearGradientPaint", "java/awt/RadialGradientPaint", "java/awt/MultipleGradientPaint",
                "java/awt/TexturePaint", "java/awt/Paint", "java/awt/Transparency", "java/awt/Image",
                "java/awt/RenderingHints", "java/awt/GraphicsEnvironment", "java/awt/FontFormatException",
                "java/awt/HeadlessException");
        allow("java/awt/geom/", "java/awt/image/", "java/awt/font/", "java/awt/color/", "javax/imageio/");
        deny("javax/imageio/spi/");
        denyMembers("java/awt/color/ICC_Profile", "getInstance(Ljava/lang/String;", "write(Ljava/lang/String;");

        // --- networking, needs the NETWORK permission ---
        network("java/net/", "java/net/http/", "javax/net/", "java/sql/", "javax/sql/");
        allow("java/net/URI", "java/net/URISyntaxException", "java/net/URLEncoder", "java/net/URLDecoder",
                "java/net/IDN", "java/net/MalformedURLException");
        deny("java/net/URLClassLoader", "java/net/URLStreamHandler", "java/net/URLStreamHandlerFactory",
                "java/net/ContentHandler", "java/net/ContentHandlerFactory", "java/net/FileNameMap",
                "java/net/SocketImpl", "java/net/SocketImplFactory", "java/net/DatagramSocketImpl",
                "java/net/DatagramSocketImplFactory", "java/net/JarURLConnection", "java/net/NetPermission",
                "java/net/spi/");
        // URLs are created through SandboxRuntime, which only allows http(s)
        denyMembers("java/net/URL", "<init>", "of", "setURLStreamHandlerFactory");
        denyMembers("java/net/URI", "toURL");
        // JVM wide settings, would affect the bot's own (token carrying) connections
        denyMembers("java/net/ProxySelector", "setDefault");
        denyMembers("java/net/Authenticator", "setDefault");
        denyMembers("java/net/CookieHandler", "setDefault");
        denyMembers("java/net/ResponseCache", "setDefault");
        denyMembers("java/net/HttpURLConnection", "setFollowRedirects");
        denyMembers("java/net/URLConnection", "setDefaultUseCaches", "setDefaultAllowUserInteraction",
                "setDefaultRequestProperty", "setFileNameMap", "setContentHandlerFactory");
        denyMembers("java/net/Socket", "setSocketImplFactory");
        denyMembers("java/net/ServerSocket", "setSocketFactory");
        denyMembers("java/net/DatagramSocket", "setDatagramSocketImplFactory");
        denyMembers("javax/net/ssl/SSLContext", "setDefault");
        denyMembers("javax/net/ssl/HttpsURLConnection", "setDefaultSSLSocketFactory", "setDefaultHostnameVerifier");

        // --- logging & annotations ---
        allow("org/jetbrains/annotations/", "org/slf4j/Logger", "org/slf4j/Marker", "org/slf4j/MarkerFactory",
                "org/slf4j/event/Level", "org/slf4j/spi/LoggingEventBuilder", "org/slf4j/LoggerFactory");
        allowOnly("org/slf4j/LoggerFactory", "getLogger");

        // --- Comitas API ---
        allow("cloud/thehsi/ComitasBotJ/API/");
        deny("cloud/thehsi/ComitasBotJ/API/Bot/InternalComitasImpl", "cloud/thehsi/ComitasBotJ/API/Bot/UtilityBackend");
        denyMembers("cloud/thehsi/ComitasBotJ/API/Bot/Comitas", "getUtilityBackend", "init");
        // Would allow running other plugins' (and the base plugin's) console commands
        denyMembers("cloud/thehsi/ComitasBotJ/API/Console/ConsoleCommandRegistry$ConsoleCommand",
                "plugin", "consoleCommandExecutor");
    }

    private SandboxPolicy() {
    }

    private static void allow(String... keys) {
        for (String key : keys) CLASS_RULES.put(key, Access.ALLOW);
    }

    private static void deny(String... keys) {
        for (String key : keys) CLASS_RULES.put(key, Access.DENY);
    }

    private static void network(String... keys) {
        for (String key : keys) CLASS_RULES.put(key, Access.NETWORK);
    }

    private static void denyMembers(String owner, String... entries) {
        MEMBER_RULES.put(owner, new MemberRule(false, Set.of(entries)));
    }

    private static void allowOnly(String owner, String... entries) {
        MEMBER_RULES.put(owner, new MemberRule(true, Set.of(entries)));
    }

    static boolean isReserved(@NotNull String internalName) {
        for (String prefix : RESERVED_PREFIXES)
            if (internalName.startsWith(prefix)) return true;
        return false;
    }

    /**
     * Classes, the sandbox parent class loader exposes from the core class loader (binary names).
     * Everything else outside the JDK is invisible to plugins.
     */
    static boolean isExposedHostClass(@NotNull String binaryName) {
        return binaryName.startsWith("cloud.thehsi.ComitasBotJ.API.")
                || binaryName.startsWith("org.slf4j.")
                || binaryName.startsWith("org.jetbrains.annotations.")
                || binaryName.equals(SandboxRuntime.class.getName());
    }

    @NotNull
    static Access classAccess(@NotNull String internalName) {
        String probe = internalName;
        while (true) {
            Access access = CLASS_RULES.get(probe);
            if (access != null) return access;
            int nested = probe.lastIndexOf('$');
            if (nested < 0) break;
            probe = probe.substring(0, nested);
        }

        int slash = internalName.lastIndexOf('/');
        while (slash > 0) {
            Access access = CLASS_RULES.get(internalName.substring(0, slash + 1));
            if (access != null) return access;
            slash = internalName.lastIndexOf('/', slash - 1);
        }

        return Access.DENY;
    }

    /**
     * @return Null if the member of this (host) class may be used, otherwise the reason why not
     */
    @Nullable
    static String memberDenied(@NotNull String owner, @NotNull String name, @NotNull String descriptor) {
        MemberRule rule = MEMBER_RULES.get(owner);
        if (rule == null) return null;

        boolean matched = false;
        for (String entry : rule.entries()) {
            if (entry.indexOf('(') >= 0 ? (name + descriptor).startsWith(entry) : entry.equals(name)) {
                matched = true;
                break;
            }
        }

        if (matched != rule.allowOnly())
            return owner.replace('/', '.') + "." + name + " is not available to plugins";
        return null;
    }

    static boolean isAllowedBootstrap(@NotNull String owner, @NotNull String name) {
        return ALLOWED_BOOTSTRAPS.contains(owner + "." + name);
    }

    private record MemberRule(boolean allowOnly, Set<String> entries) {
    }
}

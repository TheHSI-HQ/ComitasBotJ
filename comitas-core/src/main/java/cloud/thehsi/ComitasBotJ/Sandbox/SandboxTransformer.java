package cloud.thehsi.ComitasBotJ.Sandbox;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public final class SandboxTransformer {

    private static final Type GUARD =
            Type.getObjectType(
                    "cloud/thehsi/ComitasBotJ/Sandbox/SandboxGuard"
            );

    private static final Type PATH_TYPE =
            Type.getObjectType("java/nio/file/Path");

    private static final Type FILE_TYPE =
            Type.getObjectType("java/io/File");

    private static final Method REWRITE_PATH =
            new Method(
                    "rewritePath",
                    "(Ljava/nio/file/Path;)Ljava/nio/file/Path;"
            );

    private static final Method REWRITE_FILE =
            new Method(
                    "rewriteFile",
                    "(Ljava/io/File;)Ljava/io/File;"
            );

    private static final Method REWRITE_STRING =
            new Method(
                    "rewriteString",
                    "(Ljava/lang/String;)Ljava/lang/String;"
            );

    private SandboxTransformer() {
    }

    public static byte[] transform(byte[] original) {
        ClassReader reader = new ClassReader(original);

        ClassWriter writer = new ClassWriter(
                reader,
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
        );

        ClassVisitor visitor = new ClassVisitor(
                Opcodes.ASM9,
                writer
        ) {
            @Override
            public MethodVisitor visitMethod(
                    int access,
                    String name,
                    String descriptor,
                    String signature,
                    String[] exceptions
            ) {
                MethodVisitor mv = super.visitMethod(
                        access,
                        name,
                        descriptor,
                        signature,
                        exceptions
                );

                return new SandboxMethodVisitor(
                        mv,
                        access,
                        name,
                        descriptor
                );
            }
        };

        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        return writer.toByteArray();
    }

    private static final class SandboxMethodVisitor
            extends GeneratorAdapter {

        SandboxMethodVisitor(
                MethodVisitor mv,
                int access,
                String name,
                String descriptor
        ) {
            super(
                    Opcodes.ASM9,
                    mv,
                    access,
                    name,
                    descriptor
            );
        }

        @Override
        public void visitMethodInsn(
                int opcode,
                String owner,
                String name,
                String descriptor,
                boolean isInterface
        ) {

            /*
             * -------------------------------------------------------------
             * Path-producing APIs
             * -------------------------------------------------------------
             */

            if (isPathFactory(opcode, owner, name, descriptor)) {
                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );

                /*
                 * The method returned a Path.
                 *
                 * Rewrite the returned Path before plugin code gets it.
                 */
                invokeStatic(GUARD, REWRITE_PATH);
                return;
            }

            /*
             * Path -> File
             *
             * Prevent:
             *
             *     path.toFile()
             *
             * from creating an unrestricted File object.
             */
            if (opcode == Opcodes.INVOKEINTERFACE
                    && owner.equals("java/nio/file/Path")
                    && name.equals("toFile")
                    && descriptor.equals("()Ljava/io/File;")) {

                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );

                invokeStatic(GUARD, REWRITE_FILE);
                return;
            }

            /*
             * File -> Path
             *
             * Prevent an unrestricted File from being converted back into
             * a Path without passing through the guard.
             */
            if ((opcode == Opcodes.INVOKEVIRTUAL
                    || opcode == Opcodes.INVOKEINTERFACE)
                    && owner.equals("java/io/File")
                    && name.equals("toPath")
                    && descriptor.equals("()Ljava/nio/file/Path;")) {

                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );

                invokeStatic(GUARD, REWRITE_PATH);
                return;
            }

            /*
             * -------------------------------------------------------------
             * File constructors
             * -------------------------------------------------------------
             */

            if (opcode == Opcodes.INVOKESPECIAL
                    && owner.equals("java/io/File")
                    && name.equals("<init>")) {

                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );

                invokeStatic(GUARD, REWRITE_FILE);
                return;
            }

            /*
             * Constructors taking a String path.
             */
            if (opcode == Opcodes.INVOKESPECIAL
                    && name.equals("<init>")
                    && isStringPathCtor(owner, descriptor)) {

                rewriteLeadingStringArg(descriptor);

                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );

                return;
            }

            /*
             * -------------------------------------------------------------
             * Files / FileChannel / AsynchronousFileChannel
             * -------------------------------------------------------------
             *
             * Rewrite EVERY Path argument, not just the first argument.
             *
             * This catches things such as:
             *
             * Files.copy(InputStream, Path, ...)
             * Files.copy(Path, Path, ...)
             * Files.move(Path, Path, ...)
             * Files.walk(Path, ...)
             * FileChannel.open(Path, ...)
             */
            if (opcode == Opcodes.INVOKESTATIC
                    && isFilesystemStaticOwner(owner)
                    && containsPathArgument(descriptor)) {

                rewritePathArguments(descriptor);

                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );

                return;
            }

            /*
             * -------------------------------------------------------------
             * FileSystemProvider
             * -------------------------------------------------------------
             *
             * A plugin can bypass Files.* and use the provider directly:
             *
             *     path.getFileSystem().provider().newByteChannel(...)
             *
             * Catch Path arguments here as well.
             */
            if ((opcode == Opcodes.INVOKEVIRTUAL
                    || opcode == Opcodes.INVOKEINTERFACE)
                    && owner.equals("java/nio/file/spi/FileSystemProvider")
                    && containsPathArgument(descriptor)) {

                rewritePathArguments(descriptor);

                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );

                return;
            }

            super.visitMethodInsn(
                    opcode,
                    owner,
                    name,
                    descriptor,
                    isInterface
            );
        }

        private boolean isPathFactory(
                int opcode,
                String owner,
                String name,
                String descriptor
        ) {
            /*
             * Path.of(...)
             *
             * Static.
             */
            if (opcode == Opcodes.INVOKESTATIC
                    && owner.equals("java/nio/file/Path")
                    && name.equals("of")
                    && Type.getReturnType(descriptor).equals(PATH_TYPE)) {
                return true;
            }

            /*
             * Paths.get(...)
             */
            if (opcode == Opcodes.INVOKESTATIC
                    && owner.equals("java/nio/file/Paths")
                    && name.equals("get")
                    && Type.getReturnType(descriptor).equals(PATH_TYPE)) {
                return true;
            }

            /*
             * FileSystem.getPath(...)
             *
             * IMPORTANT: this is an instance method, not static.
             */
            return (opcode == Opcodes.INVOKEVIRTUAL
                    || opcode == Opcodes.INVOKEINTERFACE)
                    && owner.equals("java/nio/file/FileSystem")
                    && name.equals("getPath")
                    && Type.getReturnType(descriptor).equals(PATH_TYPE);
        }

        private boolean isFilesystemStaticOwner(String owner) {
            return owner.equals("java/nio/file/Files")
                    || owner.equals("java/nio/channels/FileChannel")
                    || owner.equals(
                    "java/nio/channels/AsynchronousFileChannel"
            );
        }

        private boolean containsPathArgument(String descriptor) {
            for (Type type : Type.getArgumentTypes(descriptor)) {
                if (type.equals(PATH_TYPE)) {
                    return true;
                }
            }

            return false;
        }

        private boolean isStringPathCtor(
                String owner,
                String descriptor
        ) {
            return switch (owner) {
                case "java/io/FileInputStream",
                     "java/io/FileReader" -> descriptor.equals(
                        "(Ljava/lang/String;)V"
                );

                case "java/io/FileOutputStream",
                     "java/io/FileWriter" -> descriptor.equals(
                        "(Ljava/lang/String;)V"
                ) || descriptor.equals(
                        "(Ljava/lang/String;Z)V"
                );

                case "java/io/RandomAccessFile" -> descriptor.equals(
                        "(Ljava/lang/String;Ljava/lang/String;)V"
                );

                default -> false;
            };
        }

        /**
         * Stack before:
         * <p>
         * ..., String[, extra]
         * <p>
         * Rewrites the first String while preserving additional arguments.
         */
        private void rewriteLeadingStringArg(String descriptor) {
            Type[] argTypes = Type.getArgumentTypes(descriptor);

            if (argTypes.length == 1) {
                invokeStatic(GUARD, REWRITE_STRING);
                return;
            }

            int extraLocal = newLocal(argTypes[1]);

            storeLocal(extraLocal);

            invokeStatic(GUARD, REWRITE_STRING);

            loadLocal(extraLocal);
        }

        /**
         * Rewrite every Path argument in an invocation.
         * <p>
         * Stack before:
         * <p>
         * ..., arg0, arg1, arg2, ...
         * <p>
         * Stack after:
         * <p>
         * ..., rewritten(arg0), rewritten(arg1), ...
         * <p>
         * Non-Path arguments are left untouched.
         */
        private void rewritePathArguments(String descriptor) {
            Type[] argTypes = Type.getArgumentTypes(descriptor);

            int[] locals = new int[argTypes.length];

            /*
             * Store all arguments in reverse order.
             *
             * This preserves the original argument order when reloading.
             */
            for (int i = argTypes.length - 1; i >= 0; i--) {
                locals[i] = newLocal(argTypes[i]);
                storeLocal(locals[i]);
            }

            /*
             * Reload arguments in their original order.
             */
            for (int i = 0; i < argTypes.length; i++) {
                loadLocal(locals[i]);

                if (argTypes[i].equals(PATH_TYPE)) {
                    invokeStatic(GUARD, REWRITE_PATH);
                }
            }
        }
    }
}
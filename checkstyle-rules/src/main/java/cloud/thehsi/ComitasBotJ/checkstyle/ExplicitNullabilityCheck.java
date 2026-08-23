package cloud.thehsi.ComitasBotJ.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class ExplicitNullabilityCheck extends AbstractCheck {

    private @NotNull
    static final Set<String> NULLABILITY_ANNOTATIONS = Set.of(
            "NotNull",
            "Nullable"
    );

    @Override
    public int[] getDefaultTokens() {
        return new int[]{
                TokenTypes.METHOD_DEF,
                TokenTypes.CTOR_DEF,
                TokenTypes.VARIABLE_DEF
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }

    @Override
    public void visitToken(@NotNull DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF -> {
                checkReturnType(ast);
                checkParameters(ast);
            }
            case TokenTypes.VARIABLE_DEF -> checkField(ast);
            default -> {
                // Nothing to check
            }
        }
    }

    private void checkField(@NotNull DetailAST field) {
        // Only check class fields, not local variables
        if (field.getParent() == null
                || field.getParent().getType() != TokenTypes.OBJBLOCK) {
            return;
        }

        DetailAST type = field.findFirstToken(TokenTypes.TYPE);

        if (type == null || isPrimitive(type))
            return;

        DetailAST modifiers = field.findFirstToken(TokenTypes.MODIFIERS);

        if (missingNullabilityAnnotation(modifiers)) {
            String name = Objects.requireNonNull(
                    field.findFirstToken(TokenTypes.IDENT)
            ).getText();

            log(
                    field,
                    "nullability.field.missing",
                    name
            );
        }
    }

    private void checkReturnType(@NotNull DetailAST method) {
        if (method.getType() != TokenTypes.METHOD_DEF)
            return;

        DetailAST type = method.findFirstToken(TokenTypes.TYPE);

        if (type == null || isVoid(type) || isPrimitive(type))
            return;

        DetailAST modifiers = method.findFirstToken(TokenTypes.MODIFIERS);

        if (missingNullabilityAnnotation(modifiers)) {
            log(
                    method,
                    "nullability.return.missing",
                    Objects.requireNonNull(method.findFirstToken(TokenTypes.IDENT)).getText()
            );
        }
    }

    private void checkParameters(@NotNull DetailAST method) {
        DetailAST parameters = method.findFirstToken(TokenTypes.PARAMETERS);

        if (parameters == null)
            return;

        for (DetailAST parameter = parameters.getFirstChild();
             parameter != null;
             parameter = parameter.getNextSibling()) {

            if (parameter.getType() != TokenTypes.PARAMETER_DEF)
                continue;

            DetailAST type = parameter.findFirstToken(TokenTypes.TYPE);

            if (type == null || isPrimitive(type))
                continue;

            DetailAST modifiers = parameter.findFirstToken(TokenTypes.MODIFIERS);

            if (missingNullabilityAnnotation(modifiers)) {
                String name = Objects.requireNonNull(parameter.findFirstToken(TokenTypes.IDENT)).getText();

                log(
                        parameter,
                        "nullability.parameter.missing",
                        name
                );
            }
        }
    }

    private boolean isVoid(@NotNull DetailAST type) {
        DetailAST child = type.getFirstChild();

        return child != null && child.getType() == TokenTypes.LITERAL_VOID;
    }

    private boolean isPrimitive(@NotNull DetailAST type) {
        DetailAST child = type.getFirstChild();

        if (child == null)
            return false;

        return switch (child.getType()) {
            case TokenTypes.LITERAL_BOOLEAN,
                 TokenTypes.LITERAL_BYTE,
                 TokenTypes.LITERAL_CHAR,
                 TokenTypes.LITERAL_SHORT,
                 TokenTypes.LITERAL_INT,
                 TokenTypes.LITERAL_LONG,
                 TokenTypes.LITERAL_FLOAT,
                 TokenTypes.LITERAL_DOUBLE -> true;
            default -> false;
        };
    }

    private boolean missingNullabilityAnnotation(@Nullable DetailAST modifiers) {
        if (modifiers == null)
            return true;

        for (DetailAST child = modifiers.getFirstChild();
             child != null;
             child = child.getNextSibling()) {

            if (child.getType() != TokenTypes.ANNOTATION)
                continue;

            DetailAST annotationName =
                    child.findFirstToken(TokenTypes.IDENT);

            if (annotationName != null
                    && NULLABILITY_ANNOTATIONS.contains(annotationName.getText())) {
                return false;
            }
        }

        return true;
    }
}
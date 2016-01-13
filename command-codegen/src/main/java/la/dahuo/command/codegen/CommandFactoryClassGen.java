package la.dahuo.command.codegen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import la.dahuo.command.Command;
import la.dahuo.command.CommandDef;
import la.dahuo.command.CommandFactory;
import la.dahuo.command.Param;
import la.dahuo.command.TypeConverter;

/**
 * Created by dhu on 15/12/16.
 */
public class CommandFactoryClassGen {
    protected final Element mElement;

    private ClassName mClassName;
    private FactoryInfo mFactoryInfo;

    private static final String JAVA_LANG_STRING = "java.lang.String";
    private static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";
    private static final String JAVA_LANG_INTEGER = "java.lang.Integer";
    private static final String JAVA_LANG_LONG = "java.lang.Long";
    private static final String JAVA_LANG_FLOAT = "java.lang.Float";
    private static final String JAVA_LANG_DOUBLE = "java.lang.Double";

    enum ParamType {
        Unsupported,
        Boolean,
        Int,
        Long,
        Float,
        Double,
        String;

        public static ParamType parseType(TypeMirror type) {
            if (JAVA_LANG_STRING.equals(type.toString())) {
                return String;
            } else if (type.getKind() == TypeKind.BOOLEAN || JAVA_LANG_BOOLEAN.equals(type.toString())) {
                return Boolean;
            } else if (type.getKind() == TypeKind.INT || JAVA_LANG_INTEGER.equals(type.toString())) {
                return Int;
            } else if (type.getKind() == TypeKind.LONG || JAVA_LANG_LONG.equals(type.toString())) {
                return Long;
            } else if (type.getKind() == TypeKind.FLOAT || JAVA_LANG_FLOAT.equals(type.toString())) {
                return Float;
            } else if (type.getKind() == TypeKind.DOUBLE || JAVA_LANG_DOUBLE.equals(type.toString())) {
                return Double;
            }
            return Unsupported;
        }
    }
    static class ParamInfo {
        public String paramName;
        public String fieldName;
        public ParamType type;

    }
    static class FactoryInfo {
        public String action;
        public ClassName factoryName;
    }
    private List<ParamInfo> mParamInfos;
    public CommandFactoryClassGen(TypeElement element) {
        mElement = element;
        mClassName = ClassName.get(element);
        mFactoryInfo = new FactoryInfo();
        mFactoryInfo.factoryName = ClassName.get(mClassName.packageName(), mClassName.simpleName() + "Factory");
        String action = element.getAnnotation(CommandDef.class).value();
        if (action == null || action.length() == 0) {
            action = element.getSimpleName().toString();
            //make first letter lowercase
            action = action.substring(0, 1).toLowerCase() + action.substring(1);
        }
        mFactoryInfo.action = action;

        mParamInfos = new ArrayList<>();
        List<VariableElement> fields = ElementFilter.fieldsIn(mElement.getEnclosedElements());
        for (VariableElement field : fields) {
            Param param = field.getAnnotation(Param.class);
            if (param != null) {
                ParamInfo paramInfo = new ParamInfo();
                paramInfo.fieldName = field.getSimpleName().toString();
                String paramName = param.value();
                if (paramName == null || paramName.length() == 0) {
                    paramName = paramInfo.fieldName;
                }
                paramInfo.paramName = paramName;
                paramInfo.type = ParamType.parseType(field.asType());
                if (paramInfo.type == ParamType.Unsupported) {
                    throw new RuntimeException("Type: " + field.asType().toString() + " Unsupported");
                }
                mParamInfos.add(paramInfo);
            }
        }
    }

    public FactoryInfo getFactoryInfo() {
        return mFactoryInfo;
    }

    public void writeTo(Filer output) throws IOException {
        MethodSpec.Builder newCommandBuilder = MethodSpec.methodBuilder("newCommand")
                .addModifiers(Modifier.PUBLIC)
                .returns(Command.class)
                .addParameter(ParameterizedTypeName.get(Map.class, String.class, Object.class), "params")
                .addStatement("$T command = new $T()", mClassName, mClassName);
        for (ParamInfo paramInfo : mParamInfos) {
            newCommandBuilder.addStatement("command.$L = $T.to$L(params.get($S))",
                    paramInfo.fieldName, TypeConverter.class, paramInfo.type.name(), paramInfo.paramName);
        }
        newCommandBuilder.addStatement("return command");

        TypeSpec factoryClass = TypeSpec.classBuilder(mFactoryInfo.factoryName.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(CommandFactory.class)
                .addMethod(newCommandBuilder.build())
                .build();
        JavaFile javaFile = JavaFile.builder(mFactoryInfo.factoryName.packageName(), factoryClass)
                .build();
        javaFile.writeTo(output);
    }

}

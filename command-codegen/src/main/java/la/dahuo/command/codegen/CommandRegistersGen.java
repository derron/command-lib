package la.dahuo.command.codegen;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import la.dahuo.command.CommandFactory;

/**
 * Created by dhu on 15/12/17.
 */
public class CommandRegistersGen {

    private List<CommandFactoryClassGen.FactoryInfo> mFactoryInfos;
    public CommandRegistersGen(List<CommandFactoryClassGen.FactoryInfo> factories) {
        mFactoryInfos = factories;
    }

    public void writeTo(Filer output) throws IOException {
        MethodSpec.Builder registerBuilder = MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ParameterizedTypeName.get(Map.class, String.class, CommandFactory.class), "map");
        for (CommandFactoryClassGen.FactoryInfo info : mFactoryInfos) {
            registerBuilder.addStatement("map.put($S, new $T())", info.action, info.factoryName);
        }

        TypeSpec registersClass = TypeSpec.classBuilder("CommandRegisters")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(registerBuilder.build())
                .build();
        JavaFile javaFile = JavaFile.builder("la.dahuo.command", registersClass)
                .build();
        javaFile.writeTo(output);
    }

}

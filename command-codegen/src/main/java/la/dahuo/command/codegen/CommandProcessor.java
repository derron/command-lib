package la.dahuo.command.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import la.dahuo.command.CommandDef;

public class CommandProcessor extends AbstractProcessor {

    private static boolean sCommandRegistersGenerated;
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(CommandDef.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<CommandFactoryClassGen.FactoryInfo> allCommandFactories = new ArrayList<>();
        for (Element element : AnnotationUtil.getElementsAnnotatedWith(roundEnv, CommandDef.class)) {
            CommandFactoryClassGen gen = new CommandFactoryClassGen((TypeElement)element);
            try {
                gen.writeTo(processingEnv.getFiler());
                allCommandFactories.add(gen.getFactoryInfo());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            if (!sCommandRegistersGenerated) {
                new CommandRegistersGen(allCommandFactories).writeTo(processingEnv.getFiler());
                sCommandRegistersGenerated = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}

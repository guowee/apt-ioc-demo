package com.muse.compiler;

import com.google.auto.service.AutoService;
import com.muse.annotation.BindView;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by GuoWee on 2018/3/14.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.muse.annotation.BindView"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ViewInjectProcessor extends AbstractProcessor {
    // Storing all the annotation information under the same Class
    Map<String, List<VariableInfo>> classMap = new HashMap<>();
    // The corresponding information stored Class : TypeElement
    Map<String, TypeElement> classTypeElement = new HashMap<>();


    private Filer filer;
    Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // collect information
        gatherInformation(roundEnv);

        // generate java code
        generateClassFile();
        return false;
    }

    private void gatherInformation(RoundEnvironment roundEnv) {

        classMap.clear();
        classTypeElement.clear();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            //Get the value of the BindView annotation
            int viewId = element.getAnnotation(BindView.class).value();
            //An element that is annotated
            VariableElement variableElement = (VariableElement) element;
            //Class, where the annotated element is located
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            // the full name of class
            String classFullName = typeElement.getQualifiedName().toString();

            // collect the annotated element in the class
            List<VariableInfo> variableList = classMap.get(classFullName);
            if (variableList == null) {
                variableList = new ArrayList<>();
                classMap.put(classFullName, variableList);
                // save the the corresponding information
                classTypeElement.put(classFullName, typeElement);
            }

            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setVariableElement(variableElement);
            variableInfo.setViewId(viewId);
            variableList.add(variableInfo);

        }
    }

    private void generateClassFile() {
        try {
            for (String classFullName : classMap.keySet()) {
                TypeElement typeElement = classTypeElement.get(classFullName);
                // Using the constructor to bind data
                MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(typeElement.asType()), "activity").build());

                List<VariableInfo> variableList = classMap.get(classFullName);
                for (VariableInfo variableInfo : variableList) {
                    VariableElement variableElement = variableInfo.getVariableElement();
                    // The name of the variable (for example: the TV of TextView TV)
                    String variableName = variableElement.getSimpleName().toString();
                    // A complete class path of a variable type (for example: android.widget.TextView)
                    String variableFullName = variableElement.asType().toString();
                    // An assignment statement is added to the construction method, such as: activity.tv = (android.widget.TextView) activity.findViewById (215334);
                    // Because of the use of the form of activity., the variable cannot use the private modifier
                    constructor.addStatement("activity.$L=($L)activity.findViewById($L)", variableName, variableFullName, variableInfo.getViewId());
                }
                // build class
                TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + "$$ViewInjector").addModifiers(Modifier.PUBLIC)
                        .addMethod(constructor.build())
                        .build();
                // Put the target Class under the same package to solve the accessibility of the Class property
                String packageFullName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec)
                        .build();
                // generating the class file
                javaFile.writeTo(filer);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

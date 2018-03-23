package com.muse.compiler;

import com.google.auto.service.AutoService;
import com.muse.annotation.BindView;
import com.muse.compiler.model.BindViewField;
import com.muse.compiler.model.BindingClass;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by GuoWee on 2018/3/14.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.muse.annotation.BindView"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ViewInjectProcessor extends AbstractProcessor {
    // Storing all the annotation information under the same Class
    Map<String, BindingClass> classMap = new HashMap<>();
    // The corresponding information stored Class : TypeElement
    Map<String, TypeElement> classTypeElement = new HashMap<>();


    private Filer mFiler;
    Elements elementUtils;
    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
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
        try {
            for (BindingClass bindingClass : classMap.values()) {
                info("Generating file for %s", bindingClass.getFullClassName());
                bindingClass.brewJava().writeTo(mFiler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            error("Generate file failed, reason: %s", e.getMessage());
        }
        return true;
    }

    private void gatherInformation(RoundEnvironment roundEnv) {
        classMap.clear();
        gatherBindView(roundEnv);
    }


    private void gatherBindView(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            BindingClass bindingClass = getBindingClass(element);
            BindViewField field = new BindViewField(element);
            bindingClass.addField(field);
        }
    }

    private BindingClass getBindingClass(Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = enclosingElement.getQualifiedName().toString();
        BindingClass annotatedClass = classMap.get(fullClassName);
        if (annotatedClass == null) {
            annotatedClass = new BindingClass(enclosingElement, elementUtils);
            classMap.put(fullClassName, annotatedClass);
        }
        return annotatedClass;
    }

    private void error(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
    }

    private void info(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
    }

}

package com.muse.compiler.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by GuoWee on 2018/3/23.
 */

public class BindingClass {
    public TypeElement mClassElement; //类名
    public List<BindViewField> mFields;//成员变量
    public Elements mElementUtils;

    public BindingClass(TypeElement classElement, Elements elementUtils) {
        this.mClassElement = classElement;
        this.mElementUtils = elementUtils;
        mFields = new ArrayList<>();
    }

    public String getFullClassName() {
        return mClassElement.getQualifiedName().toString();
    }

    public void addField(BindViewField field) {
        mFields.add(field);
    }

    private String getPackageName(TypeElement type) {
        return mElementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    public JavaFile brewJava() {
        // method inject(final T host, Object source, Provider provider)
        ClassName FINDER = ClassName.get("com.muse.api.finder", "Finder");
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mClassElement.asType()), "target", Modifier.FINAL)
                .addParameter(TypeName.OBJECT, "source")
                .addParameter(FINDER, "finder");


        //field
        for (BindViewField field : mFields) {
            injectMethodBuilder.addStatement("target.$N= ($T)(finder.findView(source,$L))", field.getFieldName()
                    , ClassName.get(field.getFieldType()), field.getResId());

        }


        String packageName = getPackageName(mClassElement);
        String className = getClassName(mClassElement, packageName);
        ClassName bindingClassName = ClassName.get(packageName, className);

        ClassName INJECTOR = ClassName.get("com.muse.api", "Injector");

        // generate whole class
        TypeSpec finderClass = TypeSpec.classBuilder(bindingClassName.simpleName() + "$$ViewInjector")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(INJECTOR, TypeName.get(mClassElement.asType())))
                .addMethod(injectMethodBuilder.build())
                .build();

        return JavaFile.builder(packageName, finderClass).build();
    }


}

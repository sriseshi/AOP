package com.srise.aptprocess;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.srise.libannotation.MyAnnotation;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

final public class MyProcessor extends AbstractProcessor {
    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;
    private HashMap<String, Set<VariableElement>> mHashMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
        mHashMap = new HashMap<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(MyAnnotation.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        note("+++++++");

        if (set.size() > 0) {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(MyAnnotation.class)) {
                VariableElement variableElement = (VariableElement) element;

                //获取包含变量的主类
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();

                Set<VariableElement> elements = mHashMap.get(typeElement.getQualifiedName().toString());

                if (elements == null) {
                    elements = new LinkedHashSet<>();
                    mHashMap.put(typeElement.getQualifiedName().toString(), elements);
                }

                elements.add(variableElement);
            }

            for (String name : mHashMap.keySet()) {
                Set<VariableElement> variableElements = mHashMap.get(name);

                String pkgName = null;
                String fileName = null;
                ClassName viewParaClassName = ClassName.get("android.view", "View");

                MethodSpec constructorOne = null;
                MethodSpec constructorTwo = null;
                ParameterSpec baseActivityPara = null;
                ParameterSpec viewPara = null;
                MethodSpec.Builder constructorTwoBuilder = null;

                for (VariableElement element : variableElements) {
                    TypeElement activityType = (TypeElement) element.getEnclosingElement();

                    if (fileName == null) {
                        baseActivityPara = ParameterSpec.builder(TypeName.get(activityType.asType()), "activity")
                                .build();
                        viewPara = ParameterSpec.builder(viewParaClassName, "view").build();

                        constructorOne = MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(baseActivityPara)
                                .addStatement("this(activity, activity.getWindow().getDecorView())")
                                .build();

                        constructorTwoBuilder = MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(baseActivityPara)
                                .addParameter(viewPara);

                        note(activityType.getSimpleName().toString());
                        note(activityType.asType().toString());
                        fileName = activityType.getSimpleName().toString() + "_MyBinder";
                    }

                    if (pkgName == null) {
                        PackageElement packageElement = (PackageElement) activityType.getEnclosingElement();
                        note(packageElement.getQualifiedName().toString());
                        pkgName = packageElement.getQualifiedName().toString();
                    }

                    note(element.asType().toString());
                    note(element.getSimpleName().toString());
                    MyAnnotation value = element.getAnnotation(MyAnnotation.class);
                    note(value.value() + "");
                    constructorTwoBuilder.addStatement("activity.$N = ($N)view.findViewById(" + value.value() + ")",
                            element.getSimpleName().toString(),
                            element.asType().toString()
                    );
                }

                constructorTwo = constructorTwoBuilder.build();

                TypeSpec binderClass = TypeSpec.classBuilder(fileName)
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ClassName.get("com.srise.aptlib", "MyBinder"))
                        .addMethod(constructorOne)
                        .addMethod(constructorTwo)
                        .build();

                JavaFile javaFile = JavaFile.builder(pkgName, binderClass)
                        .addFileComment(" This codes are generated automatically. Do not modify!")
                        .build();
                // write to file
                try {
                    javaFile.writeTo(mFiler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return true;
    }

    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}

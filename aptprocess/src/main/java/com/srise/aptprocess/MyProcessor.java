package com.srise.aptprocess;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.srise.libannotation.MyAnnotation;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static java.util.Objects.requireNonNull;

final public class MyProcessor extends AbstractProcessor {
    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;
    private HashMap<String, Set<VariableElement>> mHashMap;
    private Trees trees;
    private final RScanner rScanner = new RScanner();
    private static final ClassName ANDROID_R = ClassName.get("android", "R");
    private static final String R = "R";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
        mHashMap = new HashMap<>();
        trees = Trees.instance(processingEnv);
        rScanner.setMessage(mMessager);
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

                    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                        note("************");
                        note(annotationMirror.getAnnotationType().toString() + "");
                        note("************");
//                        if (annotationMirror.getAnnotationType().toString().equals(MyAnnotation.class.getCanonicalName())) {
//                            return annotationMirror;
//                        }
                    }
                    MyAnnotation value = element.getAnnotation(MyAnnotation.class);
                    note(value.value() + "");
                    String resName = elementToId(element, MyAnnotation.class, element.getAnnotation(MyAnnotation.class).value());
                    constructorTwoBuilder.addStatement("activity.$N = ($N)view.findViewById($N)",
                            element.getSimpleName().toString(),
                            element.asType().toString(),
                            resName
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

    private static AnnotationMirror getMirror(Element element, Class<? extends Annotation> annotation) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotation.getCanonicalName())) {
                return annotationMirror;
            }
        }
        return null;
    }

    private String elementToId(Element element, Class<? extends Annotation> annotation, int value) {
        note("trees: " + trees.getClass().getCanonicalName());
        note("element: " + element.getSimpleName());
        AnnotationMirror annotationMirror = getMirror(element, annotation);
        note("annotationMirror: " + annotationMirror);

        JCTree tree = (JCTree) trees.getTree(element, getMirror(element, annotation));
        if (tree != null) { // tree can be null if the references are compiled types and not source
            note("elementToId1");
            rScanner.reset();
            note("elementToId2");
            tree.accept(rScanner);
            note("rScanner start");
            if (!rScanner.resourceIds.isEmpty()) {
                note("values: " + rScanner.resourceIds.values().size());
                return rScanner.resourceIds.values().iterator().next();
            }
        }

        return null;
    }

    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }


    private static class RScanner extends TreeScanner {
        Map<Integer, String> resourceIds = new LinkedHashMap<>();
        private Messager mMessager;

        @Override
        public void visitIdent(JCTree.JCIdent jcIdent) {
            super.visitIdent(jcIdent);

            Symbol symbol = jcIdent.sym;

            mMessager.printMessage(Diagnostic.Kind.NOTE, "visitIdent, jcIdent.name: " + jcIdent.name.toString() + ", symbol.type: " + symbol.type.toString());

            if (symbol.type instanceof Type.JCPrimitiveType) {
                mMessager.printMessage(Diagnostic.Kind.NOTE, "innervisitIdent");

                Id id = parseId(symbol);

                if (id != null) {
                    resourceIds.put(id.value, id.resId);
                }
            }
        }

        @Override
        public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
            Symbol symbol = jcFieldAccess.sym;
            mMessager.printMessage(Diagnostic.Kind.NOTE, "visitSelect, " +
                    "jcFieldAccess.name: " + jcFieldAccess.name.toString()
                    + ", symbol: " + symbol.getClass().getCanonicalName()
                    + ", symbol.name:" + symbol.name.toString()
                    + ", symbol.type: " + symbol.type.toString());

            Id id = parseId(symbol);

            if (id != null) {
                resourceIds.put(id.value, id.resId);
            }
        }

        private Id parseId(Symbol symbol) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "parseId");

            if (symbol.getEnclosingElement() != null
                    && symbol.getEnclosingElement().getEnclosingElement() != null
                    && symbol.getEnclosingElement().getEnclosingElement().enclClass() != null) {
                try {
                    int value = (Integer) requireNonNull(((Symbol.VarSymbol) symbol).getConstantValue());
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "parseId, symbol.name: " + symbol.name.toString());
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "parseId, symbol.enclClass.name: " + symbol.enclClass().name.toString());

                    ClassName className = ClassName.get(symbol.packge().getQualifiedName().toString(), R,
                            symbol.enclClass().name.toString());
                    String resourceName = symbol.name.toString();

                    mMessager.printMessage(Diagnostic.Kind.NOTE, "className: " + className);
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "className.topLevelClassName(): " + className.topLevelClassName());
                    mMessager.printMessage(Diagnostic.Kind.NOTE, "resourceName: " + resourceName);

                    String resId = className.topLevelClassName().equals(ANDROID_R)
                            ? CodeBlock.of("$L.$N", className, resourceName).toString()
                            : CodeBlock.of("$T.$N", className, resourceName).toString();

                    return new Id(value, resId);
                } catch (Exception ignored) {
                }
            }

            return null;
        }

        @Override
        public void visitLiteral(JCTree.JCLiteral jcLiteral) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "visitLiteral");

            try {
                int value = (Integer) jcLiteral.value;
                resourceIds.put(value, String.valueOf(value));
            } catch (Exception ignored) {
            }
        }

        void reset() {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "reset");

            resourceIds.clear();
        }

        public void setMessage(Messager messager) {
            mMessager = messager;
        }
    }


    private static class Id {
        public Id(int value, String resId) {
            this.value = value;
            this.resId = resId;
        }

        int value;
        String resId;
    }
}

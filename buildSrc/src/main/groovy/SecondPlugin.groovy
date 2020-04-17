import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Plugin
import org.gradle.api.Project

class SecondPlugin implements Plugin<Project> {

    void apply(Project project) {
        System.out.println("========================");
        System.out.println("插件!");
        System.out.println("========================");
//        project.extensions.getByType(AppExtension).registerTransform(new MyTransform(project));
    }
}

class MyTransform extends Transform {
    TransformOutputProvider outputProvider;
    Project mProject;

    MyTransform(Project project) {
        super()
        mProject = project
    }

    @Override
    String getName() {
        return "custom-transform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY;
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        System.out.println("========================");
        System.out.println("transform");
        System.out.println("========================");

        outputProvider = transformInvocation.getOutputProvider();

        transformInvocation.inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->

                File dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY);


                ClassPool classPool = new ClassPool()
                String path = directoryInput.file.absolutePath;
                classPool.appendClassPath(mProject.android.bootClasspath[0].toString())
                classPool.appendClassPath(path)
                System.out.println("absolutePath = " +  mProject.dependencies);

                directoryInput.file.eachFileRecurse { File file ->
                    if (file.name.contains("MainActivity")) {
                        CtClass ctClass = classPool.getCtClass("com.srise.aop.MainActivity")
                        System.out.println("ctClass = " + ctClass);

                        if (ctClass.isFrozen())
                            ctClass.defrost()

                        CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate");
                        System.out.println("ctMethod = " + ctMethod);

                        String str = """android.util.Log.d("shi", "shixi");"""
                        ctMethod.insertBefore(str)

                        System.out.println("writeFile = " + path);

                        ctClass.writeFile(path)
                        ctClass.detach()
                    }
                }

                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }
}
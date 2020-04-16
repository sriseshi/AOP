import org.apache.tools.ant.taskdefs.Transform;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MyPlugin  implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("========================");
        System.out.println("java插件!");
        System.out.println("========================");
    }
}
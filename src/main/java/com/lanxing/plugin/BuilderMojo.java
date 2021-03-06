package com.lanxing.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 *
 * @phase install
 *
 * @requiresProject true
 */
public class BuilderMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the file.
     * @parameter expression="${project.basedir}/src/main/java"
     * @required
     */
    private String projectDirectory;

    /**
     * @parameter expression="${echo.class}"
     */
    private String className;

    public void execute()
            throws MojoExecutionException
    {
        if (className == null){
            throw new IllegalStateException("need class name");
        }
        BufferedReader reader = null;
        BufferedWriter targetOut = null;
        try {
            //maven插件运行系统并不会加载maven工程的jar包，因此需要将maven工程中的包加载到maven插件中
            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<?> sysclass = URLClassLoader.class;
            Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            List<String> classpathElements = project.getCompileClasspathElements();
            getLog().debug("----------start laod projct classes----------");
            for (int i = 0; i < classpathElements.size(); ++i) {
                getLog().debug(classpathElements.get(i));
                method.invoke(sysloader, new File(classpathElements.get(i)).toURI().toURL());
            }
            getLog().debug("----------end laod projct classes----------");


            Class sourceClass = Class.forName(className);
            String sourceClassName = className.substring(className.lastIndexOf(".") + 1, className.length());
            String targetClassName = sourceClassName + "Builder";
            String filePath = className.replace('.', '/');
            String sourceFilePath = projectDirectory + "/" + filePath + ".java";
            String targetFilePath = projectDirectory + "/" + filePath + "Builder.java";
            System.out.println(sourceFilePath);
            File sourceFile = new File(sourceFilePath);
            if (!sourceFile.exists()){
                throw new Exception(sourceFilePath + " is not exit");
            }
            reader = new BufferedReader(new FileReader(sourceFile));
            targetOut = new BufferedWriter(new FileWriter(targetFilePath));
            String tempString = null;
            while ((tempString = reader.readLine()) != null){
                //设置文件头
                if (tempString.contains("package") || tempString.contains("import")){
                    targetOut.write(tempString);
                    targetOut.newLine();
                    continue;
                }
            }

            writeClassStatement(targetOut);

            //类名
            targetOut.write("public class " + targetClassName + " {\n");

            Field[] fields = sourceClass.getDeclaredFields();

            writeFields(targetOut, fields, sourceClassName);

            writeSet(targetOut, fields, sourceClassName, targetClassName);

            writeSourceOutConstract(targetOut, fields, sourceClassName, sourceClass);

        } catch (Exception e) {
            this.getLog().debug(e);
            throw new MojoExecutionException(e.getMessage());
        }finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (targetOut != null){
                try {
                    targetOut.newLine();
                    targetOut.write("}");
                    targetOut.flush();
                    targetOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Class tmpClass = Class.forName(className);
            System.out.println(tmpClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成参数
     * @param out
     * @param fields
     * @throws IOException
     */
    private void writeFields(BufferedWriter out, Field[] fields, String sourceClassName) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (Field field : fields){
            if (field.getName().equals("serialVersionUID")){
                continue;
            }
            sb.append("\n")
                    .append("\t/**\n")
                    .append("\t * generate by builder-plugin ")
                    .append(sourceClassName).append(".").append(field.getName()).append("\n")
                    .append("\t */\n")
                    .append("\tprivate ").append(field.getType().getSimpleName())
                    .append(" ").append(field.getName()).append(";\n");

        }
        sb.append("\n");
        out.write(sb.toString());
    }

    /**
     * 生成get,set和build方法
     * @param out
     * @param fields
     * @param targetName
     * @throws IOException
     */
    private void writeSet(BufferedWriter out, Field[] fields, String sourceClassName, String targetName) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (Field field : fields){
            if (field.getName().equals("serialVersionUID")){
                continue;
            }
            sb.setLength(0);
            String paraName = field.getName();
            paraName = paraName.substring(0,1).toUpperCase() + paraName.substring(1);

            //set方法
            sb.append("\t").append("public ").append(targetName).append(" set").append(paraName)
                    .append("(").append(field.getType().getSimpleName()).append(" ")
                    .append(field.getName()).append(") {\n\t\t")
                    .append("this.").append(field.getName()).append(" = ").append(field.getName())
                    .append(";\n\t\treturn this")
                    .append(";\n\t}\n\n");

//            //get方法
//            sb.append("\t").append("public ").append(field.getType().getSimpleName()).append(" get")
//                    .append(paraName).append("() {\n\t\t")
//                    .append("return this.").append(field.getName()).append(";\n\t}\n\n");

            out.write(sb.toString());
        }

        sb.setLength(0);


    }

    private void writeSourceOutConstract(BufferedWriter out, Field[] fields, String sourceClassName, Class sourceClass) throws IOException {
        StringBuffer sb = new StringBuffer();
        String tmpClassName = sourceClassName.substring(0, 1).toLowerCase() + sourceClassName.substring(1);

        sb.append("\t/**\n").append("\t * generate by builder-plugin\n")
                .append("\t * @return {@link ").append(sourceClass.getName()).append("}\n")
                .append("\t */\n")
                .append("\tpublic ").append(sourceClassName).append(" build() {\n")
                .append("\t\t").append(sourceClassName).append(" ").append(tmpClassName)
                .append(" = new ").append(sourceClassName).append("();\n");
        for (Field field : fields){
            String paraName = field.getName();
            paraName = paraName.substring(0,1).toUpperCase() + paraName.substring(1);
            sb.append("\t\t").append(tmpClassName).append(".set").append(paraName)
                    .append("(this.").append(field.getName()).append(");\n");
        }
        sb.append("\t\treturn ").append(tmpClassName).append(";\n\t}");
        out.write(sb.toString());
    }

    private void writeClassStatement(BufferedWriter out) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("/**\n" +
                " * generate by builder-plugin\n" +
                " */\n");
        out.write(sb.toString());
    }
}

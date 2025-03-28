* Java 9+ 模块化系统（JPMS） 对反射访问的限制。CGLIB 在动态生成代理类时，需要访问 java.lang.ClassLoader 的 defineClass 方法，但 Java 模块系统默认不允许外部模块访问 java.base 模块中的某些内部成员
* 解决方案：
vm参数新增（点击 RUN-Edit Configurations，将上述内容添加到 VM options 文本框中，如果没有，点击右边得Modify options,选择Add VM options）

* `--add-opens java.base/java.lang=ALL-UNNAMED 
--add-exports java.base/jdk.internal.module=ALL-UNNAMED`


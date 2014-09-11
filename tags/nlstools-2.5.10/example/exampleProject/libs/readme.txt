You need to copy the following libraries into this directory as the classpath for the nlstools's ant tasks
----------------------------------------------------------------------------------------------------------


to determine the libs according to the version of nlstools, execute 'mvn dependency:copy-dependencies' in the nlstools directory and use the jars in directory target/dependency

 * nlstools*.jar (or use from ../../../target/)
 * commons-io-*.jar
 * commons-lang*.jar
 * xpp3_min-*.jar
 * xstream-*.jar
 * poi-*.jar (optional for excel support)
 * jsontools-core-*.jar (optional for JSON support)

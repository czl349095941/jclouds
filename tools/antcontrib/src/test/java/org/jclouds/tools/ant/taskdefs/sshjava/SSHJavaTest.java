/**
 *
 * Copyright (C) 2009 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.tools.ant.taskdefs.sshjava;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Environment.Variable;
import org.jclouds.tools.ant.TestClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * @author Adrian Cole
 */
@Test(groups = "unit", testName = "jclouds.SSHJavaTest")
public class SSHJavaTest {
   public static final Entry<String, String> LAST_ENV = Iterables.getLast(System.getenv()
            .entrySet());

   // TODO, this test will break in windows
   public void testShift() throws SecurityException, NoSuchMethodException {
      SSHJava task = makeSSHJava();
      task = directoryShift(task);
      assertEquals(task.shiftMap, ImmutableMap.<String, String> of(System.getProperty("user.home")
               + "/apache-maven-2.2.1", "maven"));
   }

   private Java populateTask(Java task) {
      Project p = new Project();
      task.setProject(p);
      task.setClassname(TestClass.class.getName());
      task.createClasspath().add(new Path(p, "target/test-classes"));
      Variable prop1 = new Environment.Variable();
      prop1.setKey("fooble");
      prop1.setValue("baz");
      task.addSysproperty(prop1);
      Variable prop2 = new Environment.Variable();
      prop2.setKey("foo");
      prop2.setValue("bar");
      task.addSysproperty(prop2);
      task.createJvmarg().setValue("-Xms16m");
      task.createJvmarg().setValue("-Xmx32m");
      Variable env = new Environment.Variable();
      env.setKey(LAST_ENV.getKey());
      env.setValue(LAST_ENV.getValue());
      task.addEnv(env);
      task.createArg().setValue(env.getKey());
      task.createArg().setValue("hello");
      task.createArg().setValue("world");
      task.setDir(new File(System.getProperty("user.dir")));
      task.setFork(true);
      task.setJvm(System.getProperty("java.home") + "/bin/java");
      task.setOutputproperty("out");
      task.setErrorProperty("err");
      task.setResultProperty("result");
      return task;
   }

   @Test(enabled = false, groups = { "live" })
   public void testSsh() throws NumberFormatException, FileNotFoundException, IOException {
      Java java = makeJava();
      java.execute();

      SSHJava javaOverSsh = makeSSHJava();
      addDestinationTo(javaOverSsh);
      javaOverSsh.execute();

      assertEquals(javaOverSsh.getProject().getProperty("out"), javaOverSsh.getProject()
               .getProperty("out"));
      assertEquals(javaOverSsh.getProject().getProperty("err"), javaOverSsh.getProject()
               .getProperty("err"));
      assertEquals(javaOverSsh.getProject().getProperty("result"), javaOverSsh.getProject()
               .getProperty("result"));
   }

   @Test(enabled = false, groups = { "live" })
   public void testSshShift() throws NumberFormatException, FileNotFoundException, IOException {
      Java java = makeJava();
      directoryShift(java);
      java.execute();

      SSHJava javaOverSsh = makeSSHJava();
      addDestinationTo(javaOverSsh);
      directoryShift(javaOverSsh);
      javaOverSsh.execute();

      assertEquals(javaOverSsh.getProject().getProperty("out"), javaOverSsh.getProject()
               .getProperty("out"));
      assertEquals(javaOverSsh.getProject().getProperty("err"), javaOverSsh.getProject()
               .getProperty("err"));
      assertEquals(javaOverSsh.getProject().getProperty("result"), javaOverSsh.getProject()
               .getProperty("result"));
   }

   private void addDestinationTo(SSHJava javaOverSsh) throws UnknownHostException {
      String sshHost = System.getProperty("jclouds.test.ssh.host");
      String sshPort = System.getProperty("jclouds.test.ssh.port");
      String sshUser = System.getProperty("jclouds.test.ssh.username");
      String sshPass = System.getProperty("jclouds.test.ssh.password");
      String sshKeyFile = System.getProperty("jclouds.test.ssh.keyfile");

      int port = (sshPort != null) ? Integer.parseInt(sshPort) : 22;
      InetAddress host = (sshHost != null) ? InetAddress.getByName(sshHost) : InetAddress
               .getLocalHost();
      javaOverSsh.setHost(host.getHostAddress());
      javaOverSsh.setPort(port);
      javaOverSsh.setUsername(sshUser);
      if (sshKeyFile != null && !sshKeyFile.trim().equals("")) {
         javaOverSsh.setKeyfile(sshKeyFile);
      } else {
         javaOverSsh.setPassword(sshPass);
      }
   }

   private SSHJava makeSSHJava() {
      SSHJava task = new SSHJava();
      populateTask(task);
      task.setRemotebase(new File("/tmp/foo"));
      task.setTrust(true);
      return task;
   }

   private Java makeJava() {
      return populateTask(new Java());
   }

   private <T extends Java> T directoryShift(T java) {
      Variable prop1 = new Environment.Variable();
      prop1.setKey("sshjava.map." + System.getProperty("user.home") + "/apache-maven-2.2.1");
      prop1.setValue("maven");
      java.addSysproperty(prop1);
      Variable prop2 = new Environment.Variable();
      prop2.setKey("settingsfile");
      prop2.setValue(System.getProperty("user.home") + "/apache-maven-2.2.1/conf/settings.xml");
      java.addSysproperty(prop2);
      Variable prop3 = new Environment.Variable();
      prop3.setKey("appHome");
      prop3.setValue(System.getProperty("user.home") + "/apache-maven-2.2.1");
      java.addSysproperty(prop3);
      return java;
   }
}

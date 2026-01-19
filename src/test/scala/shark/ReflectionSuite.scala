/*
 * Copyright (C) 2012 The Regents of The University California. 
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shark

import org.scalatest.FunSuite


/**
 * A suite of test to ensure reflections are used properly in Shark to invoke
 * Hive non-public methods. This is needed because we cannot detect reflection
 * errors until runtime. Every time reflection is used to expand visibility of
 * methods or variables, a test should be added.
 */
class ReflectionSuite extends FunSuite {

  private def declaredMethodReturnType(
      clazz: Class[_],
      name: String,
      paramTypes: Class[_]*): Class[_] = {
    val method = clazz.getDeclaredMethod(name, paramTypes: _*)
    method.setAccessible(true)
    method.getReturnType
  }

  private def declaredFieldType(clazz: Class[_], name: String): Class[_] = {
    val field = clazz.getDeclaredField(name)
    field.setAccessible(true)
    field.getType
  }

  test("CliDriver") {
    val clazz = classOf[org.apache.hadoop.hive.cli.CliDriver]

    assert(declaredMethodReturnType(
      clazz,
      "getFormattedDb",
      classOf[org.apache.hadoop.hive.conf.HiveConf],
      classOf[org.apache.hadoop.hive.cli.CliSessionState]) === classOf[String])

    assert(declaredMethodReturnType(
      clazz,
      "spacesForString",
      classOf[String]) === classOf[String])
  }

  test("Driver") {
    val clazz = classOf[org.apache.hadoop.hive.ql.Driver]

    assert(declaredMethodReturnType(
      clazz,
      "doAuthorization",
      classOf[org.apache.hadoop.hive.ql.parse.BaseSemanticAnalyzer]) === Void.TYPE)

    assert(declaredMethodReturnType(
      clazz,
      "getHooks",
      classOf[org.apache.hadoop.hive.conf.HiveConf.ConfVars],
      classOf[Class[_]]) === classOf[java.util.List[_]])

    assert(declaredFieldType(clazz, "plan") ===
      classOf[org.apache.hadoop.hive.ql.QueryPlan])

    assert(declaredFieldType(clazz, "ctx") ===
      classOf[org.apache.hadoop.hive.ql.Context])

    assert(declaredFieldType(clazz, "schema") ===
      classOf[org.apache.hadoop.hive.metastore.api.Schema])

    assert(declaredFieldType(clazz, "LOG") ===
      classOf[org.apache.commons.logging.Log])
  }

  test("SemanticAnalyzer") {
    val clazz = classOf[org.apache.hadoop.hive.ql.parse.SemanticAnalyzer]

    assert(declaredMethodReturnType(
      clazz,
      "validateCreateTable",
      classOf[org.apache.hadoop.hive.ql.plan.CreateTableDesc]) === Void.TYPE)

    assert(declaredMethodReturnType(
      clazz,
      "convertRowSchemaToViewSchema",
      classOf[org.apache.hadoop.hive.ql.parse.RowResolver]) ===
      classOf[java.util.List[_]])

    assert(declaredFieldType(clazz, "viewsExpanded") ===
      classOf[java.util.ArrayList[_]])
  }

  test("UnionOperator") {
    val clazz = classOf[org.apache.hadoop.hive.ql.exec.UnionOperator]
    assert(declaredFieldType(clazz, "needsTransform") === classOf[Array[Boolean]])
  }

  test("FileSinkOperator") {
    val fileSinkClass = classOf[org.apache.hadoop.hive.ql.exec.FileSinkOperator]
    assert(declaredFieldType(fileSinkClass, "fsp") ===
      classOf[org.apache.hadoop.hive.ql.exec.FileSinkOperator#FSPaths])

    val fsPathsClass =
      classOf[org.apache.hadoop.hive.ql.exec.FileSinkOperator#FSPaths]
    assert(declaredFieldType(fsPathsClass, "finalPaths") ===
      classOf[Array[org.apache.hadoop.fs.Path]])
  }
}

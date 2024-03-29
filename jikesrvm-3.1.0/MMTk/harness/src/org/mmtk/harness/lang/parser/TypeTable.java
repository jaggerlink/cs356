/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.mmtk.harness.lang.parser;

import java.util.HashMap;
import java.util.Map;

import org.mmtk.harness.lang.Trace;
import org.mmtk.harness.lang.type.Type;
import org.mmtk.harness.lang.type.TypeReference;

/**
 * Parser method table.
 */
public class TypeTable {

  private Map<String, Type> table = new HashMap<String, Type>();

  TypeTable() {
    add(Type.INT);
    add(Type.STRING);
    add(Type.BOOLEAN);
    add(Type.OBJECT);
    add(Type.VOID);
  }

  public void add(Type t) {
    Trace.trace(Trace.Item.PARSER,"defining type %s", t);
    if (table.containsKey(t.getName()))
      throw new RuntimeException("Type " + t.getName() + " already defined");
    table.put(t.getName(), t);
  }

  /**
   * Return either the named type or a proxy for the named type.
   * @param name
   * @return
   */
  public Type get(String name) {
    if (!table.containsKey(name))
      return new TypeReference(this,name);
    return table.get(name);
  }

  /**
   * Is there a type by the given name ?
   * @param name
   * @return
   */
  public boolean isDefined(String name) {
    return table.containsKey(name);
  }
}

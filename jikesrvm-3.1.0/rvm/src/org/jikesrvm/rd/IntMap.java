package org.jikesrvm.rd;

import org.jikesrvm.VM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.NoCheckStore;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;

/** RaceDet: map from integers to objects */
@Uninterruptible
abstract class IntMap<V> {
  
  V[] data;
  int size;

  IntMap() {
    this.data = newArray(1);
  }
  
  abstract V[] newArray(int length);
  
  abstract int getKey(V value);
  
  abstract V getNext(V value);

  abstract void setNext(V value, V next);

  final V get(int key) {
    check(true);
    int index = key % data.length;
    V tmp = data[index];
    while (tmp != null && getKey(tmp) != key) {
      tmp = getNext(tmp);
    }
    return tmp;
  }

  final void put(V value) {
    check(true);
    putInternal(value);
    size++;
    check(false);
    if (size > data.length) {
      resize(data.length << 1);
    }
    check(true);
  }

  @NoCheckStore
  @Inline
  private final void putInternal(V value) {
    int index = getKey(value) % data.length;
    setNext(value, data[index]);
    data[index] = value;
  }
  
  final void remove(int key) {
    remove(key, true);
  }
  
  final void removeIfPresent(int key) {
    remove(key, false);
  }
  
  @Inline
  private final void remove(int key, boolean mustExist) {
    check(true);
    boolean result = removeInternal(key);
    if (!result) {
      if (VM.VerifyAssertions) { VM._assert(!mustExist); }
      return;
    }
    size--;
    check(false);
    if (size < (data.length >> 1)) {
      resize(data.length >> 1);
    }
    check(true);
  }

  @NoCheckStore
  @Inline
  private final boolean removeInternal(int key) {
    int index = key % data.length;
    V tmp = data[index];
    if (tmp == null) {
      return false;
    }
    if (getKey(tmp) == key) {
      data[index] = getNext(tmp);
    } else {
      V prev = tmp;
      while (getKey(tmp) != key) {
        prev = tmp;
        tmp = getNext(tmp);
        if (tmp == null) {
          return false;
        }
      }
      setNext(prev, getNext(tmp));
    }
    return true;
  }

  @NoInline
  private final void resize(int newLength) {
    V[] oldData = data;
    data = newArray(newLength);
    for (V tmp : oldData) {
      V next;
      for (; tmp != null; tmp = next) {
        next = getNext(tmp);
        putInternal(tmp);
      }
    }
  }
  
  final V getFirst() {
    for (V tmp : data) {
      if (tmp != null) {
        return tmp;
      }
    }
    return null;
  }
  
  final int size() {
    return size;
  }
  
  @Inline
  final void check(boolean checkFullness) {
    /*
    if (VM.VerifyAssertions) {
      checkNoInline(checkFullness, false);
    }
    */
  }
  
  @NoInline
  final void checkNoInline(boolean checkFullness, boolean verbose) {

    if (verbose) {
      for (int i = 0; i < data.length; i++) {
        VM.sysWrite(i);
        VM.sysWrite(":");
        V bucket = data[i];
        for (V tmp = bucket; tmp != null; tmp = getNext(tmp)) {
          VM.sysWrite(" ");
          VM.sysWrite(getKey(tmp));
        }
        VM.sysWrite("; ");
      }
      VM.sysWriteln();
    }
    
    VM._assert(data.length > 0);
    if (checkFullness) {
      VM._assert(size >= (data.length >> 1));
      VM._assert(size <= data.length);
    }
    int numElements = 0;
    for (V bucket : data) {
      for (V tmp = bucket; tmp != null; tmp = getNext(tmp)) {
        numElements++;
      }
    }
    VM._assert(size == numElements);
  }

}

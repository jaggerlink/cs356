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
package org.jikesrvm.runtime;

import static org.jikesrvm.SizeConstants.BYTES_IN_ADDRESS;
import static org.jikesrvm.SizeConstants.BYTES_IN_INT;
import static org.jikesrvm.SizeConstants.LOG_BYTES_IN_DOUBLE;
import static org.jikesrvm.SizeConstants.LOG_BYTES_IN_INT;
import static org.jikesrvm.SizeConstants.LOG_BYTES_IN_SHORT;

import org.jikesrvm.VM;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * Low level memory management functions.
 *
 * Note that this class is "uninterruptible" - calling its methods will never
 * cause the current thread to yield the cpu to another thread (one that
 * might cause a gc, for example).
 */
@Uninterruptible
public class Memory {

  ////////////////////////
  // (1) Utilities for copying/filling/zeroing memory
  ////////////////////////

  /**
   * How many bytes is considered large enough to justify the transition to
   * C code to use memcpy?
   */
  private static final int NATIVE_THRESHOLD = 512;

  /**
   * Allow the use of C based memcpy
   */
  private static final boolean USE_NATIVE = true;

  /**
   * Number of bytes used when copying larger chunks of memory. Normally 8 bytes
   * except on x87 Intel
   */
  private static final int BYTES_IN_COPY = VM.BuildForIA32 && !VM.BuildForSSE2 ? 4 : 8;

  @Inline
  private static void copy8Bytes(Address dstPtr, Address srcPtr) {
    if (BYTES_IN_COPY == 8) {
      if (VM.BuildForIA32) {
        dstPtr.store(srcPtr.loadLong());
      } else {
        dstPtr.store(srcPtr.loadDouble());
      }
    } else {
      copy4Bytes(dstPtr, srcPtr);
      copy4Bytes(dstPtr.plus(4), srcPtr.plus(4));
    }
  }
  @Inline
  private static void copy4Bytes(Address dstPtr, Address srcPtr) {
    dstPtr.store(srcPtr.loadInt());
  }
  @Inline
  private static void copy2Bytes(Address dstPtr, Address srcPtr) {
    dstPtr.store(srcPtr.loadChar());
  }
  @Inline
  private static void copy1Bytes(Address dstPtr, Address srcPtr) {
    dstPtr.store(srcPtr.loadByte());
  }
  /**
   * Low level copy of len elements from src[srcPos] to dst[dstPos].
   *
   * Assumptions: <code> src != dst || (scrPos >= dstPos + 4) </code>
   *              and src and dst are 8Bit arrays.
   * @param src     the source array
   * @param srcPos  index in the source array to begin copy
   * @param dst     the destination array
   * @param dstPos  index in the destination array to being copy
   * @param len     number of array elements to copy
   */
  public static void arraycopy8Bit(Object src, int srcPos, Object dst, int dstPos, int len) {
    if (USE_NATIVE && len > NATIVE_THRESHOLD) {
      memcopy(Magic.objectAsAddress(dst).plus(dstPos), Magic.objectAsAddress(src).plus(srcPos), len);
    } else {
      if (len >= BYTES_IN_COPY && (srcPos & (BYTES_IN_COPY - 1)) == (dstPos & (BYTES_IN_COPY - 1))) {
        // relative alignment is the same
        Address srcPtr = Magic.objectAsAddress(src).plus(srcPos);
        Address dstPtr = Magic.objectAsAddress(dst).plus(dstPos);
        Address endPtr = srcPtr.plus(len);
        Address wordEndPtr = endPtr.toWord().and(Word.fromIntZeroExtend(BYTES_IN_COPY-1).not()).toAddress();

        if (BYTES_IN_COPY == 8) {
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(1)).NE(Word.zero())) {
            copy1Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(1);
            dstPtr = dstPtr.plus(1);
          }
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(2);
            dstPtr = dstPtr.plus(2);
          }
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(4)).NE(Word.zero())) {
            copy4Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(4);
            dstPtr = dstPtr.plus(4);
          }
        } else {
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(1)).NE(Word.zero())) {
            copy1Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(1);
            dstPtr = dstPtr.plus(1);
          }
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(2);
            dstPtr = dstPtr.plus(2);
          }
        }
        while (srcPtr.LT(wordEndPtr)) {
          if (BYTES_IN_COPY == 8) {
            copy8Bytes(dstPtr, srcPtr);
          } else {
            copy4Bytes(dstPtr, srcPtr);
          }
          srcPtr = srcPtr.plus(BYTES_IN_COPY);
          dstPtr = dstPtr.plus(BYTES_IN_COPY);
        }
        // if(VM.VerifyAssertions) VM._assert(wordEndPtr.EQ(srcPtr));
        if (BYTES_IN_COPY == 8) {
          if (endPtr.toWord().and(Word.fromIntZeroExtend(4)).NE(Word.zero())) {
            copy4Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(4);
            dstPtr = dstPtr.plus(4);
          }
          if (endPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(2);
            dstPtr = dstPtr.plus(2);
          }
          if (endPtr.toWord().and(Word.fromIntZeroExtend(1)).NE(Word.zero())) {
            copy1Bytes(dstPtr, srcPtr);
          }
        } else {
          if (endPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(2);
            dstPtr = dstPtr.plus(2);
          }
          if (endPtr.toWord().and(Word.fromIntZeroExtend(1)).NE(Word.zero())) {
            copy1Bytes(dstPtr, srcPtr);
          }
        }
      } else {
        Address srcPtr = Magic.objectAsAddress(src).plus(srcPos);
        Address dstPtr = Magic.objectAsAddress(dst).plus(dstPos);
        Address endPtr = srcPtr.plus(len);
        while (srcPtr.LT(endPtr)) {
          dstPtr.store(srcPtr.loadByte());
          srcPtr = srcPtr.plus(1);
          dstPtr = dstPtr.plus(1);
        }
      }
    }
  }

  /**
   * Low level copy of len elements from src[srcPos] to dst[dstPos].
   *
   * Assumption src != dst || (srcPos >= dstPos + 2).
   *
   * @param src     the source array
   * @param srcPos  index in the source array to begin copy
   * @param dst     the destination array
   * @param dstPos  index in the destination array to being copy
   * @param len     number of array elements to copy
   */
  public static void arraycopy16Bit(Object src, int srcPos, Object dst, int dstPos, int len) {
    if (USE_NATIVE && len > (NATIVE_THRESHOLD >> LOG_BYTES_IN_SHORT)) {
      memcopy(Magic.objectAsAddress(dst).plus(dstPos << LOG_BYTES_IN_SHORT),
              Magic.objectAsAddress(src).plus(srcPos << LOG_BYTES_IN_SHORT),
              len << LOG_BYTES_IN_SHORT);
    } else {
      if (len >= (BYTES_IN_COPY >>> LOG_BYTES_IN_SHORT) &&
          (srcPos & ((BYTES_IN_COPY - 1) >>> LOG_BYTES_IN_SHORT)) ==
          (dstPos & ((BYTES_IN_COPY - 1) >>> LOG_BYTES_IN_SHORT))) {
        // relative alignment is the same
        Address srcPtr = Magic.objectAsAddress(src).plus(srcPos << LOG_BYTES_IN_SHORT);
        Address dstPtr = Magic.objectAsAddress(dst).plus(dstPos << LOG_BYTES_IN_SHORT);
        Address endPtr = srcPtr.plus(len << LOG_BYTES_IN_SHORT);
        Address wordEndPtr = endPtr.toWord().and(Word.fromIntZeroExtend(BYTES_IN_COPY-1).not()).toAddress();

        if (BYTES_IN_COPY == 8) {
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(2);
            dstPtr = dstPtr.plus(2);
          }
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(4)).NE(Word.zero())) {
            copy4Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(4);
            dstPtr = dstPtr.plus(4);
          }
        } else {
          if (srcPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(2);
            dstPtr = dstPtr.plus(2);
          }
        }
        while (srcPtr.LT(wordEndPtr)) {
          if (BYTES_IN_COPY == 8) {
            copy8Bytes(dstPtr, srcPtr);
          } else {
            copy4Bytes(dstPtr, srcPtr);
          }
          srcPtr = srcPtr.plus(BYTES_IN_COPY);
          dstPtr = dstPtr.plus(BYTES_IN_COPY);
        }
        // if(VM.VerifyAssertions) VM._assert(wordEndPtr.EQ(srcPtr));
        if (BYTES_IN_COPY == 8) {
          if (endPtr.toWord().and(Word.fromIntZeroExtend(4)).NE(Word.zero())) {
            copy4Bytes(dstPtr, srcPtr);
            srcPtr = srcPtr.plus(4);
            dstPtr = dstPtr.plus(4);
          }
          if (endPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
          }
        } else {
          if (endPtr.toWord().and(Word.fromIntZeroExtend(2)).NE(Word.zero())) {
            copy2Bytes(dstPtr, srcPtr);
          }
        }
      } else {
        Address srcPtr = Magic.objectAsAddress(src).plus(srcPos << LOG_BYTES_IN_SHORT);
        Address dstPtr = Magic.objectAsAddress(dst).plus(dstPos << LOG_BYTES_IN_SHORT);
        Address endPtr = srcPtr.plus(len << LOG_BYTES_IN_SHORT);
        while (srcPtr.LT(endPtr)) {
          copy2Bytes(dstPtr, srcPtr);
          srcPtr = srcPtr.plus(2);
          dstPtr = dstPtr.plus(2);
        }
      }
    }
  }

  /**
   * Low level copy of <code>len</code> elements from <code>src[srcPos]</code> to <code>dst[dstPos]</code>.
   *
   * Assumption: <code>src != dst || (srcPos >= dstPos)</code> and element size is 4 bytes.
   *
   * @param src     the source array
   * @param srcIdx  index in the source array to begin copy
   * @param dst     the destination array
   * @param dstIdx  index in the destination array to being copy
   * @param len     number of array elements to copy
   */
  public static void arraycopy32Bit(Object src, int srcIdx, Object dst, int dstIdx, int len) {
    Address srcPtr = Magic.objectAsAddress(src).plus(srcIdx << LOG_BYTES_IN_INT);
    Address dstPtr = Magic.objectAsAddress(dst).plus(dstIdx << LOG_BYTES_IN_INT);
    int copyBytes = len << LOG_BYTES_IN_INT;
    if (USE_NATIVE && len > (NATIVE_THRESHOLD >> LOG_BYTES_IN_INT)) {
      memcopy(dstPtr, srcPtr, copyBytes);
    } else {
      // The elements of int[] and float[] are always 32 bit aligned
      // therefore we can do 32 bit load/stores without worrying about alignment.
      // TODO: optimize to use 64bit copies. Suspected problem in _202_jess with overlapping arrays
      //aligned32Copy(dstPtr, srcPtr, Offset.fromIntSignExtend(len << LOG_BYTES_IN_INT));
      Address endPtr = srcPtr.plus(copyBytes);
      while (srcPtr.LT(endPtr)) {
        dstPtr.store(srcPtr.loadInt());
        srcPtr = srcPtr.plus(4);
        dstPtr = dstPtr.plus(4);
      }
    }
  }

  /**
   * Low level copy of <code>len</code> elements from <code>src[srcPos]</code> to <code>dst[dstPos]</code>.
   *
   * Assumption <code>src != dst || (srcPos >= dstPos)</code> and element size is 8 bytes.
   *
   * @param src     the source array
   * @param srcIdx  index in the source array to begin copy
   * @param dst     the destination array
   * @param dstIdx  index in the destination array to being copy
   * @param len     number of array elements to copy
   */
  public static void arraycopy64Bit(Object src, int srcIdx, Object dst, int dstIdx, int len) {
    Address srcPtr = Magic.objectAsAddress(src).plus(srcIdx << LOG_BYTES_IN_DOUBLE);
    Address dstPtr = Magic.objectAsAddress(dst).plus(dstIdx << LOG_BYTES_IN_DOUBLE);
    int copyBytes = len << LOG_BYTES_IN_DOUBLE;
    if (USE_NATIVE && len > (NATIVE_THRESHOLD >> LOG_BYTES_IN_DOUBLE)) {
      memcopy(dstPtr, srcPtr, copyBytes);
    } else {
      // The elements of long[] and double[] are always doubleword aligned
      // therefore we can do 64 bit load/stores without worrying about alignment.
      Address endPtr = srcPtr.plus(copyBytes);
      while (srcPtr.LT(endPtr)) {
        copy8Bytes(dstPtr, srcPtr);
        srcPtr = srcPtr.plus(8);
        dstPtr = dstPtr.plus(8);
      }
    }
  }

  /**
   * Copy numbytes from src to dst.
   * Assumption either the ranges are non overlapping, or src >= dst + 4.
   * Also, src and dst are 4 byte aligned and numBytes is a multiple of 4.
   * @param dst the destination addr
   * @param src the source addr
   * @param numBytes the number of bytes top copy
   */
  public static void aligned32Copy(Address dst, Address src, Offset numBytes) {
    if (USE_NATIVE && numBytes.sGT(Offset.fromIntSignExtend(NATIVE_THRESHOLD))) {
      memcopy(dst, src, numBytes.toWord().toExtent());
    } else {
      if (BYTES_IN_COPY == 8) {
        Word wordMask = Word.fromIntZeroExtend(BYTES_IN_COPY-1);
        Word srcAlignment = src.toWord().and(wordMask);
        if (srcAlignment.EQ(dst.toWord().and(wordMask))) {
          Offset i = Offset.zero();
          if (srcAlignment.EQ(Word.fromIntZeroExtend(BYTES_IN_INT))) {
            copy4Bytes(dst.plus(i), src.plus(i));
            i = i.plus(BYTES_IN_INT);
          }
          Word endAlignment = srcAlignment.plus(numBytes).and(wordMask);
          numBytes = numBytes.minus(endAlignment.toOffset());
          for (; i.sLT(numBytes); i = i.plus(BYTES_IN_COPY)) {
            copy8Bytes(dst.plus(i), src.plus(i));
          }
          if (!endAlignment.isZero()) {
            copy4Bytes(dst.plus(i), src.plus(i));
          }
          return;
        }
      }
      //normal case: 32 bit or (64 bit not aligned)
      for (Offset i = Offset.zero(); i.sLT(numBytes); i = i.plus(BYTES_IN_INT)) {
        copy4Bytes(dst.plus(i), src.plus(i));
      }
    }
  }

  public static void aligned32Copy(Address dst, Address src, int numBytes) {
    aligned32Copy(dst, src, Offset.fromIntSignExtend(numBytes));
  }

  /**
   * Copy numbytes from src to dst.
   * Assumption either the ranges are non overlapping, or src >= dst + BYTES_IN_ADDRESS.
   * Also, src and dst are word aligned and numBytes is a multiple of BYTES_IN_ADDRESS.
   * @param dst the destination addr
   * @param src the source addr
   * @param numBytes the number of bytes top copy
   */
  public static void alignedWordCopy(Address dst, Address src, int numBytes) {
    if (USE_NATIVE && numBytes > NATIVE_THRESHOLD) {
      memcopy(dst, src, numBytes);
    } else {
      internalAlignedWordCopy(dst, src, numBytes);
    }
  }

  /**
   * Copy <code>numbytes</code> from <code>src</code> to <code>dst</code>.
   * Assumption either the ranges are non overlapping, or <code>src >= dst + BYTES_IN_ADDRESS</code>.
   * @param dst     The destination addr
   * @param src     The source addr
   * @param numBytes The number of bytes to copy
   */
  private static void internalAlignedWordCopy(Address dst, Address src, int numBytes) {
    Address end = src.plus(numBytes);
    while (src.LT(end)) {
      dst.store(src.loadWord());
      src = src.plus(BYTES_IN_ADDRESS);
      dst = dst.plus(BYTES_IN_ADDRESS);
    }
  }

  /**
   * Copy a region of memory.
   * @param dst   Destination address
   * @param src   Source address
   * @param cnt   Number of bytes to copy
   * Assumption: source and destination regions do not overlap
   */
  public static void memcopy(Address dst, Address src, Extent cnt) {
    SysCall.sysCall.sysCopy(dst, src, cnt);
  }

  public static void memcopy(Address dst, Address src, int cnt) {
    SysCall.sysCall.sysCopy(dst, src, Extent.fromIntSignExtend(cnt));
  }

  /**
   * Zero a region of memory.
   * @param start of address range (inclusive)
   * @param len extent to zero.
   */
  public static void zero(Address start, Extent len) {
    SysCall.sysCall.sysZero(start, len);
  }

  /**
   * Zero a range of pages of memory.
   * @param start Starting address       (must be a page address)
   * @param len   Number of bytes     (must be multiple of page size)
   */
  public static void zeroPages(Address start, int len) {
    if (VM.VerifyAssertions) VM._assert(isPageAligned(start) && isPageMultiple(len));
    SysCall.sysCall.sysZeroPages(start, len);
  }

  ////////////////////////
  // (2) Cache management
  ////////////////////////

  /**
   * Synchronize a region of memory: force data in dcache to be written out to main
   * memory so that it will be seen by icache when instructions are fetched back.
   * @param address  Start of address range
   * @param size     Size of address range (bytes)
   */
  public static void sync(Address address, int size) {
    SysCall.sysCall.sysSyncCache(address, size);
  }

  ////////////////////////
  // (3) MMap
  ////////////////////////

  // constants for protection and mapping calls
  public static final int PROT_NONE = 0;
  public static final int PROT_READ = 1;
  public static final int PROT_WRITE = 2;
  public static final int PROT_EXEC = 4;

  public static final int MAP_PRIVATE = 2;
  public static final int MAP_FIXED     = (VM.BuildForLinux) ? 16 : (VM.BuildForOsx) ?     16 : (VM.BuildForSolaris) ? 0x10 :256;
  public static final int MAP_ANONYMOUS = (VM.BuildForLinux) ? 32 : (VM.BuildForOsx) ? 0x1000 : (VM.BuildForSolaris) ? 0x100 : 16;

  public static boolean isPageMultiple(int val) {
    int pagesizeMask = getPagesize() - 1;
    return ((val & pagesizeMask) == 0);
  }

  public static boolean isPageMultiple(Extent val) {
    Word pagesizeMask = Word.fromIntZeroExtend(getPagesize() - 1);
    return val.toWord().and(pagesizeMask).isZero();
  }

  public static boolean isPageMultiple(Offset val) {
    Word pagesizeMask = Word.fromIntZeroExtend(getPagesize() - 1);
    return val.toWord().and(pagesizeMask).isZero();
  }

  public static boolean isPageAligned(Address addr) {
    Word pagesizeMask = Word.fromIntZeroExtend(getPagesize() - 1);
    return addr.toWord().and(pagesizeMask).isZero();
  }

  /**
   * Do generic mmap non-file memory mapping call
   * @param address  Start of address range (Address)
   * @param size    Size of address range
   * @param prot    Protection (int)
   * @param flags (int)
   * @return Address (of region) if successful; errno (1 to 127) otherwise
   */
  public static Address mmap(Address address, Extent size, int prot, int flags) {
    if (VM.VerifyAssertions) {
      VM._assert(isPageAligned(address) && isPageMultiple(size));
    }
    return SysCall.sysCall.sysMMapErrno(address, size, prot, flags, -1, Offset.zero());
  }

  /**
   * Do mmap demand zero fixed address memory mapping call
   * @param address  Start of address range
   * @param size     Size of address range
   * @return Address (of region) if successful; errno (1 to 127) otherwise
   */
  public static Address dzmmap(Address address, Extent size) {
    if (VM.VerifyAssertions) {
      VM._assert(isPageAligned(address) && isPageMultiple(size));
    }
    int prot = PROT_READ | PROT_WRITE | PROT_EXEC;
    int flags = MAP_ANONYMOUS | MAP_PRIVATE | MAP_FIXED;
    return mmap(address, size, prot, flags);
  }

  /**
   * Do mprotect system call
   * @param address Start of address range (Address)
   * @param size Size of address range
   * @param prot Protection (int)
   * @return true iff success
   */
  public static boolean mprotect(Address address, Extent size, int prot) {
    if (VM.VerifyAssertions) {
      VM._assert(isPageAligned(address) && isPageMultiple(size));
    }
    return SysCall.sysCall.sysMProtect(address, size, prot) == 0;
  }

  private static int pagesize = -1;
  private static int pagesizeLog = -1;

  /**
   * Do getpagesize call
   * @return page size
   */
  public static int getPagesize() {
    if (pagesize == -1) {
      pagesize = SysCall.sysCall.sysGetPageSize();
      pagesizeLog = -1;
      int temp = pagesize;
      while (temp > 0) {
        temp >>>= 1;
        pagesizeLog++;
      }
      if (VM.VerifyAssertions) VM._assert((1 << pagesizeLog) == pagesize);
    }
    return pagesize;
  }

  public static void dumpMemory(Address start, int beforeBytes, int afterBytes) {

    beforeBytes = alignDown(beforeBytes, BYTES_IN_ADDRESS);
    afterBytes = alignUp(afterBytes, BYTES_IN_ADDRESS);
    VM.sysWrite("---- Dumping memory from ");
    VM.sysWrite(start.minus(beforeBytes));
    VM.sysWrite(" to ");
    VM.sysWrite(start.plus(afterBytes));
    VM.sysWrite(" ----\n");
    for (int i = -beforeBytes; i < afterBytes; i += BYTES_IN_ADDRESS) {
      VM.sysWrite(i, ": ");
      VM.sysWrite(start.plus(i));
      Word value = start.plus(i).loadWord();
      VM.sysWriteln("  ", value);
    }
  }

  @Inline
  public static Address alignUp(Address address, int alignment) {
    return address.plus(alignment - 1).toWord().and(Word.fromIntSignExtend(~(alignment - 1))).toAddress();
  }

  @Inline
  public static Address alignDown(Address address, int alignment) {
    return address.toWord().and(Word.fromIntSignExtend(~(alignment - 1))).toAddress();
  }

  // These versions are here to accommodate the boot image writer
  @Inline
  public static int alignUp(int address, int alignment) {
    return ((address + alignment - 1) & ~(alignment - 1));
  }

  @Inline
  public static int alignDown(int address, int alignment) {
    return (address & ~(alignment - 1));
  }
}

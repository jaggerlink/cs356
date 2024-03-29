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
package org.jikesrvm;

import org.vmmagic.unboxed.*;

/**
 * Constants defining heap layout constants
 */
public interface HeapLayoutConstants {

  /** The address of the start of the data section of the boot image. */
  Address BOOT_IMAGE_DATA_START =
    Address.fromIntZeroExtend( 0x10000000 );

  /** The address of the start of the code section of the boot image. */
  Address BOOT_IMAGE_CODE_START =
    Address.fromIntZeroExtend( 0x14000000 );

  /** The address of the start of the ref map section of the boot image. */
  Address BOOT_IMAGE_RMAP_START =
    Address.fromIntZeroExtend( 0x16000000 );

  /** The address in virtual memory that is the highest that can be mapped. */
  Address MAXIMUM_MAPPABLE =
    Address.fromIntZeroExtend( 0xa8000000 );

  /** The maximum boot image data size */
  int BOOT_IMAGE_DATA_SIZE = 56<<20;

  /** The maximum boot image code size */
  int BOOT_IMAGE_CODE_SIZE = 24<<20;

  /* Typical compression ratio is about 1/20 */
  int BAD_MAP_COMPRESSION = 5;  // conservative heuristic
  int MAX_BOOT_IMAGE_RMAP_SIZE = BOOT_IMAGE_DATA_SIZE/BAD_MAP_COMPRESSION;

  /** The address of the end of the data section of the boot image. */
  Address BOOT_IMAGE_DATA_END = BOOT_IMAGE_DATA_START.plus(BOOT_IMAGE_DATA_SIZE);
  /** The address of the end of the code section of the boot image. */
  Address BOOT_IMAGE_CODE_END = BOOT_IMAGE_CODE_START.plus(BOOT_IMAGE_CODE_SIZE);
  /** The address of the end of the ref map section of the boot image. */
  Address BOOT_IMAGE_RMAP_END = BOOT_IMAGE_RMAP_START.plus(MAX_BOOT_IMAGE_RMAP_SIZE);
  /** The address of the end of the boot image. */
  Address BOOT_IMAGE_END = BOOT_IMAGE_RMAP_END;
}

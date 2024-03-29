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

import static org.jikesrvm.runtime.EntrypointHelper.getField;
import static org.jikesrvm.runtime.EntrypointHelper.getMethod;

import org.jikesrvm.VM;
import org.jikesrvm.classloader.RVMField;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.NormalMethod;

/**
 * Fields and methods of the virtual machine that are needed by
 * compiler-generated machine code or C runtime code.
 */
public class Entrypoints {
  // The usual causes for getField/Method() to fail are:
  //  1. you mispelled the class name, member name, or member signature
  //  2. the class containing the specified member didn't get compiled
  //

  public static final NormalMethod bootMethod = EntrypointHelper.getMethod(org.jikesrvm.VM.class, "boot", "()V");

  public static final RVMMethod java_lang_reflect_Method_invokeMethod =
      getMethod(java.lang.reflect.Method.class, "invoke",
          "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
  public static final RVMMethod getClassFromStackFrame =
    getMethod(org.jikesrvm.classloader.RVMClass.class, "getClassFromStackFrame", "(I)Lorg/jikesrvm/classloader/RVMClass;");
  public static final RVMMethod getClassLoaderFromStackFrame =
    getMethod(org.jikesrvm.classloader.RVMClass.class, "getClassLoaderFromStackFrame", "(I)Ljava/lang/ClassLoader;");

  public static final RVMField magicObjectRemapperField =
      getField(org.jikesrvm.runtime.Magic.class,
               "objectAddressRemapper",
               org.jikesrvm.runtime.ObjectAddressRemapper.class);

  public static final NormalMethod instanceOfMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "instanceOf", "(Ljava/lang/Object;I)Z");
  public static final NormalMethod checkcastMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "checkcast", "(Ljava/lang/Object;I)V");
  public static final NormalMethod checkstoreMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "checkstore", "(Ljava/lang/Object;Ljava/lang/Object;)V");
  public static final NormalMethod aastoreMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "aastore", "([Ljava/lang/Object;ILjava/lang/Object;)V");
  public static final NormalMethod aastoreUninterruptibleMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "aastoreUninterruptible", "([Ljava/lang/Object;ILjava/lang/Object;)V");
  public static final NormalMethod athrowMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "athrow", "(Ljava/lang/Throwable;)V");

  // Allocation-related entry points
  //
  public static final NormalMethod resolvedNewScalarMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class,
                "resolvedNewScalar",
                "(ILorg/jikesrvm/objectmodel/TIB;ZIIII)Ljava/lang/Object;");
  public static final NormalMethod unresolvedNewScalarMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "unresolvedNewScalar", "(II)Ljava/lang/Object;");
  public static final NormalMethod unresolvedNewArrayMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "unresolvedNewArray", "(III)Ljava/lang/Object;");
  public static final NormalMethod resolvedNewArrayMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class,
                "resolvedNewArray",
                "(IIILorg/jikesrvm/objectmodel/TIB;IIII)Ljava/lang/Object;");

  public static final RVMField sysWriteLockField = getField(org.jikesrvm.VM.class, "sysWriteLock", int.class);
  public static final RVMField intBufferLockField =
      getField(org.jikesrvm.Services.class, "intBufferLock", int.class);
  public static final RVMField dumpBufferLockField =
      getField(org.jikesrvm.Services.class, "dumpBufferLock", int.class);

  public static final NormalMethod unexpectedAbstractMethodCallMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "unexpectedAbstractMethodCall", "()V");
  public static final NormalMethod raiseNullPointerException =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "raiseNullPointerException", "()V");
  public static final NormalMethod raiseArrayBoundsException =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "raiseArrayIndexOutOfBoundsException", "(I)V");
  public static final NormalMethod raiseArithmeticException =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "raiseArithmeticException", "()V");
  public static final NormalMethod raiseAbstractMethodError =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "raiseAbstractMethodError", "()V");
  public static final NormalMethod raiseIllegalAccessError =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "raiseIllegalAccessError", "()V");
  public static final NormalMethod deliverHardwareExceptionMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "deliverHardwareException", "(II)V");
  public static final NormalMethod unlockAndThrowMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "unlockAndThrow", "(Ljava/lang/Object;Ljava/lang/Throwable;)V");

  // RaceDet: *uninstrumented* version
  public static final NormalMethod unlockAndThrowWithoutRaceDetInstrMethod =
      getMethod(org.jikesrvm.runtime.RuntimeEntrypoints.class, "unlockAndThrowWithoutRaceDetInstr", "(Ljava/lang/Object;Ljava/lang/Throwable;)V");
  
  public static final RVMField gcLockField = getField("Ljava/lang/VMCommonLibrarySupport$GCLock;", "gcLock", int.class);

  public static final NormalMethod invokeInterfaceMethod =
      getMethod(org.jikesrvm.classloader.InterfaceInvocation.class,
                "invokeInterface",
                "(Ljava/lang/Object;I)Lorg/jikesrvm/ArchitectureSpecific$CodeArray;");
  public static final NormalMethod findItableMethod =
      getMethod(org.jikesrvm.classloader.InterfaceInvocation.class,
                "findITable",
                "(Lorg/jikesrvm/objectmodel/TIB;I)Lorg/jikesrvm/objectmodel/ITable;");
  public static final NormalMethod unresolvedInvokeinterfaceImplementsTestMethod =
      getMethod(org.jikesrvm.classloader.InterfaceInvocation.class,
                "unresolvedInvokeinterfaceImplementsTest",
                "(ILjava/lang/Object;)V");

  public static final NormalMethod lockMethod =
      getMethod(org.jikesrvm.objectmodel.ObjectModel.class, "genericLock", "(Ljava/lang/Object;)V");
  public static final NormalMethod unlockMethod =
      getMethod(org.jikesrvm.objectmodel.ObjectModel.class, "genericUnlock", "(Ljava/lang/Object;)V");

  public static final NormalMethod inlineLockMethod =
      getMethod(org.jikesrvm.scheduler.ThinLock.class,
                "inlineLock",
                "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;)V");
  public static final NormalMethod inlineUnlockMethod =
      getMethod(org.jikesrvm.scheduler.ThinLock.class,
                "inlineUnlock",
                "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;)V");

  // RaceDet: uninstrumented versions of lock and unlock

  public static final NormalMethod lockWithoutRaceDetInstrMethod =
    getMethod(org.jikesrvm.objectmodel.ObjectModel.class, "genericLockWithoutRaceDetInstr", "(Ljava/lang/Object;)V");
  public static final NormalMethod unlockWithoutRaceDetInstrMethod =
    getMethod(org.jikesrvm.objectmodel.ObjectModel.class, "genericUnlockWithoutRaceDetInstr", "(Ljava/lang/Object;)V");
  public static final NormalMethod inlineLockWithoutRaceDetInstrMethod =
    getMethod(org.jikesrvm.scheduler.ThinLock.class, "inlineLockWithoutRaceDetInstr", "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;)V");
  public static final NormalMethod inlineUnlockWithoutRaceDetInstrMethod =
    getMethod(org.jikesrvm.scheduler.ThinLock.class, "inlineUnlockWithoutRaceDetInstr", "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;)V");

  
  // RaceDet: barrier methods and other entrypoints

  public static final NormalMethod rdFieldReadBarrierResolvedMethod = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierResolved", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdFieldReadBarrierResolvedSamplingMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierResolvedSampling", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdFieldReadBarrierResolvedSamplingStatsMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierResolvedSamplingStats", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdFieldReadBarrierResolvedUsePassedMetadataMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierResolvedUsePassedMetadata", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdFieldReadBarrierStaticResolvedMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierStaticResolved", "(Lorg/vmmagic/unboxed/Offset;I)V");
  public static final NormalMethod rdFieldReadBarrierStaticResolvedSamplingMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierStaticResolvedSampling", "(Lorg/vmmagic/unboxed/Offset;II)V");
  public static final NormalMethod rdFieldReadBarrierStaticResolvedSamplingStatsMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierStaticResolvedSamplingStats", "(Lorg/vmmagic/unboxed/Offset;II)V");
  public static final NormalMethod rdFieldReadBarrierResolvedVolatileMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierResolvedVolatile", "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;)V");
  public static final NormalMethod rdFieldReadBarrierStaticResolvedVolatileMethod    = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierStaticResolvedVolatile", "(Lorg/vmmagic/unboxed/Offset;)V");
  public static final NormalMethod rdFieldWriteBarrierResolvedMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierResolved", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdFieldWriteBarrierResolvedSamplingMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierResolvedSampling", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdFieldWriteBarrierResolvedSamplingStatsMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierResolvedSamplingStats", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdFieldWriteBarrierResolvedUsePassedMetadataMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierResolvedUsePassedMetadata", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdFieldWriteBarrierStaticResolvedMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierStaticResolved", "(Lorg/vmmagic/unboxed/Offset;I)V");
  public static final NormalMethod rdFieldWriteBarrierStaticResolvedSamplingMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierStaticResolvedSampling", "(Lorg/vmmagic/unboxed/Offset;II)V");
  public static final NormalMethod rdFieldWriteBarrierStaticResolvedSamplingStatsMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierStaticResolvedSamplingStats", "(Lorg/vmmagic/unboxed/Offset;II)V");
  public static final NormalMethod rdFieldWriteBarrierResolvedVolatileMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierResolvedVolatile", "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;)V");
  public static final NormalMethod rdFieldWriteBarrierStaticResolvedVolatileMethod   = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierStaticResolvedVolatile", "(Lorg/vmmagic/unboxed/Offset;)V");
  public static final NormalMethod rdFieldReadBarrierUnresolvedMethod  = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierUnresolved", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdFieldReadBarrierStaticUnresolvedMethod  = getMethod(org.jikesrvm.rd.Barriers.class, "fieldReadBarrierStaticUnresolved", "(II)V");
  public static final NormalMethod rdFieldWriteBarrierUnresolvedMethod = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierUnresolved", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdFieldWriteBarrierStaticUnresolvedMethod = getMethod(org.jikesrvm.rd.Barriers.class, "fieldWriteBarrierStaticUnresolved", "(II)V");
  public static final NormalMethod rdArrayReadBarrierMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayReadBarrier", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdArrayReadBarrierSamplingMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayReadBarrierSampling", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdArrayReadBarrierSamplingStatsMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayReadBarrierSamplingStats", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdArrayReadBarrierUsePassedMetadataMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayReadBarrierUsePassedMetadata", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdArrayWriteBarrierMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayWriteBarrier", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdArrayWriteBarrierSamplingMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayWriteBarrierSampling", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdArrayWriteBarrierSamplingStatsMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayWriteBarrierSamplingStats", "(Ljava/lang/Object;III)V");
  public static final NormalMethod rdArrayWriteBarrierUsePassedMetadataMethod = getMethod(org.jikesrvm.rd.Barriers.class, "arrayWriteBarrierUsePassedMetadata", "(Ljava/lang/Object;II)V");
  public static final NormalMethod rdIndirectMetadataBarrierMethod = getMethod(org.jikesrvm.rd.GetMetadata.class, "indirectMetadataBarrier", "(Ljava/lang/Object;I)Ljava/lang/Object;");
  
  public static final NormalMethod rdLiteRaceMethodSampleMethod = getMethod(org.jikesrvm.rd.LiteRace.class, "methodSample", "(I)V");
  
  public static final RVMField rdSamplingActivatedField = getField(org.jikesrvm.rd.Sampling.class, "samplingActivated", int.class);
  public static final RVMField rdRaceCountField = getField(org.jikesrvm.rd.Race.class, "count", int.class);
  public static final RVMField rdStatValueField = getField(org.jikesrvm.rd.Stats.Stat.class, "value", long.class);
  
  
  public static final NormalMethod lazyMethodInvokerMethod =
      getMethod(org.jikesrvm.runtime.DynamicLinker.class, "lazyMethodInvoker", "()V");
  public static final NormalMethod unimplementedNativeMethodMethod =
      getMethod(org.jikesrvm.runtime.DynamicLinker.class, "unimplementedNativeMethod", "()V");
  public static final NormalMethod sysCallMethod =
      getMethod(org.jikesrvm.runtime.DynamicLinker.class, "sysCallMethod", "()V");

  public static final NormalMethod resolveMemberMethod =
      getMethod(org.jikesrvm.classloader.TableBasedDynamicLinker.class, "resolveMember", "(I)I");
  public static final RVMField memberOffsetsField =
      getField(org.jikesrvm.classloader.TableBasedDynamicLinker.class, "memberOffsets", int[].class);

  /** 1L */
  public static final RVMField longOneField = getField(org.jikesrvm.runtime.MathConstants.class, "longOne", long.class);
  /** -1.0F */
  public static final RVMField minusOneField = getField(org.jikesrvm.runtime.MathConstants.class, "minusOne", float.class);
  /** 0.0F */
  public static final RVMField zeroFloatField = getField(org.jikesrvm.runtime.MathConstants.class, "zero", float.class);
  /**0.5F */
  public static final RVMField halfFloatField = getField(org.jikesrvm.runtime.MathConstants.class, "half", float.class);
  /** 1.0F */
  public static final RVMField oneFloatField = getField(org.jikesrvm.runtime.MathConstants.class, "one", float.class);
  /** 2.0F */
  public static final RVMField twoFloatField = getField(org.jikesrvm.runtime.MathConstants.class, "two", float.class);
  /** 2.0F^32 */
  public static final RVMField two32Field = getField(org.jikesrvm.runtime.MathConstants.class, "two32", float.class);
  /** 0.5F^32 */
  public static final RVMField half32Field = getField(org.jikesrvm.runtime.MathConstants.class, "half32", float.class);
  /** 1e-9 */
  public static final RVMField billionthField = getField(org.jikesrvm.runtime.MathConstants.class, "billionth", double.class);
  /** 0.0 */
  public static final RVMField zeroDoubleField = getField(org.jikesrvm.runtime.MathConstants.class, "zeroD", double.class);
  /** 1.0 */
  public static final RVMField oneDoubleField = getField(org.jikesrvm.runtime.MathConstants.class, "oneD", double.class);
  /** largest double that can be rounded to an int */
  public static final RVMField maxintField =
      getField(org.jikesrvm.runtime.MathConstants.class, "maxint", double.class);
  /** largest double that can be rounded to a long */
  public static final RVMField maxlongField =
    getField(org.jikesrvm.runtime.MathConstants.class, "maxlong", double.class);
  /** smallest double that can be rounded to an int */
  public static final RVMField minintField =
      getField(org.jikesrvm.runtime.MathConstants.class, "minint", double.class);
  /** largest float that can be rounded to an int */
  public static final RVMField maxintFloatField =
    getField(org.jikesrvm.runtime.MathConstants.class, "maxintF", float.class);
  /** largest float that can be rounded to a long */
  public static final RVMField maxlongFloatField =
    getField(org.jikesrvm.runtime.MathConstants.class, "maxlongF", float.class);
  /** IEEEmagic constant */
  public static final RVMField IEEEmagicField =
      getField(org.jikesrvm.runtime.MathConstants.class, "IEEEmagic", double.class);
  /** special double value for use in int <--> double conversions */
  public static final RVMField I2DconstantField =
      getField(org.jikesrvm.runtime.MathConstants.class,
               "I2Dconstant",
               double.class);

  public static final RVMField bootThreadField =
    getField(org.jikesrvm.scheduler.RVMThread.class, "bootThread",
             org.jikesrvm.scheduler.RVMThread.class);

  public static final RVMField scratchStorageField =
      getField(org.jikesrvm.scheduler.RVMThread.class, "scratchStorage", double.class);
  public static final RVMField takeYieldpointField =
      getField(org.jikesrvm.scheduler.RVMThread.class, "takeYieldpoint", int.class);
  
  public static final RVMField execStatusField = getField(org.jikesrvm.scheduler.RVMThread.class, "execStatus", int.class);

  public static final RVMField referenceReferentField =
      getField(java.lang.ref.Reference.class, "_referent", org.vmmagic.unboxed.Address.class);

  /** Used in deciding which stack frames we can elide when printing. */
  public static final NormalMethod mainThreadRunMethod =
      getMethod(org.jikesrvm.scheduler.MainThread.class, "run", "()V");

  public static final NormalMethod yieldpointFromPrologueMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "yieldpointFromPrologue", "()V");
  public static final NormalMethod yieldpointFromBackedgeMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "yieldpointFromBackedge", "()V");
  public static final NormalMethod yieldpointFromEpilogueMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "yieldpointFromEpilogue", "()V");
  public static final NormalMethod enterJNIBlockedFromJNIFunctionCallMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "enterJNIBlockedFromJNIFunctionCall", "()V");
  public static final NormalMethod enterJNIBlockedFromCallIntoNativeMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "enterJNIBlockedFromCallIntoNative", "()V");
  public static final NormalMethod leaveJNIBlockedFromJNIFunctionCallMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "leaveJNIBlockedFromJNIFunctionCall", "()V");
  public static final NormalMethod leaveJNIBlockedFromCallIntoNativeMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "leaveJNIBlockedFromCallIntoNative", "()V");

  public static final NormalMethod threadRunMethod = getMethod(org.jikesrvm.scheduler.RVMThread.class, "run", "()V");
  public static final NormalMethod threadStartoffMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "startoff", "()V");
  public static final RVMField threadStackField = getField(org.jikesrvm.scheduler.RVMThread.class, "stack", byte[].class);
  public static final RVMField stackLimitField =
      getField(org.jikesrvm.scheduler.RVMThread.class, "stackLimit", org.vmmagic.unboxed.Address.class);

  public static final RVMField threadSlotField = getField(org.jikesrvm.scheduler.RVMThread.class, "threadSlot", int.class);
  public static final RVMField jniEnvField =
      getField(org.jikesrvm.scheduler.RVMThread.class, "jniEnv", org.jikesrvm.jni.JNIEnvironment.class);
  public static final RVMField threadContextRegistersField =
      getField(org.jikesrvm.scheduler.RVMThread.class,
               "contextRegisters",
               org.jikesrvm.ArchitectureSpecific.Registers.class);
  public static final RVMField threadContextRegistersSaveField =
      getField(org.jikesrvm.scheduler.RVMThread.class,
               "contextRegistersSave",
               org.jikesrvm.ArchitectureSpecific.Registers.class);
  public static final RVMField threadExceptionRegistersField =
      getField(org.jikesrvm.scheduler.RVMThread.class,
               "exceptionRegisters",
               org.jikesrvm.ArchitectureSpecific.Registers.class);

  public static final RVMField tracePrevAddressField =
      getField(org.jikesrvm.objectmodel.MiscHeader.class, "prevAddress", org.vmmagic.unboxed.Word.class);
  public static final RVMField traceOIDField =
      getField(org.jikesrvm.objectmodel.MiscHeader.class, "oid", org.vmmagic.unboxed.Word.class);
  /*
  public static final RVMField dispenserField = getField(org.jikesrvm.mm.mmtk.Lock.class, "dispenser", int.class);
  public static final RVMField servingField = getField(org.jikesrvm.mm.mmtk.Lock.class, "serving", int.class);
  public static final RVMField lockThreadField =
      getField(org.jikesrvm.mm.mmtk.Lock.class, "thread", org.jikesrvm.scheduler.RVMThread.class);
  */
  public static final RVMField lockStateField = getField(org.jikesrvm.mm.mmtk.Lock.class, "state", int.class);
  public static final RVMField gcStatusField = getField(org.mmtk.plan.Plan.class, "gcStatus", int.class);
  public static final RVMField SQCFField = getField(org.mmtk.utility.deque.SharedDeque.class, "completionFlag", int.class);
  public static final RVMField SQNCField = getField(org.mmtk.utility.deque.SharedDeque.class, "numConsumers", int.class);
  public static final RVMField SQNCWField =
      getField(org.mmtk.utility.deque.SharedDeque.class, "numConsumersWaiting", int.class);
  public static final RVMField SQheadField =
      getField(org.mmtk.utility.deque.SharedDeque.class, "head", org.vmmagic.unboxed.Address.class);
  public static final RVMField SQtailField =
      getField(org.mmtk.utility.deque.SharedDeque.class, "tail", org.vmmagic.unboxed.Address.class);
  public static final RVMField SQBEField = getField(org.mmtk.utility.deque.SharedDeque.class, "bufsenqueued", int.class);
  public static final RVMField synchronizedCounterField =
      getField(org.jikesrvm.mm.mmtk.SynchronizedCounter.class, "count", int.class);

  public static final NormalMethod arrayStoreWriteBarrierMethod =
      getMethod(org.jikesrvm.mm.mminterface.MemoryManager.class, "arrayStoreWriteBarrier", "(Ljava/lang/Object;ILjava/lang/Object;)V");
  public static final NormalMethod putfieldWriteBarrierMethod =
      getMethod(org.jikesrvm.mm.mminterface.MemoryManager.class, "putfieldWriteBarrier", "(Ljava/lang/Object;Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;I)V");
  public static final NormalMethod putstaticWriteBarrierMethod =
      getMethod(org.jikesrvm.mm.mminterface.MemoryManager.class, "putstaticWriteBarrier", "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;I)V");

  public static final NormalMethod arrayLoadReadBarrierMethod =
      getMethod(org.jikesrvm.mm.mminterface.MemoryManager.class, "arrayLoadReadBarrier", "(Ljava/lang/Object;I)Ljava/lang/Object;");
  public static final NormalMethod getfieldReadBarrierMethod =
      getMethod(org.jikesrvm.mm.mminterface.MemoryManager.class, "getfieldReadBarrier", "(Ljava/lang/Object;Lorg/vmmagic/unboxed/Offset;I)Ljava/lang/Object;");
  public static final NormalMethod getstaticReadBarrierMethod =
      getMethod(org.jikesrvm.mm.mminterface.MemoryManager.class, "getstaticReadBarrier", "(Lorg/vmmagic/unboxed/Offset;I)Ljava/lang/Object;");

  public static final NormalMethod modifyCheckMethod =
      getMethod(org.jikesrvm.mm.mminterface.MemoryManager.class, "modifyCheck", "(Ljava/lang/Object;)V");

  // used in boot image writer
  public static final RVMField debugRequestedField =
      getField(org.jikesrvm.scheduler.RVMThread.class, "debugRequested", boolean.class);
  public static final NormalMethod dumpStackAndDieMethod =
      getMethod(org.jikesrvm.scheduler.RVMThread.class, "dumpStackAndDie", "(Lorg/vmmagic/unboxed/Address;)V");

  public static final RVMField latestContenderField =
      getField(org.jikesrvm.scheduler.SpinLock.class, "latestContender", org.jikesrvm.scheduler.RVMThread.class);

  public static final RVMField depthField = getField(org.jikesrvm.classloader.RVMType.class, "depth", int.class);
  public static final RVMField idField = getField(org.jikesrvm.classloader.RVMType.class, "id", int.class);
  public static final RVMField dimensionField = getField(org.jikesrvm.classloader.RVMType.class, "dimension", int.class);

  public static final RVMField innermostElementTypeDimensionField =
      getField(org.jikesrvm.classloader.RVMArray.class, "innermostElementTypeDimension", int.class);

  public static final RVMField JNIEnvSavedTRField =
      getField(org.jikesrvm.jni.JNIEnvironment.class, "savedTRreg", org.jikesrvm.scheduler.RVMThread.class);
  public static final RVMField JNIEnvBasePointerOnEntryToNative =
      getField(org.jikesrvm.jni.JNIEnvironment.class, "basePointerOnEntryToNative", org.vmmagic.unboxed.Address.class);
  public static final RVMField JNIGlobalRefsField =
    getField(org.jikesrvm.jni.JNIGlobalRefTable.class, "JNIGlobalRefs", org.vmmagic.unboxed.AddressArray.class);
  public static final RVMField JNIRefsField =
      getField(org.jikesrvm.jni.JNIEnvironment.class, "JNIRefs", org.vmmagic.unboxed.AddressArray.class);
  public static final RVMField JNIRefsTopField = getField(org.jikesrvm.jni.JNIEnvironment.class, "JNIRefsTop", int.class);
  public static final RVMField JNIRefsMaxField = getField(org.jikesrvm.jni.JNIEnvironment.class, "JNIRefsMax", int.class);
  public static final RVMField JNIRefsSavedFPField =
      getField(org.jikesrvm.jni.JNIEnvironment.class, "JNIRefsSavedFP", int.class);
  public static final RVMField JNITopJavaFPField =
      getField(org.jikesrvm.jni.JNIEnvironment.class, "JNITopJavaFP", org.vmmagic.unboxed.Address.class);
  public static final RVMField JNIPendingExceptionField =
      getField(org.jikesrvm.jni.JNIEnvironment.class, "pendingException", java.lang.Throwable.class);
  public static final RVMField JNIExternalFunctionsField =
      getField(org.jikesrvm.jni.JNIEnvironment.class, "externalJNIFunctions", org.vmmagic.unboxed.Address.class);
  public static final RVMField JNIEnvSavedJTOCField =
      VM.BuildForPowerPC ? getField(org.jikesrvm.jni.JNIEnvironment.class,
                                    "savedJTOC",
                                    org.vmmagic.unboxed.Address.class) : null;
  public static final RVMMethod jniEntry =
      VM.BuildForIA32 ? getMethod(org.jikesrvm.jni.JNIEnvironment.class,
                                  "entryToJNI",
                                  "(I)V") : null;
  public static final RVMMethod jniExit =
      VM.BuildForIA32 ? getMethod(org.jikesrvm.jni.JNIEnvironment.class,
                                  "exitFromJNI",
                                  "(I)Ljava/lang/Object;") : null;

  public static final RVMField the_boot_recordField =
      getField(org.jikesrvm.runtime.BootRecord.class, "the_boot_record", org.jikesrvm.runtime.BootRecord.class);
  public static final RVMField externalSignalFlagField =
      getField(org.jikesrvm.runtime.BootRecord.class, "externalSignalFlag", int.class);
  public static final RVMField sysLongDivideIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysLongDivideIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysLongRemainderIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysLongRemainderIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysLongToFloatIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysLongToFloatIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysLongToDoubleIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysLongToDoubleIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysFloatToIntIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysFloatToIntIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysDoubleToIntIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysDoubleToIntIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysFloatToLongIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysFloatToLongIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysDoubleToLongIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysDoubleToLongIP", org.vmmagic.unboxed.Address.class);
  public static final RVMField sysDoubleRemainderIPField =
      getField(org.jikesrvm.runtime.BootRecord.class, "sysDoubleRemainderIP", org.vmmagic.unboxed.Address.class);

  public static final RVMField edgeCountersField =
      getField(org.jikesrvm.compilers.baseline.EdgeCounts.class, "data", int[][].class);

  //////////////////
  // Entrypoints that are valid only when the opt compiler is included in the build
  //////////////////
  public static final RVMField specializedMethodsField;

  public static final NormalMethod optThreadSwitchFromOsrOptMethod;
  public static final NormalMethod optThreadSwitchFromPrologueMethod;
  public static final NormalMethod optThreadSwitchFromBackedgeMethod;
  public static final NormalMethod optThreadSwitchFromEpilogueMethod;
  public static final NormalMethod yieldpointFromNativePrologueMethod;
  public static final NormalMethod yieldpointFromNativeEpilogueMethod;
  public static final NormalMethod optResolveMethod;
  public static final NormalMethod optNewArrayArrayMethod;
  public static final NormalMethod optNew2DArrayMethod;
  public static final NormalMethod sysArrayCopy;

  static {
    if (VM.BuildForOptCompiler) {
      specializedMethodsField =
          getField(org.jikesrvm.compilers.opt.specialization.SpecializedMethodPool.class,
                   "specializedMethods",
                   org.jikesrvm.ArchitectureSpecific.CodeArray[].class);
      optThreadSwitchFromOsrOptMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptSaveVolatile.class, "yieldpointFromOsrOpt", "()V");
      optThreadSwitchFromPrologueMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptSaveVolatile.class, "yieldpointFromPrologue", "()V");
      optThreadSwitchFromBackedgeMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptSaveVolatile.class, "yieldpointFromBackedge", "()V");
      optThreadSwitchFromEpilogueMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptSaveVolatile.class, "yieldpointFromEpilogue", "()V");
      yieldpointFromNativePrologueMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptSaveVolatile.class, "yieldpointFromNativePrologue", "()V");
      yieldpointFromNativeEpilogueMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptSaveVolatile.class, "yieldpointFromNativeEpilogue", "()V");
      optResolveMethod = getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptSaveVolatile.class, "resolve", "()V");

      optNewArrayArrayMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptLinker.class, "newArrayArray", "(I[II)Ljava/lang/Object;");
      optNew2DArrayMethod =
          getMethod(org.jikesrvm.compilers.opt.runtimesupport.OptLinker.class, "new2DArray", "(IIII)Ljava/lang/Object;");
      sysArrayCopy = getMethod("Ljava/lang/VMCommonLibrarySupport;", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V");
      sysArrayCopy.setRuntimeServiceMethod(false);
    } else {
      specializedMethodsField = null;
      optThreadSwitchFromOsrOptMethod = null;
      optThreadSwitchFromPrologueMethod = null;
      optThreadSwitchFromBackedgeMethod = null;
      optThreadSwitchFromEpilogueMethod = null;
      yieldpointFromNativePrologueMethod = null;
      yieldpointFromNativeEpilogueMethod = null;
      optResolveMethod = null;
      optNewArrayArrayMethod = null;
      optNew2DArrayMethod = null;
      sysArrayCopy = null;
    }
  }

  public static final RVMField classLoaderDefinedPackages =
    getField(java.lang.ClassLoader.class, "definedPackages", java.util.HashMap.class);

  public static final RVMField luni1;
  public static final RVMField luni2;
  public static final RVMField luni3;
  public static final RVMField luni4;
  public static final RVMField luni5;
  public static final RVMField luni6;

  static {
    if (VM.BuildForHarmony) {
      luni1 = getField("Lorg/apache/harmony/luni/util/Msg;", "bundle", java.util.ResourceBundle.class);
      luni2 = getField("Lorg/apache/harmony/archive/internal/nls/Messages;", "bundle", java.util.ResourceBundle.class);
      luni3 = getField("Lorg/apache/harmony/luni/internal/nls/Messages;", "bundle", java.util.ResourceBundle.class);
      luni4 = getField("Lorg/apache/harmony/nio/internal/nls/Messages;", "bundle", java.util.ResourceBundle.class);
      luni5 = getField("Lorg/apache/harmony/niochar/internal/nls/Messages;", "bundle", java.util.ResourceBundle.class);
      luni6 = getField(java.util.logging.LogManager.class, "manager", java.util.logging.LogManager.class);
    } else {
      luni1 = null;
      luni2 = null;
      luni3 = null;
      luni4 = null;
      luni5 = null;
      luni6 = null;
    }
  }
}

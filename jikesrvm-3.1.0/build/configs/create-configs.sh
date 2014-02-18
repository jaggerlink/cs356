#!/bin/bash

# RaceDet: generate configurations

for i in *{GenImmix,GenMS,Immix}.properties
do

  OUT=`echo $i | sed s/.properties/_fieldMetadata.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT

  OUT=`echo $i | sed s/.properties/_syncOps.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT

  OUT=`echo $i | sed s/.properties/_syncOpsSampling.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.sampling=true >> $OUT

  OUT=`echo $i | sed s/.properties/_syncOpsStats.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.stats=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdDefault.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSampling.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdLiteRace.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.lite-race=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingNoInlining.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.no-inlining=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingRBE.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.rbe=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingNoCheckHoisting.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.no-check-hoisting=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingPassIndirectMetadata.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.pass-indirect-metadata=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingNoEscapeAnalysis.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.no-escape-analysis=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingStats.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.stats=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingStatsSIHB.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.stats=true >> $OUT
  echo config.rd.static-init-happens-before=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingPassIndirectMetadata.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.pass-indirect-metadata=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingNoOpt.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.no-opt=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdSamplingStaticsOnly.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.sampling=true >> $OUT
  echo config.rd.statics-only=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdStats.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.stats=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoEscapeAnalysis.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-escape-analysis=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoEscapeAnalysisStats.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-escape-analysis=true >> $OUT
  echo config.rd.stats=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdStaticsOnly.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.statics-only=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdEmptyBarriers.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.empty-barriers=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoArrays.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-arrays=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdPassIndirectMetadata.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.pass-indirect-metadata=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoBase.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-base=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoOpt.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-opt=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoBarriers.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-base=true >> $OUT
  echo config.rd.no-opt=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdTrackTimes.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.stats=true >> $OUT
  echo config.rd.track-times=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdTrackObjects.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.stats=true >> $OUT
  echo config.rd.track-objects=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdTrackAllocSites.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.stats=true >> $OUT
  echo config.rd.track-alloc-sites=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoInlining.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-inlining=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdRBE.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.rbe=true >> $OUT

  OUT=`echo $i | sed s/.properties/_rdNoBarrierCAS.properties/`
  cp $i $OUT
  echo config.rd.field-metadata=true >> $OUT
  echo config.rd.sync-ops=true >> $OUT
  echo config.rd.barriers=true >> $OUT
  echo config.rd.no-barrier-cas=true >> $OUT

done

package com.msa.android.data.source.local;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class TechnicalAnalysisDataSource_Factory implements Factory<TechnicalAnalysisDataSource> {
  @Override
  public TechnicalAnalysisDataSource get() {
    return newInstance();
  }

  public static TechnicalAnalysisDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TechnicalAnalysisDataSource newInstance() {
    return new TechnicalAnalysisDataSource();
  }

  private static final class InstanceHolder {
    private static final TechnicalAnalysisDataSource_Factory INSTANCE = new TechnicalAnalysisDataSource_Factory();
  }
}

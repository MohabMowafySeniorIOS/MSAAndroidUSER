package com.msa.android.domain.usecase;

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
public final class SilverValueCalculator_Factory implements Factory<SilverValueCalculator> {
  @Override
  public SilverValueCalculator get() {
    return newInstance();
  }

  public static SilverValueCalculator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SilverValueCalculator newInstance() {
    return new SilverValueCalculator();
  }

  private static final class InstanceHolder {
    private static final SilverValueCalculator_Factory INSTANCE = new SilverValueCalculator_Factory();
  }
}

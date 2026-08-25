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
public final class CalculatePortfolioValue_Factory implements Factory<CalculatePortfolioValue> {
  @Override
  public CalculatePortfolioValue get() {
    return newInstance();
  }

  public static CalculatePortfolioValue_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CalculatePortfolioValue newInstance() {
    return new CalculatePortfolioValue();
  }

  private static final class InstanceHolder {
    private static final CalculatePortfolioValue_Factory INSTANCE = new CalculatePortfolioValue_Factory();
  }
}

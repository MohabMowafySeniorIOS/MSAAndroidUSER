package com.msa.android.presentation.screens.calculators.silvervalue;

import com.msa.android.domain.repository.MetalsRepository;
import com.msa.android.domain.usecase.SilverValueCalculator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SilverValueViewModel_Factory implements Factory<SilverValueViewModel> {
  private final Provider<MetalsRepository> metalsRepoProvider;

  private final Provider<SilverValueCalculator> calcProvider;

  public SilverValueViewModel_Factory(Provider<MetalsRepository> metalsRepoProvider,
      Provider<SilverValueCalculator> calcProvider) {
    this.metalsRepoProvider = metalsRepoProvider;
    this.calcProvider = calcProvider;
  }

  @Override
  public SilverValueViewModel get() {
    return newInstance(metalsRepoProvider.get(), calcProvider.get());
  }

  public static SilverValueViewModel_Factory create(Provider<MetalsRepository> metalsRepoProvider,
      Provider<SilverValueCalculator> calcProvider) {
    return new SilverValueViewModel_Factory(metalsRepoProvider, calcProvider);
  }

  public static SilverValueViewModel newInstance(MetalsRepository metalsRepo,
      SilverValueCalculator calc) {
    return new SilverValueViewModel(metalsRepo, calc);
  }
}

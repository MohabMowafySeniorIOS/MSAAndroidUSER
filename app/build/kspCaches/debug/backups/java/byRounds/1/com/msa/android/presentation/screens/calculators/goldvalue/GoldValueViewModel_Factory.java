package com.msa.android.presentation.screens.calculators.goldvalue;

import com.msa.android.domain.repository.MetalsRepository;
import com.msa.android.domain.usecase.GoldValueCalculator;
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
public final class GoldValueViewModel_Factory implements Factory<GoldValueViewModel> {
  private final Provider<MetalsRepository> metalsRepoProvider;

  private final Provider<GoldValueCalculator> calculatorProvider;

  public GoldValueViewModel_Factory(Provider<MetalsRepository> metalsRepoProvider,
      Provider<GoldValueCalculator> calculatorProvider) {
    this.metalsRepoProvider = metalsRepoProvider;
    this.calculatorProvider = calculatorProvider;
  }

  @Override
  public GoldValueViewModel get() {
    return newInstance(metalsRepoProvider.get(), calculatorProvider.get());
  }

  public static GoldValueViewModel_Factory create(Provider<MetalsRepository> metalsRepoProvider,
      Provider<GoldValueCalculator> calculatorProvider) {
    return new GoldValueViewModel_Factory(metalsRepoProvider, calculatorProvider);
  }

  public static GoldValueViewModel newInstance(MetalsRepository metalsRepo,
      GoldValueCalculator calculator) {
    return new GoldValueViewModel(metalsRepo, calculator);
  }
}

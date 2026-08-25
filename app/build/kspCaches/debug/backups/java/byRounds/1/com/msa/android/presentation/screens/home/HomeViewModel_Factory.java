package com.msa.android.presentation.screens.home;

import com.msa.android.domain.repository.BanksRepository;
import com.msa.android.domain.repository.MetalsRepository;
import com.msa.android.domain.usecase.PriceCalculator;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<MetalsRepository> metalsRepoProvider;

  private final Provider<BanksRepository> banksRepoProvider;

  private final Provider<PriceCalculator> priceCalculatorProvider;

  public HomeViewModel_Factory(Provider<MetalsRepository> metalsRepoProvider,
      Provider<BanksRepository> banksRepoProvider,
      Provider<PriceCalculator> priceCalculatorProvider) {
    this.metalsRepoProvider = metalsRepoProvider;
    this.banksRepoProvider = banksRepoProvider;
    this.priceCalculatorProvider = priceCalculatorProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(metalsRepoProvider.get(), banksRepoProvider.get(), priceCalculatorProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<MetalsRepository> metalsRepoProvider,
      Provider<BanksRepository> banksRepoProvider,
      Provider<PriceCalculator> priceCalculatorProvider) {
    return new HomeViewModel_Factory(metalsRepoProvider, banksRepoProvider, priceCalculatorProvider);
  }

  public static HomeViewModel newInstance(MetalsRepository metalsRepo, BanksRepository banksRepo,
      PriceCalculator priceCalculator) {
    return new HomeViewModel(metalsRepo, banksRepo, priceCalculator);
  }
}

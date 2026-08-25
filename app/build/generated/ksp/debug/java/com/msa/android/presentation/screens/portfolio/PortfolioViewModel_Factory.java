package com.msa.android.presentation.screens.portfolio;

import com.msa.android.domain.repository.MetalsRepository;
import com.msa.android.domain.repository.PortfolioRepository;
import com.msa.android.domain.usecase.CalculatePortfolioValue;
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
public final class PortfolioViewModel_Factory implements Factory<PortfolioViewModel> {
  private final Provider<PortfolioRepository> portfolioRepoProvider;

  private final Provider<MetalsRepository> metalsRepoProvider;

  private final Provider<PriceCalculator> priceCalculatorProvider;

  private final Provider<CalculatePortfolioValue> calculateValueProvider;

  public PortfolioViewModel_Factory(Provider<PortfolioRepository> portfolioRepoProvider,
      Provider<MetalsRepository> metalsRepoProvider,
      Provider<PriceCalculator> priceCalculatorProvider,
      Provider<CalculatePortfolioValue> calculateValueProvider) {
    this.portfolioRepoProvider = portfolioRepoProvider;
    this.metalsRepoProvider = metalsRepoProvider;
    this.priceCalculatorProvider = priceCalculatorProvider;
    this.calculateValueProvider = calculateValueProvider;
  }

  @Override
  public PortfolioViewModel get() {
    return newInstance(portfolioRepoProvider.get(), metalsRepoProvider.get(), priceCalculatorProvider.get(), calculateValueProvider.get());
  }

  public static PortfolioViewModel_Factory create(
      Provider<PortfolioRepository> portfolioRepoProvider,
      Provider<MetalsRepository> metalsRepoProvider,
      Provider<PriceCalculator> priceCalculatorProvider,
      Provider<CalculatePortfolioValue> calculateValueProvider) {
    return new PortfolioViewModel_Factory(portfolioRepoProvider, metalsRepoProvider, priceCalculatorProvider, calculateValueProvider);
  }

  public static PortfolioViewModel newInstance(PortfolioRepository portfolioRepo,
      MetalsRepository metalsRepo, PriceCalculator priceCalculator,
      CalculatePortfolioValue calculateValue) {
    return new PortfolioViewModel(portfolioRepo, metalsRepo, priceCalculator, calculateValue);
  }
}

package com.msa.android.presentation.screens.portfolio;

import com.msa.android.domain.repository.PortfolioRepository;
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
public final class AddPortfolioItemViewModel_Factory implements Factory<AddPortfolioItemViewModel> {
  private final Provider<PortfolioRepository> repoProvider;

  public AddPortfolioItemViewModel_Factory(Provider<PortfolioRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public AddPortfolioItemViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static AddPortfolioItemViewModel_Factory create(
      Provider<PortfolioRepository> repoProvider) {
    return new AddPortfolioItemViewModel_Factory(repoProvider);
  }

  public static AddPortfolioItemViewModel newInstance(PortfolioRepository repo) {
    return new AddPortfolioItemViewModel(repo);
  }
}

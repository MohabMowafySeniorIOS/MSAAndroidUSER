package com.msa.android.presentation.screens.technicalanalysis;

import com.msa.android.data.source.local.TechnicalAnalysisDataSource;
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
public final class TechnicalAnalysisViewModel_Factory implements Factory<TechnicalAnalysisViewModel> {
  private final Provider<TechnicalAnalysisDataSource> sourceProvider;

  public TechnicalAnalysisViewModel_Factory(Provider<TechnicalAnalysisDataSource> sourceProvider) {
    this.sourceProvider = sourceProvider;
  }

  @Override
  public TechnicalAnalysisViewModel get() {
    return newInstance(sourceProvider.get());
  }

  public static TechnicalAnalysisViewModel_Factory create(
      Provider<TechnicalAnalysisDataSource> sourceProvider) {
    return new TechnicalAnalysisViewModel_Factory(sourceProvider);
  }

  public static TechnicalAnalysisViewModel newInstance(TechnicalAnalysisDataSource source) {
    return new TechnicalAnalysisViewModel(source);
  }
}

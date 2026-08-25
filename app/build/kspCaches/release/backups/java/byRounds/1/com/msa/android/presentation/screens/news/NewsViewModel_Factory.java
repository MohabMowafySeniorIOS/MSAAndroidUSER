package com.msa.android.presentation.screens.news;

import com.msa.android.domain.repository.NewsRepository;
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
public final class NewsViewModel_Factory implements Factory<NewsViewModel> {
  private final Provider<NewsRepository> repoProvider;

  public NewsViewModel_Factory(Provider<NewsRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public NewsViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static NewsViewModel_Factory create(Provider<NewsRepository> repoProvider) {
    return new NewsViewModel_Factory(repoProvider);
  }

  public static NewsViewModel newInstance(NewsRepository repo) {
    return new NewsViewModel(repo);
  }
}

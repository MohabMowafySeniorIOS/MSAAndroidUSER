package com.msa.android.presentation.screens.more;

import com.msa.android.domain.repository.VersionRepository;
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
public final class MoreViewModel_Factory implements Factory<MoreViewModel> {
  private final Provider<VersionRepository> versionRepoProvider;

  public MoreViewModel_Factory(Provider<VersionRepository> versionRepoProvider) {
    this.versionRepoProvider = versionRepoProvider;
  }

  @Override
  public MoreViewModel get() {
    return newInstance(versionRepoProvider.get());
  }

  public static MoreViewModel_Factory create(Provider<VersionRepository> versionRepoProvider) {
    return new MoreViewModel_Factory(versionRepoProvider);
  }

  public static MoreViewModel newInstance(VersionRepository versionRepo) {
    return new MoreViewModel(versionRepo);
  }
}

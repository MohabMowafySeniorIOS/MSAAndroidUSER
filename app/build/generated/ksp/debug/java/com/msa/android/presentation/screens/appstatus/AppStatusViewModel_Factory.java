package com.msa.android.presentation.screens.appstatus;

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
public final class AppStatusViewModel_Factory implements Factory<AppStatusViewModel> {
  private final Provider<VersionRepository> versionRepoProvider;

  public AppStatusViewModel_Factory(Provider<VersionRepository> versionRepoProvider) {
    this.versionRepoProvider = versionRepoProvider;
  }

  @Override
  public AppStatusViewModel get() {
    return newInstance(versionRepoProvider.get());
  }

  public static AppStatusViewModel_Factory create(Provider<VersionRepository> versionRepoProvider) {
    return new AppStatusViewModel_Factory(versionRepoProvider);
  }

  public static AppStatusViewModel newInstance(VersionRepository versionRepo) {
    return new AppStatusViewModel(versionRepo);
  }
}

package com.msa.android.presentation.screens.appstatus;

import com.msa.android.data.source.local.LanguagePreferences;
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
public final class ScreenMaintenanceViewModel_Factory implements Factory<ScreenMaintenanceViewModel> {
  private final Provider<VersionRepository> versionRepoProvider;

  private final Provider<LanguagePreferences> languagePrefsProvider;

  public ScreenMaintenanceViewModel_Factory(Provider<VersionRepository> versionRepoProvider,
      Provider<LanguagePreferences> languagePrefsProvider) {
    this.versionRepoProvider = versionRepoProvider;
    this.languagePrefsProvider = languagePrefsProvider;
  }

  @Override
  public ScreenMaintenanceViewModel get() {
    return newInstance(versionRepoProvider.get(), languagePrefsProvider.get());
  }

  public static ScreenMaintenanceViewModel_Factory create(
      Provider<VersionRepository> versionRepoProvider,
      Provider<LanguagePreferences> languagePrefsProvider) {
    return new ScreenMaintenanceViewModel_Factory(versionRepoProvider, languagePrefsProvider);
  }

  public static ScreenMaintenanceViewModel newInstance(VersionRepository versionRepo,
      LanguagePreferences languagePrefs) {
    return new ScreenMaintenanceViewModel(versionRepo, languagePrefs);
  }
}

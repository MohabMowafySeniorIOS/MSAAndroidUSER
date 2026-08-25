package com.msa.android.presentation.screens.banks;

import com.msa.android.data.source.local.BankNotificationPreferences;
import com.msa.android.domain.repository.BanksRepository;
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
public final class BanksViewModel_Factory implements Factory<BanksViewModel> {
  private final Provider<BanksRepository> repoProvider;

  private final Provider<BankNotificationPreferences> notifPrefsProvider;

  public BanksViewModel_Factory(Provider<BanksRepository> repoProvider,
      Provider<BankNotificationPreferences> notifPrefsProvider) {
    this.repoProvider = repoProvider;
    this.notifPrefsProvider = notifPrefsProvider;
  }

  @Override
  public BanksViewModel get() {
    return newInstance(repoProvider.get(), notifPrefsProvider.get());
  }

  public static BanksViewModel_Factory create(Provider<BanksRepository> repoProvider,
      Provider<BankNotificationPreferences> notifPrefsProvider) {
    return new BanksViewModel_Factory(repoProvider, notifPrefsProvider);
  }

  public static BanksViewModel newInstance(BanksRepository repo,
      BankNotificationPreferences notifPrefs) {
    return new BanksViewModel(repo, notifPrefs);
  }
}

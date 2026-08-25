package com.msa.android.data.source.local;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class BankNotificationPreferences_Factory implements Factory<BankNotificationPreferences> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  public BankNotificationPreferences_Factory(Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public BankNotificationPreferences get() {
    return newInstance(dataStoreProvider.get());
  }

  public static BankNotificationPreferences_Factory create(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new BankNotificationPreferences_Factory(dataStoreProvider);
  }

  public static BankNotificationPreferences newInstance(DataStore<Preferences> dataStore) {
    return new BankNotificationPreferences(dataStore);
  }
}

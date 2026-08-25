package com.msa.android.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
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
public final class BanksRepositoryImpl_Factory implements Factory<BanksRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public BanksRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public BanksRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static BanksRepositoryImpl_Factory create(Provider<FirebaseFirestore> firestoreProvider) {
    return new BanksRepositoryImpl_Factory(firestoreProvider);
  }

  public static BanksRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new BanksRepositoryImpl(firestore);
  }
}

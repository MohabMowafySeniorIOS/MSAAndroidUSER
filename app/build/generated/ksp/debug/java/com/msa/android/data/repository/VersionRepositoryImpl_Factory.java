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
public final class VersionRepositoryImpl_Factory implements Factory<VersionRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public VersionRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public VersionRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static VersionRepositoryImpl_Factory create(
      Provider<FirebaseFirestore> firestoreProvider) {
    return new VersionRepositoryImpl_Factory(firestoreProvider);
  }

  public static VersionRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new VersionRepositoryImpl(firestore);
  }
}

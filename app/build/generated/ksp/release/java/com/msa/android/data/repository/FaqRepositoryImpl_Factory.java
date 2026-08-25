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
public final class FaqRepositoryImpl_Factory implements Factory<FaqRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public FaqRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public FaqRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static FaqRepositoryImpl_Factory create(Provider<FirebaseFirestore> firestoreProvider) {
    return new FaqRepositoryImpl_Factory(firestoreProvider);
  }

  public static FaqRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new FaqRepositoryImpl(firestore);
  }
}

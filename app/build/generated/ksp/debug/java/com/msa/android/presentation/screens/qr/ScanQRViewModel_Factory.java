package com.msa.android.presentation.screens.qr;

import com.msa.android.domain.repository.QRRepository;
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
public final class ScanQRViewModel_Factory implements Factory<ScanQRViewModel> {
  private final Provider<QRRepository> repositoryProvider;

  public ScanQRViewModel_Factory(Provider<QRRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScanQRViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScanQRViewModel_Factory create(Provider<QRRepository> repositoryProvider) {
    return new ScanQRViewModel_Factory(repositoryProvider);
  }

  public static ScanQRViewModel newInstance(QRRepository repository) {
    return new ScanQRViewModel(repository);
  }
}
